package in.co.codoc.chatdemo;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by ashik619 on 24-11-2016.
 */
public interface FileUploadService {
    @Multipart
    @POST("file_upload")
    Call<ResponseBody> upload(@Part("expert_id") RequestBody expert_id,
                              @Part("user_id") RequestBody user_id,
                              @Part("report_id") RequestBody report_id,
                             // @Part("about_me") RequestBody about_me,
            @Part MultipartBody.Part file);
}
