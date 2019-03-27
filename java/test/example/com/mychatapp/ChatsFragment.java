package test.example.com.mychatapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;


public class ChatsFragment extends Fragment {


    public ChatsFragment() {
        //empty constructor
    }


    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mChatUsersDatabase;
    private DatabaseReference mRootRef;

    private String mCurrent_user_id;

    private View mMainView;
    FloatingActionButton fabChats;
    ListView mChatsListView;
    ContactsAdapter chatsAdapter;

    ArrayList<Users> firebaseUsers=new ArrayList<>();
    ArrayList<Users> commonChatUsers=new ArrayList<>();
    ArrayList<String> mChatUsersList=new ArrayList<>();


    ArrayList<Users> CopyUserList=new ArrayList<Users>();
    EditText mChatsSearch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null)
        {
            //check if user is Online
            Intent startIntent = new Intent(mMainView.getContext(), MainActivity.class);
            startActivity(startIntent);
        }


        mCurrent_user_id = mAuth.getCurrentUser().getUid();


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        mChatUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");
        mChatUsersDatabase.keepSynced(true);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            //check if user is Online
            Intent startIntent = new Intent(mMainView.getContext(), MainActivity.class);
            startActivity(startIntent);
        }


        fabChats=mMainView.findViewById(R.id.fab_chats);
        fabChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(mMainView.getContext(),UsersActivity.class);
                startActivity(intent);
            }
        });



        mChatsListView=mMainView.findViewById(R.id.ChatsListView);
        chatsAdapter=new ContactsAdapter(mMainView.getContext(),commonChatUsers);

        mChatsSearch = mMainView.findViewById(R.id.ChatsSearch);
        mChatsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = mChatsSearch.getText().toString().toLowerCase(Locale.getDefault());


                ArrayList<Users> Temp = new ArrayList<Users>();
                Temp.addAll(CopyUserList);


                commonChatUsers.clear();

                if (text.length() == 0) {

                    commonChatUsers.addAll(CopyUserList);

                    //contactsAdapter.notifyDataSetChanged();
                } else {

                    for (Users wp : CopyUserList) {

                        if ((wp.getName().toLowerCase()).contains(text)) {

                            commonChatUsers.add(wp);
                        }
                    }
                }
                chatsAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mChatsListView.setAdapter(chatsAdapter);
        mChatsListView.setOnItemClickListener(onItemClick);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                firebaseUsers.clear();
                for (final DataSnapshot user:dataSnapshot.getChildren())
                {
                    if(mAuth.getCurrentUser()!=null && !mAuth.getCurrentUser().getUid().equals(user.getKey())) {

                        Users u=user.getValue(Users.class);
                        u.setUserid(user.getKey());

                        if(user.hasChild("online")) {

                            String userOnline = user.child("online").getValue().toString();
                            u.setUserOnlineStatus(userOnline);
                        }
                        firebaseUsers.add(u);
                    }
                }

                getCommonChats();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mChatUsersDatabase.child(mCurrent_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mChatUsersList.clear();
                for (DataSnapshot chatID:dataSnapshot.getChildren())
                {
                    mChatUsersList.add(chatID.getKey());
                }

                getCommonChats();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        loadMessages();


    }

    private void loadMessages() {
        mRootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference syncMessages=mRootRef.child("messages").child(mCurrent_user_id);
        syncMessages.keepSynced(true);


        mRootRef.child("messages").child(mCurrent_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user:dataSnapshot.getChildren())
                {
                    for (Users x:commonChatUsers)
                    {
                        if(x.getUserid().equals(user.getKey()))
                        {
                            for (DataSnapshot u : user.getChildren())
                            {
                                if(u.child("type").getValue().equals("text"))
                                    x.setStatus(u.child("message").getValue().toString());
                                else //changed in case of image
                                    x.setStatus(" ");

                            }
                        }
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void getCommonChats()
    {
        commonChatUsers.clear();

        for (Users users:firebaseUsers)
        {
            if(mChatUsersList.contains(users.getUserid()))
            {
                commonChatUsers.add(users);
            }
        }

        loadMessages();


        CopyUserList.clear();
        CopyUserList.addAll(commonChatUsers);

        chatsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        if (mAuth.getCurrentUser() == null) {
            //check if user is Online
            Intent startIntent = new Intent(mMainView.getContext(), MainActivity.class);
            startActivity(startIntent);
        }
        super.onResume();
        getCommonChats();
    }

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
            chatIntent.putExtra("user_id", commonChatUsers.get(position).getUserid());
            chatIntent.putExtra("user_name", commonChatUsers.get(position).getName());
            startActivity(chatIntent);
        }
    };


}
