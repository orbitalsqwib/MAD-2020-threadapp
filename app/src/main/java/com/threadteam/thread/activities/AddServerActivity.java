package com.threadteam.thread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.models.Server;

public class AddServerActivity extends AppCompatActivity {

    private static final String LogTAG = "ThreadApp: ";

    //FIREBASE
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;

    //DATA STORE
    private String joinServerID;

    //VIEW OBJECTS
    private NestedScrollView BaseAddServerNSV;
    private EditText JoinServerIdEditText;
    private Button JoinServerButton;
    private EditText MakeServerNameEditText;
    private EditText MakeServerDescEditText;
    private Button MakeServerButton;

    private androidx.appcompat.widget.Toolbar TopNavToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addserver);

        // SETUP TOOLBARS
        View topNavView = findViewById(R.id.addServerNavbarInclude);
        TopNavToolbar = topNavView.findViewById(R.id.topNavToolbar);

        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TopNavToolbar.setTitle("Add Server");

        // BIND VIEW OBJECTS
        BaseAddServerNSV = findViewById(R.id.baseAddServerNSV);
        JoinServerIdEditText = findViewById(R.id.joinServerIdEditText);
        JoinServerButton = findViewById(R.id.joinServerButton);
        MakeServerNameEditText = findViewById(R.id.makeServerNameEditText);
        MakeServerDescEditText = findViewById(R.id.makeServerDescEditText);
        MakeServerButton = findViewById(R.id.makeServerButton);

        JoinServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleJoinServer();
            }
        });

        MakeServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMakeServer();
            }
        });

        MakeServerNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MakeServerNameEditText.setText(MakeServerNameEditText.getText().toString().trim());
                }
            }
        });

        MakeServerDescEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MakeServerDescEditText.setText(MakeServerDescEditText.getText().toString().trim());
                }
            }
        });

        //INITIALISE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void handleJoinServer() {

        final String userId = currentUser.getUid();
        joinServerID = JoinServerIdEditText.getText().toString();

        final ValueEventListener testUserNotSubscribed = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    displayError("User is already subscribed to this server!");
                } else {
                    // Subscribe user to server
                    databaseRef.child("users").child(userId)
                            .child("_subscribedServers").child(joinServerID).setValue(true);
                    databaseRef.child("members").child(joinServerID).child(currentUser.getUid()).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        final ValueEventListener testServerExists = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    // Server does not exist
                    displayError("No server exists for this ID!");
                } else {
                    // Test user subscription
                    databaseRef.child("users").child(userId).child("_subscribedServers")
                            .child(joinServerID).addListenerForSingleValueEvent(testUserNotSubscribed);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        ValueEventListener shareCodeLookup = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    // Server Share Code does not exist
                    displayError("Server Share Code is invalid! Check that your friend still has the Share Code page open!");
                } else {
                    // Continue using normal pipeline
                    joinServerID = (String) dataSnapshot.getValue();
                    databaseRef.child("servers").child(joinServerID).addListenerForSingleValueEvent(testServerExists);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        //TestServerExists -> TestUserIsNotSubscribed -> SubscribeUserToServer
        if(joinServerID.length() == 6) {
            // Handle Server Share Code
            databaseRef.child("shares").child(joinServerID).addListenerForSingleValueEvent(shareCodeLookup);
        } else {
            databaseRef.child("servers").child(joinServerID).addListenerForSingleValueEvent(testServerExists);
        }

        finish();
    }

    private void handleMakeServer() {

        String userId = currentUser.getUid();
        String makeServerName = MakeServerNameEditText.getText().toString().trim();
        String makeServerDesc = MakeServerDescEditText.getText().toString().trim();

        if(makeServerName.length() == 0) {
            MakeServerNameEditText.setError("Your server name can't be empty!");
            return;
        }

        Server newServer = new Server(userId, makeServerName, makeServerDesc);

        String newServerId = databaseRef.child("servers").push().getKey();

        if (newServerId == null) {
            Log.v(LogTAG, "Couldn't obtain serverId for new server! Aborting server creation!");
            return;
        }

        databaseRef.child("servers").child(newServerId).setValue(newServer);
        databaseRef.child("users").child(userId).child("_subscribedServers").child(newServerId).setValue(true);
        databaseRef.child("members").child(newServerId).child(currentUser.getUid()).setValue(true);

        finish();
    }

    private void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.v(LogTAG, message);
    }
}
