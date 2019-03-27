package test.example.com.mychatapp;


import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {


    private Button mRegBtn;
   // private Button mLoginBtn;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = (Button) findViewById(R.id.start_reg_btn);
    //    mLoginBtn = (Button) findViewById(R.id.start_login_btn);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        Intent intent = new Intent(StartActivity.this, PhoneAuthentication_Activity.class);
                        startActivity(intent);

                // Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
               // startActivity(reg_intent);
            }
        });

		/*
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login_intent);

            }
        });
*/
    }

    @Override
    protected void onStart() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);

             if(checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                 super.onStart();
             }
             else
             {
                 requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                 this.onStart();
             }
        }

        else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            super.onStart();
        }
        else
            this.onStart();

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(StartActivity.this, PhoneAuthentication_Activity.class);
                startActivity(intent);

            } else {
                Toast.makeText(StartActivity.this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
