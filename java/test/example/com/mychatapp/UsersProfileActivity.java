package test.example.com.mychatapp;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UsersProfileActivity extends AppCompatActivity {


    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfilePhone,mProfileOnlineStatus;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mUsersDatabase;

    ListView sharedContent;
    private  DatabaseReference mMediaSharedRefernce;
    private final ArrayList<Messages> messagesList = new ArrayList<>();
    ContentshareAdapter Adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);


        Toolbar toolbar = findViewById(R.id.profile_user_toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });


        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.keepSynced(true);


        mProfileImage = (ImageView) findViewById(R.id.profile_user_image);
        mProfileName = (TextView) findViewById(R.id.profile_user_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_user_status);
        mProfilePhone = findViewById(R.id.profile_user_phone);
        mProfileOnlineStatus = findViewById(R.id.profile_user_OnlineStatus);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString().toUpperCase();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String phone =dataSnapshot.child("phone").getValue().toString();
                String online=dataSnapshot.child("online").getValue().toString();


                if (online.equals("true"))
                {
                    mProfileOnlineStatus.setText("Online");
                }
                else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mProfileOnlineStatus.setText(lastSeenTime);
                }

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                mProfilePhone.setText(phone);


                Picasso.with(UsersProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError() {
                       // Picasso.with(UsersProfileActivity.this).load(image).into(mProfileImage);
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProgressDialog.dismiss();


        //media shared

        sharedContent=findViewById(R.id.mediaShared);
        final String currentUser=FirebaseAuth.getInstance().getCurrentUser().getUid();
        mMediaSharedRefernce = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUser).child(user_id);
        final String uid=user_id;
        mMediaSharedRefernce.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                messagesList.clear();
                for (DataSnapshot m:dataSnapshot.getChildren()) {


                    Messages message = m.getValue(Messages.class);

                    if (message.getFrom().equals(uid)) {
                        messagesList.add(message);
                        Adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        this.Adapter=new ContentshareAdapter(UsersProfileActivity.this,(ArrayList<Messages>) messagesList);
        sharedContent.setAdapter(Adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            finish();
        }
        return true;
    }

}