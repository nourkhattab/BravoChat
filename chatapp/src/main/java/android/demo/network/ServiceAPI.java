package android.demo.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;

public interface ServiceAPI {
    @GET("services/api/Chatting/GetContactsIds")
    @Headers({"Content-Type: application/json"})
    Call<List<String>> GetContactsIdsAPI(@Header("Authorization") String token);
}
