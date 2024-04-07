package org.dmiit3iy.retorfit;


import org.dmiit3iy.dto.ResponseResult;
import retrofit2.Call;
import retrofit2.http.GET;

import java.io.File;

public interface DirectoryService {
    @GET("/directory")
    Call<ResponseResult<File>> get();
}
