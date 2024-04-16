package org.dmiit3iy.retorfit;

import okhttp3.MultipartBody;
import org.dmiit3iy.dto.ResponseResult;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {
    @Multipart
    @POST(".")
    Call<ResponseResult<String>> upload(@Part MultipartBody.Part file);
}
