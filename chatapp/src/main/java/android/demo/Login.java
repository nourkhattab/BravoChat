package android.demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.authentication.ChatAuthentication;
import org.chat21.android.core.authentication.task.RefreshFirebaseInstanceIdTask;
import org.chat21.android.core.exception.ChatFieldNotFoundException;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.utils.StringUtils;

import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

public class Login {

    private static final String TAG = "ChatLoginActivity";
    private int retryCount = 0;
    private FirebaseAuth mAuth;

    public void runDispatch(Context context) {

        mAuth = FirebaseAuth.getInstance();
        Log.d(DEBUG_LOGIN, "ChatSplashActivity.runDispatch");
        // If current user has already logged in launch the target activity,
        // else launch the login activity
        ChatAuthentication.getInstance().setTenant(ChatManager.Configuration.appId);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (context != null) {
                Log.d(DEBUG_LOGIN, "ChatSplashActivity.runDispatch: user is not logged in. Goto  ChatLoginActivity");
                SharedPreferences sharedpreferences = context.getSharedPreferences("CHAT_PRES", Context.MODE_PRIVATE);
                String token = sharedpreferences.getString("token", null); // getting String
                signIn(token, context);//, password, context);
            }
        }
    }

     private void signIn(final String token, final Context context){
//        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // [START sign_in_with_token]
        mAuth.signInWithCustomToken(token)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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

                                    ChatManager.start(context, mChatConfiguration, loggedUser);
//                                    ChatManager.getInstance().getMyPresenceHandler().connect();
                                    Log.i(TAG, "chat has been initialized with success");

                                    //get restricted users IDs to the logged in user
                                    SharedPreferences sharedpreferences = context.getSharedPreferences("CHAT_PRES", Context.MODE_PRIVATE);
                                    String token = sharedpreferences.getString("authenticationHeader", null); // getting String
                                    if(token != null){
                                        ChatManager.getInstance().setAuthorizationToken(token);
                                        Utils utils = new Utils();
                                        utils.getRestrictedContactsIDs(token);
                                    }

//                                    // get device token
//                                    new RefreshFirebaseInstanceIdTask().execute();
//                                    new RefreshFirebaseInstanceIdTask().generateInstance();
                                    RefreshFirebaseInstanceIdTask refreshFirebaseInstanceIdTask = new RefreshFirebaseInstanceIdTask();
                                    refreshFirebaseInstanceIdTask.generateInstance();

                                    ChatUI.getInstance().setContext(context);
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
                                                Intent intent = new Intent(context.getApplicationContext(),
                                                        ContactListActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                                                context.startActivity(intent);
                                            }
                                        }
                                    });
                                    Log.i(TAG, "ChatUI has been initialized with success");
                                    Toast.makeText(context,"ChatUI has been initialized with success", Toast.LENGTH_LONG ).show();
                                }

                                @Override
                                public void onUserRetrievedError(Exception e) {
                                    Log.d(TAG, "ChatLoginActivity.signInWithEmail.onUserRetrievedError: " + e.toString());
                                }
                            });

                        } else {
                            retryCount ++;
                            if(retryCount <= 2)
                                signIn(token, context);
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(context, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
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
