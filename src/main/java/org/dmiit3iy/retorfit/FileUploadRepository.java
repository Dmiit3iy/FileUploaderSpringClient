package org.dmiit3iy.retorfit;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lingala.zip4j.ZipFile;
import okhttp3.*;
import org.dmiit3iy.dto.ResponseResult;
import org.dmiit3iy.util.Constants;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileUploadRepository {
    private final ObjectMapper objectMapper;
    private FileUploadService service;

    public FileUploadRepository() {
        objectMapper = new ObjectMapper();
        //objectMapper.registerModule(new JavaTimeModule());
        OkHttpClient client = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL + "file/")
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(client)
                .build();
        this.service = retrofit.create(FileUploadService.class);
    }


    public void uploadFile(ZipFile zipFile) throws IOException {
        RequestBody requestFile =RequestBody.create(MediaType.parse(Files.probeContentType(zipFile.getFile().toPath())),zipFile.getFile());

        MultipartBody.Part body = MultipartBody.Part.createFormData("document",zipFile.getFile().getName(), requestFile);

        Call<ResponseResult<String>> call = service.upload(body);
        ResponseResult<String> res = call.execute().body();

    }

//    public void downloadFile(String filename, long id, long version, String path) throws IOException {
//        Call<ResponseBody> call = this.service.showFile(filename, id, version);
//        ResponseBody body = call.execute().body();
//        String client = path;
//        File file = new File(client);
//        file.mkdirs();
//        try (FileOutputStream outputStream = new FileOutputStream(new File(file, filename))) {
//            outputStream.write(body.bytes());
//        }
//    }


//    public void downloadZip(String s, String path) throws IOException {
//        Call<ResponseBody> call = this.service.getZip();
//        ResponseBody body = call.execute().body();
//        String client = path;
//        File file = new File(client);
//        file.mkdirs();
//        try (FileOutputStream outputStream = new FileOutputStream(new File(file,s+".zip"))) {
//            outputStream.write(body.bytes());
//        }
//    }

}
