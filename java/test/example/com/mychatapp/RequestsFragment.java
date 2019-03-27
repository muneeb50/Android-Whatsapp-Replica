package test.example.com.mychatapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {


    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;

    private String mCurrent_user_id;
    private View mMainView;

    FloatingActionButton fabContacts;
    EditText mContactsSearch;

    ListView mContactsListView;
    ContactsAdapter contactsAdapter;

    ArrayList<String> phoneContacts = new ArrayList<>();
    ArrayList<Users> firebaseUsers = new ArrayList<>();
    ArrayList<Users> commonContacts = new ArrayList<>();
    ArrayList<Users> CopyUserList=new ArrayList<Users>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            //check if user is Online
            Intent startIntent = new Intent(mMainView.getContext(), MainActivity.class);
            startActivity(startIntent);
        }

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

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

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);

            fetchContacts();
        }
        else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            fetchContacts();
        }

        */

        fetchContacts();

        mContactsSearch = mMainView.findViewById(R.id.ContactsSearch);
        mContactsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = mContactsSearch.getText().toString().toLowerCase(Locale.getDefault());

                ArrayList<Users> Temp = new ArrayList<Users>();
                Temp.addAll(CopyUserList);

                commonContacts.clear();

                if (text.length() == 0) {
                    commonContacts.addAll(CopyUserList);
                }
                else {
                    for (Users wp : CopyUserList) {
                        if ((wp.getName().toLowerCase()).contains(text)) {
                            commonContacts.add(wp);
                        }
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabContacts = mMainView.findViewById(R.id.fab_contacts);
        fabContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                //startActivityForResult(intent, CONTACT_PICKER_RESULT);
                startActivity(intent);
            }
        });

        mContactsListView = mMainView.findViewById(R.id.ContactsListView);
        contactsAdapter = new ContactsAdapter(mMainView.getContext(), commonContacts);
        mContactsListView.setAdapter(contactsAdapter);

        mContactsListView.setOnItemClickListener(onItemClick);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                firebaseUsers.clear();

                for (final DataSnapshot user : dataSnapshot.getChildren()) {
                    if(mAuth.getCurrentUser()!=null) {
                        if (!mAuth.getCurrentUser().getUid().equals(user.getKey())) {

                            Users u = user.getValue(Users.class);
                            u.setUserid(user.getKey());

                            if (user.hasChild("online")) {
                                String userOnline = user.child("online").getValue().toString();
                                u.setUserOnlineStatus(userOnline);
                            }
                            firebaseUsers.add(u);
                        }
                    }
                }

                fetchCommonContacts();
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public RequestsFragment() {}

    @Override
    public void onResume() {
        if (mAuth.getCurrentUser() == null) {
            //check if user is Online
            Intent startIntent = new Intent(mMainView.getContext(), MainActivity.class);
            startActivity(startIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);

                fetchContacts();
        }
        else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            fetchContacts();
        }

        fetchCommonContacts();
        contactsAdapter.notifyDataSetChanged();

        super.onResume();
    }

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent profileIntent = new Intent(getContext(), UsersProfileActivity.class);
            profileIntent.putExtra("user_id", commonContacts.get(position).getUserid());
            startActivity(profileIntent);

        }
    };

    public void fetchCommonContacts() {
        commonContacts.clear();

        for (int i = 0; i < firebaseUsers.size(); i++) {

            String s = firebaseUsers.get(i).getPhone();
            String s2 = new String();

            if (s != null && s.contains("+92")) {
                s2 = s.replace("+92", "0");
            }
            if (phoneContacts.contains(firebaseUsers.get(i).getPhone()) || phoneContacts.contains(s2)) {
                commonContacts.add(firebaseUsers.get(i));
            }

        }

        CopyUserList.clear();
        CopyUserList.addAll(commonContacts);
    }

    public void fetchContacts() {

            phoneContacts.clear();

            ContentResolver contentResolver = mMainView.getContext().getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                    if (hasPhoneNumber > 0) {
                        Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                new String[]{id},
                                null);

                        while (cursor2.moveToNext()) {
                            String s = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String s2 = new String();
                            for (int i = 0; i < s.length(); i++) {
                                if (i == 0 && s.charAt(i) == '+')
                                    s2 = "+";
                                if (s.charAt(i) <= '9' && s.charAt(i) >= '0')
                                    s2 = s2.concat("" + s.charAt(i));
                            }
                            if (s2.length() > 0)
                                phoneContacts.add(s2);
                        }
                        cursor2.close();

                    }
                }
            }
            cursor.close();


    }
/*
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                fetchContacts();
            } else {
                Toast.makeText(mMainView.getContext(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

*/
}
