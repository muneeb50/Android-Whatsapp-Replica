package test.example.com.mychatapp;

import android.app.Activity;
import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;
    private String mChatUser;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;


    private final List<Messages> messagesList = new ArrayList<>();

    ////sending picture
    private StorageReference mStorageReference;
    private static final int Gallery_Pic = 1;

    //camera
    boolean safetouse1 = false;
    boolean safetouse2 = false;
    private static final int CAMERA_REQUEST = 1888;
    private File imageFile;
    //private String IDforImage;
    private static int IDforImage = 0;

    private DatabaseReference mnotificationReference;
    DatabaseReference mSyncoffline;
	
    ListView listView;
    myMassageAdapter mMassageAdapter;

    //share button
    ImageView mShareBtn;
    ImageView mDeleteBtn;
    ImageView mCopyBtn;
    ImageView mForwardBtn;


    ClipData clipData;
    String copyText;

    ArrayList<Integer> mSelectedlist=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");


        String userName = getIntent().getStringExtra("user_name");



        //notifications
        mnotificationReference=FirebaseDatabase.getInstance().getReference().child("notifications");


        mSyncoffline=FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId).child(mChatUser);
        mSyncoffline.keepSynced(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // Custom Action bar Items

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);



        mShareBtn=findViewById(R.id.custom_bar_share_btn);
        mDeleteBtn=findViewById(R.id.custom_bar_delete_btn);
        mCopyBtn=findViewById(R.id.custom_bar_copy_btn);
        mForwardBtn=findViewById(R.id.custom_bar_forward_btn);


        mShareBtn.setVisibility(View.INVISIBLE);
        mDeleteBtn.setVisibility(View.INVISIBLE);
        mCopyBtn.setVisibility(View.INVISIBLE);
        mForwardBtn.setVisibility(View.INVISIBLE);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);


        listView=findViewById(R.id.messages_list);
        mMassageAdapter=new myMassageAdapter(ChatActivity.this,(ArrayList<Messages>) messagesList);
        listView.setAdapter(mMassageAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadMessages();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(mSelectedlist.size()<=0 || !mSelectedlist.contains(position)) {

                    mSelectedlist.add(position);

                    if(mSelectedlist.size()>1)
                    {
                        mForwardBtn.setVisibility(View.VISIBLE);
                        mDeleteBtn.setVisibility(View.VISIBLE);

                        mShareBtn.setVisibility(View.INVISIBLE);
                        mCopyBtn.setVisibility(View.INVISIBLE);
                    }
                    else if("text".equals(messagesList.get(position).getType()))
                    {
                        //copy Msg String
                        copyText = messagesList.get(position).getMessage();

                        mForwardBtn.setVisibility(View.VISIBLE);
                        mDeleteBtn.setVisibility(View.VISIBLE);
                        mCopyBtn.setVisibility(View.VISIBLE);

                        mShareBtn.setVisibility(View.INVISIBLE);
                    }
                    else if("Image".equals(messagesList.get(position).getType()))
                    {
                        mForwardBtn.setVisibility(View.VISIBLE);
                        mDeleteBtn.setVisibility(View.VISIBLE);
                        mShareBtn.setVisibility(View.VISIBLE);

                        mCopyBtn.setVisibility(View.INVISIBLE);
                    }

                }
                else
                {
                    if(mSelectedlist.size()>0 && mSelectedlist.contains(position)) {

                        mSelectedlist.remove((Object)position);

                       if(mSelectedlist.size()==0)
                        {
                            mForwardBtn.setVisibility(View.INVISIBLE);
                            mDeleteBtn.setVisibility(View.INVISIBLE);
                            mShareBtn.setVisibility(View.INVISIBLE);
                            mCopyBtn.setVisibility(View.INVISIBLE);
                        }
                        else if(mSelectedlist.size()==1)
                        {
                            if(messagesList.get(0).getType().equals("text"))
                            {
                                mForwardBtn.setVisibility(View.VISIBLE);
                                mDeleteBtn.setVisibility(View.VISIBLE);
                                mShareBtn.setVisibility(View.INVISIBLE);
                                mCopyBtn.setVisibility(View.VISIBLE);
                            }
                            else if(messagesList.get(0).getType().equals("Image"))
                            {
                                mForwardBtn.setVisibility(View.VISIBLE);
                                mDeleteBtn.setVisibility(View.VISIBLE);
                                mShareBtn.setVisibility(View.VISIBLE);
                                mCopyBtn.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                return true;
            }

        });


        mTitleView.setText(userName);


        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                if(online.equals("true")) {

                    mLastSeenView.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(mChatMessageView.getText().toString(),"text");
            }
        });


        //sending picture
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {        // send image button
            @Override
            public void onClick(View view) {

                /*
                Intent GalleryIntent = new Intent();
                GalleryIntent.setType("image/*");
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(GalleryIntent, "Select Image"), Gallery_Pic);
                */

                CharSequence options[] = new CharSequence[]{"Open Camera", "Open Gallery"};

                final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

                builder.setTitle("Select Option to Take Photo");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Click Event
                        if(i == 0){

                            checkCameraPermission();

                        }

                        if(i == 1){

                            Intent GalleryIntent = new Intent();
                            GalleryIntent.setType("image/*");
                            GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(GalleryIntent, "Select Image"), Gallery_Pic);

                        }

                    }
                });

                builder.show();


            }
        });


        forwardMsgsRecieved();


    }

    public void forwardMsgsRecieved()
    {
        if(getIntent().hasExtra("forwardList")) {

            ArrayList<String> forwardlist=new ArrayList<>();
            ArrayList<String> forwardtype=new ArrayList<>();

            forwardlist = getIntent().getStringArrayListExtra("forwardList");
            forwardtype = getIntent().getStringArrayListExtra("forwardType");

            for (int i=0;i<forwardlist.size();i++){
                sendMessage(forwardlist.get(i),forwardtype.get(i));
            }

        }

    }

    public void btnClick(View v)
    {
        if(v.getId()==mTitleView.getId())
        {
            Intent profileIntent = new Intent(ChatActivity.this, UsersProfileActivity.class);
            profileIntent.putExtra("user_id", mChatUser);
            v.getContext().startActivity(profileIntent);
        }

        if(v.getId()==mProfileImage.getId())
        {
            Intent profileIntent = new Intent(ChatActivity.this, UsersProfileActivity.class);
            profileIntent.putExtra("user_id", mChatUser);
            v.getContext().startActivity(profileIntent);
        }

        if(v.getId()==mShareBtn.getId())
        {
            mShareBtn.setVisibility(View.INVISIBLE);
            mDeleteBtn.setVisibility(View.INVISIBLE);
            mCopyBtn.setVisibility(View.INVISIBLE);
            mForwardBtn.setVisibility(View.INVISIBLE);

            shareItem(messagesList.get(mSelectedlist.get(0)).getMessage());

            mSelectedlist.remove(0);
            Toast.makeText(ChatActivity.this,"Shared",Toast.LENGTH_LONG).show();
        }

        if(v.getId()==mDeleteBtn.getId())
        {
            mShareBtn.setVisibility(View.INVISIBLE);
            mDeleteBtn.setVisibility(View.INVISIBLE);
            mCopyBtn.setVisibility(View.INVISIBLE);
            mForwardBtn.setVisibility(View.INVISIBLE);

            DatabaseReference msgRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);


            while(mSelectedlist.size()>0) {

                int lastindex=mSelectedlist.size()-1;

                String msgID = messagesList.get(mSelectedlist.get(lastindex)).getId();
                msgRef.child(msgID).removeValue();

                messagesList.remove(messagesList.get(mSelectedlist.get(lastindex)));
                mSelectedlist.remove(lastindex);

            }

            mMassageAdapter.notifyDataSetChanged();


        }


        if(v.getId()==mCopyBtn.getId())
        {
            ClipboardManager cm= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipData=ClipData.newPlainText("text",copyText);
            cm.setPrimaryClip(clipData);

            mSelectedlist.remove(0);
            Toast.makeText(ChatActivity.this,"Copied",Toast.LENGTH_LONG).show();

            mShareBtn.setVisibility(View.INVISIBLE);
            mDeleteBtn.setVisibility(View.INVISIBLE);
            mCopyBtn.setVisibility(View.INVISIBLE);
            mForwardBtn.setVisibility(View.INVISIBLE);
        }

        if(v.getId()==mForwardBtn.getId())
        {
            mShareBtn.setVisibility(View.INVISIBLE);
            mDeleteBtn.setVisibility(View.INVISIBLE);
            mCopyBtn.setVisibility(View.INVISIBLE);
            mForwardBtn.setVisibility(View.INVISIBLE);

            ArrayList<String> forwardlist=new ArrayList<>();
            ArrayList<String> forwardtype=new ArrayList<>();

            for(int i=0;i<mSelectedlist.size();i++) {
                forwardlist.add(messagesList.get(mSelectedlist.get(i)).getMessage());
                forwardtype.add(messagesList.get(mSelectedlist.get(i)).getType());
            }

            Intent intent=new Intent(ChatActivity.this,UsersActivity.class);
            intent.putStringArrayListExtra("forwardList",forwardlist);
            intent.putStringArrayListExtra("forwardType",forwardtype);
            startActivity(intent);

            finish();

        }

    }

    //external image share
    public void shareItem(String url) {
        Picasso.with(getApplicationContext()).load(url).into(new Target() {
            @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                startActivity(Intent.createChooser(i, "Share Image"));
            }
            @Override public void onBitmapFailed(Drawable errorDrawable) { }
            @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
        });
    }

    public Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


    //load from firebase
    private void loadMessages() {

        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                message.setId(dataSnapshot.getKey());//set id

                messagesList.add(message);

                mMassageAdapter.notifyDataSetChanged();
                listView.smoothScrollToPosition(listView.getCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(String message,String mtype) {


		//String message = mChatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();


            // notificaions to database
            Map<String,String> notificationdata=new HashMap<>();
            notificationdata.put("from",mCurrentUserId);
            notificationdata.put("type","message_alert");

            mnotificationReference.child(mChatUser).push().setValue(notificationdata)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });



            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", mtype);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {


                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }

    }


    AdapterView.OnItemClickListener onItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener=new AdapterView.OnItemLongClickListener() {
       @Override
       public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
           return true;
       }
   };

    // picture selection
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
        String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

        final  DatabaseReference currentuser_meesage_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        final DatabaseReference mchatuser_meesage_push=mRootRef.child("messages").child(mChatUser).child(mCurrentUserId);

        final String push_id1 = currentuser_meesage_push.getKey();
        //final String push_id2=mchatuser_meesage_push.getKey();

        if (requestCode == Gallery_Pic && resultCode == RESULT_OK) {     

            Uri imageUri = data.getData();
            Date d=new Date();
            StorageReference filepath = mStorageReference.child("mImageStorage").child("Messageimages").child(push_id1+ d.toString() + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        String downloadurl = task.getResult().getDownloadUrl().toString();
                        Map userMap = new HashMap<>();
                        userMap.put("message", downloadurl);
                        userMap.put("seen", false);
                        userMap.put("type", "Image");
                        userMap.put("time", ServerValue.TIMESTAMP);
                        userMap.put("from", mCurrentUserId);

                        currentuser_meesage_push.push().setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(ChatActivity.this, "Image uploaded Succesfully", Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                        mchatuser_meesage_push.push().setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    //Toast.makeText(ChatActivity.this, "Image2 uploaded Succesfully", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                    }
                    else {
                        Toast.makeText(ChatActivity.this,"Error in uploading image",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // camera
        else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            String imageTempName = "a";
            Date d=new Date();
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            Uri tempUri = getImageUri(getApplicationContext(), imageBitmap, imageTempName);
            StorageReference filepath = mStorageReference.child("mImageStorage").child("Messageimages").child(push_id1+d.toString() + ".jpg");
            filepath.putFile(tempUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        String downloadurl = task.getResult().getDownloadUrl().toString();
                        Map userMap = new HashMap<>();
                        userMap.put("message", downloadurl);
                        userMap.put("seen", false);
                        userMap.put("type", "Image");
                        userMap.put("time", ServerValue.TIMESTAMP);
                        userMap.put("from", mCurrentUserId);

                        currentuser_meesage_push.push().setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(ChatActivity.this,"Image uploaded Succesfully",Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                        mchatuser_meesage_push.push().setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                   // Toast.makeText(ChatActivity.this,"Image2 uploaded Succesfully",Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                    }
                    else {
                        Toast.makeText(ChatActivity.this,"Error in uploading image",Toast.LENGTH_LONG).show();
                    }

                }
            });


        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage, String imageName) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, imageName, null);
        return Uri.parse(path);
    }


    public void checkCameraPermission() {

        //String[] permissionArrays = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        3);
            }
        } else {
            safetouse1 = true;
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        4);
            }
        } else {
            safetouse2 = true;
        }

        if(safetouse1 && safetouse2) {

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        else {
            Toast.makeText(this,"First allow permission and for that press camera button again",Toast.LENGTH_SHORT).show();
        }
    }

    // request permssion
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 3) {
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];

                this.safetouse1 = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission

                    safetouse1 = false;

                }
            }
        }
        if (requestCode == 4) {
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];

                this.safetouse2 = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission

                    safetouse2 = false;

                }
            }


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.search_chat){


        }

        else if (item.getItemId() == R.id.clear_chat)
            {

                Intent intent=new Intent(ChatActivity.this,MainActivity.class);
                startActivity(intent);
                finish();

                DatabaseReference msgRef2=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
                msgRef2.removeValue();
                DatabaseReference msgRef3=mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser);
                msgRef3.removeValue();
            }


        return true;
    }


}
