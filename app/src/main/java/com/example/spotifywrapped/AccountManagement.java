package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.spotifywrapped.databinding.ActivityAccountManagementBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountManagement extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityAccountManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        Button signOutBtn = (Button) findViewById(R.id.sign_out);
        signOutBtn.setOnClickListener((v) -> {
            signOut();
        });


        Button deleteBtn = (Button) findViewById(R.id.delete_account);
        deleteBtn.setOnClickListener((v) -> {
            deleteAccount();
        });

        Button updateProfileBtn = (Button) findViewById(R.id.update_profile);
        updateProfileBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(AccountManagement.this, UpdateProfileActivity.class);
            startActivity(intent);
        });

        Button homeBtn = (Button) findViewById(R.id.btnHome);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountManagement.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(Task<Void> task) {

//                        FirebaseApp.initializeApp(AccountManagement.this);
//                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                        if (currentUser == null) {
//                            Intent intent = new Intent(AccountManagement.this, AuthActivity.class);
//                            startActivity(intent);
//                        }
//                        currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        Intent intent = new Intent(AccountManagement.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                });
    }

    public void deleteAccount() {
         AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...


                        FirebaseApp.initializeApp(AccountManagement.this);
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser == null) {
                            Intent intent = new Intent(AccountManagement.this, AuthActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

}
