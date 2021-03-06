package com.mainaliayush2007.apiwithfileupload;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageAPI {
    @Multipart
    @POST("3/upload")
    Call<ResponseBody> upload(
            @Header("Authorization") String clientId,
            @Part() MultipartBody.Part file
    );


}
