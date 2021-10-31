package cn.djzhao.retrofit;

/**
 * 参数处理器
 */
public abstract class ParameterHandler {

    /**
     * 给参数赋值
     *
     * @param serviceMethod
     * @param value
     */
    abstract void apply(ServiceMethod serviceMethod, String value);

    public static final class QueryParameterHandler extends ParameterHandler {

        private String key;

        public QueryParameterHandler(String key) {
            this.key = key;
        }

        @Override
        void apply(ServiceMethod serviceMethod, String value) {
            serviceMethod.addQueryParameter(key, value);
        }
    }

    public static final class FieldParameterHandler extends ParameterHandler {

        private String key;

        public FieldParameterHandler(String key) {
            this.key = key;
        }

        @Override
        void apply(ServiceMethod serviceMethod, String value) {
            serviceMethod.addFieldParameter(key, value);
        }
    }
}
