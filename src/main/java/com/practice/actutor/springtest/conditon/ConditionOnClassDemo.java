package com.practice.actutor.springtest.conditon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConditionOnClassDemo {
        public static void main(String[] args) throws IOException {
            String url = "https://carbase.shumaidata.com/carbaseinfo";
            String appCode = "你的AppCode";

            Map<String, String> params = new HashMap<>();
            params.put("carno", "车牌号");
            String result = get(appCode, url, params);
            System.out.println(result);
        }

        /**
         * 用到的HTTP工具包：okhttp 3.13.1
         * <dependency>
         * <groupId>com.squareup.okhttp3</groupId>
         * <artifactId>okhttp</artifactId>
         * <version>3.13.1</version>
         * </dependency>
         */
        public static String get(String appCode, String url, Map<String, String> params) throws IOException {
            url = url + buildRequestUrl(params);
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).addHeader("Authorization", "APPCODE " + appCode).build();
            Response response = client.newCall(request).execute();
            System.out.println("返回状态码" + response.code() + ",message:" + response.message());
            String result = response.body().string();
            return result;
        }

        public static String buildRequestUrl(Map<String, String> params) {
            StringBuilder url = new StringBuilder("?");
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                url.append(key).append("=").append(params.get(key)).append("&");
            }
            return url.toString().substring(0, url.length() - 1);
        }
}
