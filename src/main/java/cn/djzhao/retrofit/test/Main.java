package cn.djzhao.retrofit.test;

import cn.djzhao.retrofit.DJRetrofit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        DJRetrofit.Builder builder = new DJRetrofit.Builder();

        DJRetrofit djRetrofit = builder.baseUrl("https://www.baidu.com/")
                .build();

        BadiduService badiduService = djRetrofit.create(BadiduService.class);

        Call call = badiduService.query("retrofit");

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println(response.body().string());
                } else {
                    System.out.println(response.message());
                }
            }
        });
    }
}
