package android.demo;

import android.content.Context;
import android.content.Intent;
import android.demo.network.APIClient;
import android.demo.network.ServiceAPI;
import android.util.Log;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.messages.activities.MessageListActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
import static org.chat21.android.utils.DebugConstants.DEBUG_CONTACTS_SYNC;

public class Utils {

    public void openDirectNotification(Context context, String senderId, String senderFullName) {

        Intent intent = new Intent(context, MessageListActivity.class);
//        intent.setAction(Intent.ACTION_MAIN);
        //intent.addCategory(Intent.CATEGORY_LAUNCHER);
       // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, new ChatUser(senderId, senderFullName));
        intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);
        context.startActivity(intent);
    }

    public void getRestrictedContactsIDs(String token){

        ServiceAPI apiService =
                APIClient.getClient().create(ServiceAPI.class);

        Call<List<String>> call = apiService.GetContactsIdsAPI(token);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> iDs = response.body();
                if (iDs != null) {
                    ChatManager.getInstance().setRestrictedUsersIds(iDs);
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.v(DEBUG_CONTACTS_SYNC, "  GetContacsAPI ERROR");
            }
        });
    }
}
