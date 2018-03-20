package com.mythcon.savr.ngelih;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mythcon.savr.ngelih.Common.Common;
import com.mythcon.savr.ngelih.Model.User;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnSignUp;
    TextView textSlogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.btn_signin);
        btnSignUp = (Button) findViewById(R.id.btn_signup);

        textSlogan = (TextView) findViewById(R.id.textSlogan);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        textSlogan.setTypeface(typeface);

        //init Paper
        Paper.init(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(MainActivity.this,SignUp.class);
                startActivity(signup);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signin = new Intent(MainActivity.this,SignIn.class);
                startActivity(signin);
            }
        });

        //Check Remember
        String user = Paper.book().read(Common.USER_KEY);
        String pass = Paper.book().read(Common.PASSWORD_KEY);
        if (user != null && pass != null) {
            if (!user.isEmpty() && !pass.isEmpty()) {
                login(user, pass);
            }
        }
    }

    private void login(final String phone, final String pass) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if (Common.isConnectedToInternet(getBaseContext())){

            //  init Firebase
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(phone).exists()) {
                        progressDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);

                        if (user.getPass().equals(pass))
                        {
                            Intent toHOme = new Intent(MainActivity.this,Home.class);
                            Common.currentUser = user;
                            startActivity(toHOme);
                            finish();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Wrong password !!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User not exist !!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(MainActivity.this, "Please check you internet connection !!", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
