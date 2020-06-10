package org.chat21.android.ui.login.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.demo.TabActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.authentication.ChatAuthentication;
import org.chat21.android.core.authentication.task.RefreshFirebaseInstanceIdTask;
import org.chat21.android.core.contacts.listeners.OnContactCreatedCallback;
import org.chat21.android.core.exception.ChatFieldNotFoundException;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;

import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.utils.ChatUtils;
import org.chat21.android.utils.StringUtils;

import java.util.Map;

import chat21.android.demo.R;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by stefanodp91 on 21/12/17.
 */


// sources:
// https://www.bignerdranch.com/blog/splash-screens-the-right-way/
// https://github.com/parse-community/ParseUI-Android/blob/master/ParseUI-Login/src/main/java/com/parse/ui/ParseLoginDispatchActivity.java
// https://bitbucket.org/frontiere21/smart21-android-aste/src/dce43efe4ae649a1516d4b5e397eff4ba77676f9/app/src/main/java/it/smart21/android/aste/activities/DispatchActivity.java?at=default&fileviewer=file-view-default
public class ChatSplashActivity extends AppCompatActivity {

    /**
     * The class to start when the login process has been finished
     *
     * @return the target class
     */
//    protected abstract Class<?> getTargetClass();

//    /**
//     * The intent to launch to perform the login activity
//     *
//     * @return the chat login intent
//     */
    private static final String TAG = "ChatLoginActivity";
    private static final int LOGIN_REQUEST = 0;
    private static final int TARGET_REQUEST = 1;


    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_splash);
        Log.d(DEBUG_LOGIN, "ChatSplashActivity.onCreate");


        ChatAuthentication.getInstance().setTenant(ChatManager.Configuration.appId);
