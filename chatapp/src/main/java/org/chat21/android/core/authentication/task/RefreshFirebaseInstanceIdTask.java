package org.chat21.android.core.authentication.task;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.chat21.android.core.ChatManager;
import org.chat21.android.utils.ChatUtils;
import org.chat21.android.utils.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by andrealeo
 */
public class RefreshFirebaseInstanceIdTask extends AsyncTask<Object, Object, Void> {
    private static final String TAG_TOKEN = "TAG_TOKEN";

    public RefreshFirebaseInstanceIdTask() {
        Log.d(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask");
    }

    public void generateInstance() {

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh:" +
                " called with instanceId: " + token);
        Log.i(TAG_TOKEN, "SaveFirebaseInstanceIdService: token == " + token);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String appId = ChatManager.Configuration.appId;

        if (firebaseUser != null && StringUtils.isValid(appId)) {

            DatabaseReference root;
            if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
                root = FirebaseDatabase.getInstance()
                        .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl);
            } else {
                root = FirebaseDatabase.getInstance().getReference();
            }

            DatabaseReference firebaseUsersPath = root
                    .child("apps/" + ChatManager.Configuration.appId +
                            "/users/" + firebaseUser.getUid() + "/instances/" + token);

            Map<String, Object> device = new HashMap<>();
            device.put("device_model", ChatUtils.getDeviceModel());
            device.put("platform", "Android");
            device.put("platform_version", ChatUtils.getSystemVersion());
            device.put("language", "En");

            firebaseUsersPath.setValue(device); // placeholder value

            Log.i(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh: " +
                    "saved with token: " + token +
                    ", appId: " + appId + ", firebaseUsersPath: " + firebaseUsersPath);
        } else {
            Log.i(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh:" +
                    "user is null. token == " + token + ", appId == " + appId);
        }
    }

    @Override
    protected Void doInBackground(Object... params) {
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
            Log.i(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: instanceId deleted with success.");

//             Now manually call onTokenRefresh()
            Log.d(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: Getting new token");
//            String token = FirebaseInstanceId.getInstance().getToken();
//            Log.i(TAG_TOKEN, "RefreshFirebaseInstanceIdTask: token == " + token);

        } catch (IOException e) {
            Log.e(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: deleteInstanceIdCatch: " + e.getMessage());
        }

        return null;
    }
}