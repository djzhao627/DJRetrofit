package cn.djzhao.retrofit.test;

import cn.djzhao.retrofit.annotation.GET;
import cn.djzhao.retrofit.annotation.Query;
import okhttp3.Call;

/**
 * 百度相关的接口服务
 */
public interface BadiduService {

    /**
     * 根据关键字搜索
     *
     * @param keyword
     * @return
     */
    @GET("s")
    Call query(@Query("wd") String keyword);
}
