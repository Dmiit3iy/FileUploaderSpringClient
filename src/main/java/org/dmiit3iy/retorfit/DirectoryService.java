package org.dmiit3iy.retorfit;


import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.model.UserFile;
import retrofit2.Call;
import retrofit2.http.GET;

import java.io.File;
import java.util.List;

public interface DirectoryService {
    @GET("directory/all/")
    Call<ResponseResult<List<File>>> get();

    @GET("directory/")
    Call<ResponseResult<File>> getRoot();
}
