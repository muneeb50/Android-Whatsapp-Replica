package test.example.com.mychatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class myMassageAdapter extends ArrayAdapter<Messages> {

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;

    public myMassageAdapter( Context context, ArrayList<Messages> mMessageList) {
        super(context, 0,mMessageList);

        this.mMessageList=mMessageList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_single_layout, parent, false);
        }

        TextView messageText=convertView.findViewById(R.id.message_text_layout);
        final CircleImageView profileImage=convertView.findViewById(R.id.message_profile_layout);
        final TextView displayName=convertView.findViewById(R.id.name_text_layout);
        TextView messageTime=convertView.findViewById(R.id.time_text_layout);

        final ImageView messageImageView=convertView.findViewById(R.id.message_image_layout);
        final Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                displayName.setText(name);

                Picasso.with(profileImage.getContext()).load(image).placeholder(R.drawable.default_avatar).into(profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if("text".equals(c.getType())) {

            messageImageView.setVisibility(View.INVISIBLE);
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(c.getMessage());

        }
        else
        {
            messageText.setVisibility(View.INVISIBLE);
            messageImageView.setVisibility(View.VISIBLE);
            //Picasso.with(getContext()).load(c.getMessage()).into(messageImageView);       // loading image
            Picasso.with(getContext()).load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(messageImageView, new Callback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onError() {
                    Picasso.with(getContext()).load(c.getMessage()).into(messageImageView);
                }
            });
        }

        ////Time
        GetTimeAgo getTimeAgo = new GetTimeAgo();
        String SentTime = getTimeAgo.getTimeAgo(c.getTime(), messageTime.getContext());
        messageTime.setText(String.valueOf(SentTime));

        return convertView;

    }
}
