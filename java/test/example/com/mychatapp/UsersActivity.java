package test.example.com.mychatapp;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    ListView mUserActivityListView;
    ContactsAdapter mAdapter;


    ArrayList<String> phoneContacts=new ArrayList<>();
    ArrayList<Users> firebaseUsers=new ArrayList<>();
    ArrayList<Users> commonContacts=new ArrayList<>();

    ArrayList<Users> CopyUserList=new ArrayList<Users>();
    EditText mSearchText;
    ImageView mRefresh;
    ImageView mSearchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Select Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchIcon=findViewById(R.id.users_search_icon);
        mSearchText=findViewById(R.id.users_search_bar_edittext);
        mRefresh=findViewById(R.id.users_search_bar_refresh);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mAuth=FirebaseAuth.getInstance();
        ToolBarfunction();
    }

    @Override
    public void onBackPressed() {
        if(mSearchIcon.getVisibility()!=View.VISIBLE)
        {
            mSearchText.setVisibility(View.GONE);
            mSearchText.setText("");
            mRefresh.setVisibility(View.VISIBLE);
            mSearchIcon.setVisibility(View.VISIBLE);
        }
        else
            super.onBackPressed();

    }

    public void ToolBarfunction()
    {
        mSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchIcon.setVisibility(View.GONE);
                mSearchText.setVisibility(View.VISIBLE);
            }
        });

        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onResume();
                Toast.makeText(UsersActivity.this,"Refreshed",Toast.LENGTH_LONG).show();

            }
        });

        if(mSearchText!=null)
        {
            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    String text = mSearchText.getText().toString().toLowerCase(Locale.getDefault());

                    ArrayList<Users> Temp = new ArrayList<Users>();
                    Temp.addAll(CopyUserList);

                    commonContacts.clear();

                    if (text.length() == 0) {

                        commonContacts.addAll(CopyUserList);

                    } else {

                        for (Users wp : CopyUserList) {

                            if ((wp.getName().toLowerCase()).contains(text)) {

                                commonContacts.add(wp);
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        fetchContacts();

        mUserActivityListView=findViewById(R.id.ContactsListView_UsersActivity);
        mAdapter=new ContactsAdapter(UsersActivity.this,commonContacts);
        mUserActivityListView.setAdapter(mAdapter);

        mUserActivityListView.setOnItemClickListener(onItemClick);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                firebaseUsers.clear();

                for (final DataSnapshot user:dataSnapshot.getChildren())
                {
                    if(!mAuth.getCurrentUser().getUid().equals(user.getKey())) {

                        Users u=user.getValue(Users.class);
                        u.setUserid(user.getKey());

                        if(user.hasChild("online")) {

                            String userOnline = user.child("online").getValue().toString();
                            u.setUserOnlineStatus(userOnline);
                        }

                        firebaseUsers.add(u);
                    }

                }
                fetchCommonContacts();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

  }


    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent chatIntent = new Intent(UsersActivity.this, ChatActivity.class);
            chatIntent.putExtra("user_id", commonContacts.get(position).getUserid());
            chatIntent.putExtra("user_name", commonContacts.get(position).getName());

            if(getIntent().hasExtra("forwardList")) {

                ArrayList<String> forwardlist=new ArrayList<>();
                ArrayList<String> forwardtype=new ArrayList<>();

                forwardlist = getIntent().getStringArrayListExtra("forwardList");
                forwardtype = getIntent().getStringArrayListExtra("forwardType");

                chatIntent.putStringArrayListExtra("forwardList",forwardlist);
                chatIntent.putStringArrayListExtra("forwardType",forwardtype);

            }

            startActivity(chatIntent);
            finish();

        }
    };

    public void fetchCommonContacts()
    {
        commonContacts.clear();

        for (int i=0 ; i < firebaseUsers.size() ; i++)
        {

            String s=firebaseUsers.get(i).getPhone();
            String s2=new String();

            if(s!=null && s.contains("+92")) {
                s2=s.replace("+92", "0");
            }
            if(phoneContacts.contains(firebaseUsers.get(i).getPhone()) || phoneContacts.contains(s2))
            {
                commonContacts.add(firebaseUsers.get(i));
            }

        }


        CopyUserList.clear();
        CopyUserList.addAll(commonContacts);
    }

    public void fetchContacts()
    {
        phoneContacts.clear();

        ContentResolver contentResolver=getContentResolver();
        Cursor cursor=contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,null,null,null);

        if (cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                String id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber= Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber>0)
                {
                    Cursor cursor2=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"=?",
                            new String[]{id},
                            null);

                    while (cursor2.moveToNext())
                    {
                        String s=cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String s2=new String();
                        for (int i=0;i<s.length();i++)
                        {
                            if(i==0 && s.charAt(i)=='+')
                                s2="+";
                            if(s.charAt(i)<='9' && s.charAt(i)>='0')
                                s2=s2.concat(""+s.charAt(i));
                        }
                        if(s2.length()>0)
                            phoneContacts.add(s2);
                    }
                    cursor2.close();

                }
            }
        }
        cursor.close();
    }




}