//        ChatAuthentication.getInstance().createAuthListener(onAuthStateChangeListener);

        TextView title = findViewById(R.id.title);
        title.setText(ChatUtils.getApplicationName(this));

        runDispatch();

    }

    @Override
    public void onStart() {
        super.onStart();

//        ChatAuthentication.getInstance().getFirebaseAuth()
//                .addAuthStateListener(ChatAuthentication.getInstance().getAuthListener());

        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onStart: auth state listener attached ");
    }

    @Override
    public void onStop() {
//        ChatAuthentication.getInstance().removeAuthStateListener();
        Log.d(DEBUG_LOGIN, "ChatLoginActivity.onStart: auth state listener detached ");

        super.onStop();
    }

    @Override
    final protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(DEBUG_LOGIN, "ChatSplashActivity.onActivityResult: requestCode == " + requestCode + " resultCode == " + requestCode);
        setResult(resultCode);
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            runDispatch();
        } else {
            finish();
        }
    }

    private void runDispatch() {

        Log.d(DEBUG_LOGIN, "ChatSplashActivity.runDispatch");
        // If current user has already logged in launch the target activity,
        // else launch the login activity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d(DEBUG_LOGIN, "ChatSplashActivity.runDispatch: user is logged in. Goto : " + TabActivity.class.getName());
            Intent targetIntent = new Intent(this, TabActivity.class);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                targetIntent.putExtras(extras);
            }
            startActivityForResult(targetIntent, TARGET_REQUEST);
        } else {
            Log.d(DEBUG_LOGIN, "ChatSplashActivity.runDispatch: user is not logged in. Goto  ChatLoginActivity");
            SharedPreferences sharedpreferences = getSharedPreferences("CHAT_PRES", Context.MODE_PRIVATE);
            String email = sharedpreferences.getString("email", null); // getting String
            String password = sharedpreferences.getString("password", null); // getting Integer
            signIn(email, password);
        }
    }

    private void signIn(String email, String password){


        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            lookUpContactById(user.getUid(), new OnUserLookUpComplete() {
                                @Override
                                public void onUserRetrievedSuccess(IChatUser loggedUser) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedSuccess: loggedUser == " + loggedUser.toString());

                                    ChatManager.Configuration mChatConfiguration =
                                            new ChatManager.Configuration.Builder(ChatManager.Configuration.appId)
                                                    .build();

                                    ChatManager.start(ChatSplashActivity.this, mChatConfiguration, loggedUser);
                                    Log.i(TAG, "chat has been initialized with success");

//                                    // get device token
                                    new RefreshFirebaseInstanceIdTask().execute();

                                    ChatUI.getInstance().setContext(ChatSplashActivity.this);
                                    Log.i(TAG, "ChatUI has been initialized with success");

                                    ChatUI.getInstance().enableGroups(true);

                                    // set on new conversation click listener
                                    // final IChatUser support = new ChatUser("support", "Chat21 Support");
                                    final IChatUser support = null;
                                    ChatUI.getInstance().setOnNewConversationClickListener(new OnNewConversationClickListener() {
                                        @Override
                                        public void onNewConversationClicked() {
                                            if (support != null) {
                                                ChatUI.getInstance().openConversationMessagesActivity(support);
                                            } else {
                                                Intent intent = new Intent(getApplicationContext(),
                                                        ContactListActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                                                startActivity(intent);
                                            }
                                        }
                                    });
                                    Log.i(TAG, "ChatUI has been initialized with success");

//                                    setResult(Activity.RESULT_OK);
                                    Intent targetIntent = new Intent(ChatSplashActivity.this, TabActivity.class);
                                    startActivityForResult(targetIntent, TARGET_REQUEST);
                                    finish();
                                }

                                @Override
                                public void onUserRetrievedError(Exception e) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedError: " + e.toString());
                                }
                            });

                            // enable persistence must be made before any other usage of FirebaseDatabase instance.
                            try {
                                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                            } catch (DatabaseException databaseException) {
                                Log.w(TAG, databaseException.toString());
                            } catch (Exception e) {
                                Log.w(TAG, e.toString());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                            Toast.makeText(ChatSplashActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
//                            setResult(Activity.RESULT_CANCELED);
//                            finish();
//                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void lookUpContactById(String userId, final OnUserLookUpComplete onUserLookUpComplete) {


        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts/" + userId);
        }

        contactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: dataSnapshot == " + dataSnapshot.toString());

                if (dataSnapshot.getValue() != null) {
                    try {
                        IChatUser loggedUser = decodeContactSnapShop(dataSnapshot);
                        Log.d(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: loggedUser == " + loggedUser.toString());
                        onUserLookUpComplete.onUserRetrievedSuccess(loggedUser);
                    } catch (ChatFieldNotFoundException e) {
                        Log.e(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById.onDataChange: " + e.toString());
                        onUserLookUpComplete.onUserRetrievedError(e);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(DEBUG_LOGIN, "ChatLoginActivity.lookUpContactById: " + databaseError.toString());
                onUserLookUpComplete.onUserRetrievedError(databaseError.toException());
            }
        });
    }

    private static IChatUser decodeContactSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(TAG, "decodeContactSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

//        String contactId = dataSnapshot.getKey();

        String uid = (String) map.get("uid");
        if (uid == null) {
            throw new ChatFieldNotFoundException("Required uid field is null for contact id : " + uid);
        }

//        String timestamp = (String) map.get("timestamp");

        String lastname = (String) map.get("lastname");
        String firstname = (String) map.get("firstname");
        String imageurl = (String) map.get("imageurl");
        String email = (String) map.get("email");


        IChatUser contact = new ChatUser();
        contact.setId(uid);
        contact.setEmail(email);
        contact.setFullName(firstname + " " + lastname);
        contact.setProfilePictureUrl(imageurl);

        Log.v(TAG, "decodeContactSnapShop.contact : " + contact);

        return contact;
    }

    private interface OnUserLookUpComplete {
        void onUserRetrievedSuccess(IChatUser loggedUser);

        void onUserRetrievedError(Exception e);
    }
}