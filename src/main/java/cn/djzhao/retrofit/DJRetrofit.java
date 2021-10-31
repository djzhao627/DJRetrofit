package cn.djzhao.retrofit;

import cn.djzhao.retrofit.test.BadiduService;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DJRetrofit {

    // call的创建工厂，实质上就是OkHttpClient
    final Call.Factory callFactory;
    final HttpUrl baseUrl;

    private final Map<Method, ServiceMethod> serviceMethodCache = new ConcurrentHashMap<>();

    private DJRetrofit(Call.Factory callFactory, HttpUrl baseUrl) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
    }

    /**
     * 根据接口创建动态代理对象
     *
     * @param service
     */
    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 如果是调用的父类方法(Object)则直接执行
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                // 通过method来生成对应的ServiceMethod
                ServiceMethod serviceMethod = loadServiceMethod(method);
                return serviceMethod.invoke(args);
            }
        });
    }

    private ServiceMethod loadServiceMethod(Method method) {
        // 查看是否已经缓存当前方法
        ServiceMethod serviceMethod = serviceMethodCache.get(method);
        if (serviceMethod == null) {
            synchronized (serviceMethodCache) {
                if (serviceMethod == null) {
                    serviceMethod = new ServiceMethod.Builder(this, method).build();
                }
            }
        }
        return serviceMethod;
    }


    public static final class Builder {

        private HttpUrl baseUrl;
        private Call.Factory callFactory;

        /**
         * 设置baseurl
         * @param url
         * @return
         */
        public Builder baseUrl(String url) {
            this.baseUrl = HttpUrl.get(url);
            return this;
        }

        /**
         * 设置call工厂：就是OkHttpClient
         * @param callFactory
         * @return
         */
        public Builder callFactory(Call.Factory callFactory) {
            this.callFactory = callFactory;
            return this;
        }

        /**
         * 创建DJRetrofit
         * @return
         */
        public DJRetrofit build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Must call baseUrl() before build()");
            }
            if (callFactory == null) {
                callFactory = (Call.Factory) new OkHttpClient();
            }
            return new DJRetrofit(callFactory, baseUrl);
        }
    }
}
