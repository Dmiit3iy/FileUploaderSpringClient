package org.dmiit3iy.retorfit;

import okhttp3.MultipartBody;
import org.dmiit3iy.dto.ResponseResult;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface FileUploadService {
    @Multipart
    @POST("file/")
    Call<ResponseResult<String>> upload(@Part MultipartBody.Part document,@Query("path") String path);

    @POST("dir/")
    Call<ResponseResult<String>> create(@Query("path") String path);
}
