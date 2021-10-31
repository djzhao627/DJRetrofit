package cn.djzhao.retrofit;

import cn.djzhao.retrofit.annotation.Field;
import cn.djzhao.retrofit.annotation.GET;
import cn.djzhao.retrofit.annotation.POST;
import cn.djzhao.retrofit.annotation.Query;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 通过Method转换未来的ServiceMethod
 */
public class ServiceMethod {

    /**
     * 主机url
     */
    private final HttpUrl baseUrl;
    /**
     * 相对路径
     */
    private final String relativeUrl;
    /**
     * 请求构造器
     */
    private final Call.Factory callFactory;
    /**
     * 请求方式
     */
    private final String requestMethod;
    /**
     * 参数处理器
     */
    private final ParameterHandler[] parameterHandlers;
    /**
     * url构造器
     */
    private final HttpUrl.Builder urlBuilder;
    /**
     * 请求体构造器
     */
    private FormBody.Builder formBuilder;

    public ServiceMethod(Builder builder) {
        this.baseUrl = builder.djRetrofit.baseUrl;
        this.relativeUrl = builder.relativeUrl;
        this.callFactory = builder.djRetrofit.callFactory;
        this.requestMethod = builder.requestMethod;
        this.parameterHandlers = builder.parameterHandlers;
        urlBuilder = baseUrl.newBuilder(relativeUrl);
        if (builder.hasBody) {
            this.formBuilder = new FormBody.Builder();
        }
    }

    /**
     * 执行当前方法，获取对应的Call
     *
     * @param args
     * @return
     */
    public Object invoke(Object[] args) {
        // 处理请求参数
        for (int i = 0; i < parameterHandlers.length; i++) {
            ParameterHandler parameterHandler = parameterHandlers[i];
            parameterHandler.apply(this, args[i].toString());
        }
        // 最终请求url
        HttpUrl url = urlBuilder.build();
        // 创建请求体
        FormBody formBody = null;
        if (formBuilder != null) {
            formBody = formBuilder.build();
        }
        // 生成请求
        Request request = new Request.Builder()
                .url(url)
                .method(requestMethod, formBody)
                .build();
        // 通过callFactory创建一个新的call
        return callFactory.newCall(request);
    }

    /**
     * 添加GET参数
     *
     * @param key
     * @param value
     */
    public void addQueryParameter(String key, String value) {
        urlBuilder.addQueryParameter(key, value);
    }

    /**
     * 添加form表单参数
     *
     * @param key
     * @param value
     */
    public void addFieldParameter(String key, String value) {
        formBuilder.add(key, value);
    }

    public final static class Builder {

        private final DJRetrofit djRetrofit;
        /**
         * 方法上的所有注解
         */
        private final Annotation[] methodAnnotations;
        /**
         * 参数上的所有注解(一个参数可能有多个注解，所以二维数组)
         */
        private final Annotation[][] parameterAnnotations;
        /**
         * 请求方式
         */
        private String requestMethod;
        /**
         * 是否有请求体
         */
        private boolean hasBody;
        /**
         * 请求的相对路径
         */
        private String relativeUrl;
        private ParameterHandler[] parameterHandlers;

        public Builder(DJRetrofit djRetrofit, Method method) {
            this.djRetrofit = djRetrofit;
            methodAnnotations = method.getAnnotations();
            parameterAnnotations = method.getParameterAnnotations();
        }

        /**
         * 根据注解来构建当前的ServiceMethod
         *
         * @return
         */
        public ServiceMethod build() {
            // 处理方法上的注解
            for (Annotation methodAnnotation : methodAnnotations) {
                // TODO: 2021/10/31 目前只处理GET和POST两种注解
                if (methodAnnotation instanceof GET) {
                    this.requestMethod = "GET";
                    this.hasBody = false;
                    this.relativeUrl = ((GET) methodAnnotation).value();
                } else if (methodAnnotation instanceof POST) {
                    this.requestMethod = "POST";
                    this.hasBody = true;
                    this.relativeUrl = ((POST) methodAnnotation).value();
                }
            }

            parameterHandlers = new ParameterHandler[parameterAnnotations.length];
            // 处理参数的注解
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                for (Annotation annotation : parameterAnnotation) {
                    if (annotation instanceof Query) {
                        if ("POST".equals(this.requestMethod)) {
                            throw new IllegalStateException("Cannot use Query parameter in POST request");
                        }
                        String key = ((Query) annotation).value();
                        parameterHandlers[i] = new ParameterHandler.QueryParameterHandler(key);
                    } else if (annotation instanceof Field) {
                        if ("GET".equals(this.requestMethod)) {
                            throw new IllegalStateException("Cannot use Field parameter in GET request");
                        }
                        String key = ((Field) annotation).value();
                        parameterHandlers[i] = new ParameterHandler.FieldParameterHandler(key);
                    }
                }
            }
            return new ServiceMethod(this);
        }
    }
}
