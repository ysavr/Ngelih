package com.mythcon.savr.ngelih;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mythcon.savr.ngelih.Common.Common;
import com.mythcon.savr.ngelih.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    EditText edtPass,edtPhone;
    Button btnSignIn;
    CheckBox ckcRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPass = (MaterialEditText) findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText) findViewById(R.id.edtPhone);
        btnSignIn = (Button) findViewById(R.id.btn_signin);
        ckcRemember = (CheckBox) findViewById(R.id.checkRemember);

        Paper.init(this);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())){

                //    <--SAVE USER & PASSWORD----------->
                    if (ckcRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY,edtPhone.getText().toString());
                        Paper.book().write(Common.PASSWORD_KEY,edtPass.getText().toString());
                    }

                //  init Firebase
                    final ProgressDialog progressDialog = new ProgressDialog(SignIn.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                progressDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString());

                                if (user.getPass().equals(edtPass.getText().toString()))
                                {
                                    Intent toHOme = new Intent(SignIn.this,Home.class);
                                    Common.currentUser = user;
                                    startActivity(toHOme);
                                    finish();
                                }
                                else {
                                    Toast.makeText(SignIn.this, "Wrong password !!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(SignIn.this, "User not exist !!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignIn.this, "Please check you internet connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
