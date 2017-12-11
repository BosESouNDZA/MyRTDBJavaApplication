package com.example.bosesound.myrtdbjavaapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private SignInButton signInButton;
    private EditText etMessage;
    private EditText etUser;
    private DatabaseReference mRootRef;
    private String uid;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 45;
    private FirebaseAuth mAuth;
    private TextView tv_message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }
    private void initView(){
        // To register click event to view
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null)
                    signOut();
                else
                    signIn();
                break;
            // ...
        }
    }
    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        mGoogleSignInClient.revokeAccess();
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Sign in");
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("test", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("test", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            uid = user.getUid();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("test", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Sign out");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("test", "Google sign in failed", e);
                // ...
            }
        }
    }

    public void saveRecord(View view) {
        etMessage = findViewById(R.id.et_message);
        etUser = findViewById(R.id.et_user);
        String stringMessage = etMessage.getText().toString();
        String stringUser    = etUser.getText().toString();
        if (stringMessage.equals("") || stringUser.equals("")) {
            return;
        }
        DatabaseReference mUserRef = mRootRef
                .child("records").child(uid).child("name");
        DatabaseReference mMessageRef = mRootRef
                .child("records").child(uid).child("quote");
        mUserRef.setValue(stringUser);
        mMessageRef.setValue(stringMessage);
    }

    public void fetchQuote(View view) {
        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String all = dataSnapshot.child("records").getValue().toString();
                tv_message = findViewById(R.id.tv_message);
                tv_message.setText(all);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
