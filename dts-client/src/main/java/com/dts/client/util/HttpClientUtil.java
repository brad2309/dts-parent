package com.dts.client.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {
	
	private static Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
    private static CloseableHttpClient httpClient = null;
    private static Integer TIMEOUT = 30000;
    /**
     * 创建HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient createHttpClient(int maxTotal,
                                                       int maxPerRoute, int maxRoute, String hostname, int port) {
        CloseableHttpClient httpClient;
        try {
            ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                    .getSocketFactory();


            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
                    null, new TrustStrategy() {
                        // 信任所有
                        @Override
                        public boolean isTrusted(X509Certificate[] chain,
                                                 String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder
                    .<ConnectionSocketFactory> create().register("http", plainsf)
                    .register("https", sslsf).build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                    registry);
            // 将最大连接数增加
            cm.setMaxTotal(maxTotal);
            // 将每个路由基础的连接增加
            cm.setDefaultMaxPerRoute(maxPerRoute);
            HttpHost httpHost = new HttpHost(hostname, port);
            // 将目标主机的最大连接数增加
            cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

            // 请求重试处理
            HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception,
                                            int executionCount, HttpContext context) {
                    if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {// 超时
                        return false;
                    }
                    if (exception instanceof UnknownHostException) {// 目标服务器不可达
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                        return false;
                    }
                    if (exception instanceof SSLException) {// SSL握手异常
                        return false;
                    }

                    HttpClientContext clientContext = HttpClientContext
                            .adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    // 如果请求是幂等的，就再次尝试
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                }
            };

            httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setRetryHandler(httpRequestRetryHandler).build();
            return httpClient;
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 获取HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient getHttpClient(String url) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        if (httpClient == null) {
            synchronized (HttpClientUtil.class) {
                if (httpClient == null) {
                    httpClient = createHttpClient(1000, 1000, 1000, hostname, port);
                }
            }
        }
        return httpClient;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @author zlj
     * @date 2015年7月31日
     * @modifyBy zlj 2015年7月31日
     *
     * @param url
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return
     */
    public static String get(String url, String param) {
        CloseableHttpClient httpClient = getHttpClient(url);//HttpClients.createDefault();
        String body = null;
        try {
            if (param != null && !"".equals(param)) {
                url = String.format("%s?%s", url, param);
            }
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT).build();
            httpGet.setConfig(requestConfig);
            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    body = EntityUtils.toString(entity,Charset.forName("UTF-8"));
                    EntityUtils.consume(entity);
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return body;

    }

    public static String get(String url, String param, Map<String, String> headers) {
        CloseableHttpClient httpClient = getHttpClient(url);
        String body = null;
        try {
            if (param != null && !"".equals(param)) {
                url = String.format("%s?%s", url, param);
            }
            HttpGet httpGet = new HttpGet(url);
            if (headers != null && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpGet.addHeader(key, headers.get(key));
                }
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT).build();
            httpGet.setConfig(requestConfig);
            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    body = EntityUtils.toString(entity,Charset.forName("UTF-8"));
                    EntityUtils.consume(entity);
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return body;

    }


    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @author zlj
     * @date 2015年7月31日
     * @modifyBy zlj 2015年7月31日
     *
     * @param url
     * @param params key-value形式参数
     * @return
     */
    public static String post(String url, Map<String, String> params) {
        CloseableHttpClient httpClient = getHttpClient(url);//HttpClients.createDefault();
        String body = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT).build();
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new UrlEncodedFormEntity(nvps,"utf-8"));//new UrlEncodedFormEntity(nvps,"utf-8")
            CloseableHttpResponse response = httpClient.execute(httpPost);

            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    body = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                }else{
                    throw new RuntimeException(statusCode+"");
                }

            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
                throw new RuntimeException(e);
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
        return body;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @author zlj
     * @date 2015年7月31日
     * @modifyBy zlj 2015年7月31日
     *
     * @param url
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return
     */
    public static String getHttps(String url, String param) {
        return getHttps(url, param, null);
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @author zlj
     * @date 2015年7月31日
     * @modifyBy zlj 2015年7月31日
     *
     * @param url
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param code 返回结果编码格式
     * @return
     */
    public static String getHttps(String url, String param, String code) {
        //HttpClientUtil.createSSLClientDefault();
        CloseableHttpClient httpClient = getHttpClient(url);
        String body = null;
        try {
            if (param != null && !"".equals(param.trim())) {
                url = String.format("%s?%s", url, param);
            }
            HttpGet httpGet = new HttpGet(url);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT).build();
            httpGet.setConfig(requestConfig);

            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    if (code != null && !"".equals(code)) {
                        body = EntityUtils.toString(entity, code);
                    } else {
                        body = EntityUtils.toString(entity);
                    }
                    EntityUtils.consume(entity);
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return body;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @author zlj
     * @date 2015年7月31日
     * @modifyBy zlj 2015年7月31日
     *
     * @param url
     * @param params key-value形式参数
     * @return null 请求失败
     */
    public static String postHttps(String url, Map<String, String> params) {
        CloseableHttpClient httpClient = getHttpClient(url);
        String body = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT).build();
            httpPost.setConfig(requestConfig);

            httpPost.setEntity(new UrlEncodedFormEntity(nvps,"utf-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);

            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                body = null;
                if (statusCode >= 200 && statusCode < 300) {
                    if (entity != null) {
                        body = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                    }
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch(SocketTimeoutException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }catch (UnsupportedEncodingException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return body;
    }


    /**
     *
     * @Title: delete
     * @Description: Delete请求
     * @param url
     * @param param
     * @return boolean 返回类型
     * @throws
     */
    public static boolean deleteHttps(String url, String param) {
        CloseableHttpClient httpClient = getHttpClient(url);//HttpClientUtil.createSSLClientDefault();

        boolean success = false;
        try {
            if (param != null && !"".equals(param.trim())) {
                url = String.format("%s?%s", url, param);
            }
            HttpDelete httpDelete = new HttpDelete(url);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT).build();
            httpDelete.setConfig(requestConfig);

            CloseableHttpResponse response = httpClient.execute(httpDelete);
            try {
                HttpEntity entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    success = true;
                    EntityUtils.consume(entity);
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return success;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @author zlw
     * @date 2015年7月31日
     * @modifyBy zlw 2015年7月31日
     *
     * @param url
     * @param json json格式参数
     * @return
     */
    public static String postHttpsJson(String url, String json) {
        CloseableHttpClient httpClient = getHttpClient(url);
        String body = null;
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");

            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    body = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                }

            } catch (Exception e) {
                log.error(ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (ClientProtocolException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return body;
    }
}