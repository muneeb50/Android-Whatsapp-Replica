package test.example.com.mychatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ContentshareAdapter extends ArrayAdapter<Messages> {


    private List<Messages> mMessageList;


    public ContentshareAdapter( Context context, ArrayList<Messages> mMessageList) {
        super(context, 0,mMessageList);

        this.mMessageList=mMessageList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_content_share, parent, false);
        }
       final  ImageView    messageImageView=(ImageView)convertView.findViewById(R.id.mediaShared_Message_image);

        final Messages c = mMessageList.get(position);


        if("Image".equals(c.getType())) {

            messageImageView.setVisibility(View.VISIBLE);

            Picasso.with(getContext()).load(c.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(messageImageView, new Callback() {
                @Override
                public void onSuccess() {}

                @Override
                public void onError() {
                    Picasso.with(getContext()).load(c.getMessage()).into(messageImageView);
                }
            });
        }

        return convertView;

    }
}
