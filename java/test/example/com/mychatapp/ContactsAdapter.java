package test.example.com.mychatapp;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;



import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends ArrayAdapter<Users> {
    public ContactsAdapter(Context context, ArrayList<Users> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

         final Users user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.users_single_layout, parent, false);
        }

        TextView userName=convertView.findViewById(R.id.user_single_name);
        userName.setText(user.getName());

        TextView userStatus=convertView.findViewById(R.id.user_single_status);
        userStatus.setText(user.getStatus());

        CircleImageView userImageView = convertView.findViewById(R.id.user_single_image);
        Picasso.with(convertView.getContext()).load(user.getImage()).placeholder(R.drawable.default_avatar).into(userImageView);
/*
        Picasso.with(getContext()).load(user.getImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(userImageView, new Callback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onError() {
               // Picasso.with(getContext()).load(user.getImage()).into(userImageView);
            }
        });
*/

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getContext(), UsersProfileActivity.class);
                profileIntent.putExtra("user_id", user.getUserid());
                v.getContext().startActivity(profileIntent);
            }
        });

        ImageView userOnlineView = (ImageView) convertView.findViewById(R.id.user_single_online_icon);
        TextView userOnlineTimeStatus=convertView.findViewById(R.id.user_single_time_text_layout);

        if(user.getUserOnlineStatus()!=null) {

            if (user.getUserOnlineStatus().equals("true")) {

                userOnlineView.setVisibility(View.VISIBLE);

                userOnlineTimeStatus.setText("Online");

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

                GetTimeAgo getTimeAgo = new GetTimeAgo();
                String SentTime = getTimeAgo.getTimeAgo(Long.parseLong(user.getUserOnlineStatus()),convertView.getContext());

                userOnlineTimeStatus.setText(SentTime);


            }
        }
        return convertView;
    }
}
