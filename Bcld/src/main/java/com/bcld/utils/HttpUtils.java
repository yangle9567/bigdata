package com.bcld.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * http工具类，有不完事的地方
 * 
 * @author liudecai
 * 
 */
public class HttpUtils {

    private static Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 以文件形式返回请求仅支持http
     * 
     * @param url
     * @return
     */
    public static File getFile(String url, Map<String, String> map, String proxyIp, String proxyPort, String proxyUsername, String proxyPassword) {
        if (null == proxyUsername) {
            proxyUsername = "";
        }
        if (null == proxyPassword) {
            proxyPassword = "";
        }
        try {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

            Set<Entry<String, String>> entrySet = map.entrySet();
            for (Entry<String, String> entry : entrySet) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "utf-8");

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);

            BasicHttpContext httpContext = new BasicHttpContext();

            DefaultHttpClient httpClient = new DefaultHttpClient();
            if (StringUtils.isNotBlank(proxyIp)) {
                if (null == proxyPort) {
                    proxyPort = "80";
                }
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxyIp, Integer.parseInt(proxyPort)), new UsernamePasswordCredentials(proxyUsername, proxyPassword));
                HttpHost proxy = new HttpHost(proxyIp, Integer.parseInt(proxyPort));
                httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
            }
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            File file = File.createTempFile(IdUtils.generateUuid(), "");
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(entity.getContent(), outputStream);
            IOUtils.closeQuietly(outputStream);
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getString(String url, String nameValuePairString) {
        String str = "";
        try {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            if (StringUtils.isNotBlank(nameValuePairString)) {
                String[] nameValuePairs = nameValuePairString.split(",");
                for (int i = 0; i < nameValuePairs.length; i++) {
                    String[] split = nameValuePairs[i].split("=");
                    params.add(new BasicNameValuePair(split[0], split[1]));
                }
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "utf-8");

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);

            BasicHttpContext httpContext = new BasicHttpContext();

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                str = IOUtils.toString(entity.getContent());
            }
        } catch (IOException e) {
        }
        return str;
    }

    /**
     * 根据类型的不同（get或post）返回请求头，响应头和响应实体，亦及页面内容。
     * 
     * @param type
     * @param url
     * @param domain
     * @param postNVP
     * @param cookieNVP
     * @return
     */
    public static Map<String, Object> getMap(String type, String url, String domain, String postNVP, String cookieNVP) {
        Map<String, Object> map = new HashMap<String, Object>();
        HttpClient httpClient = new DefaultHttpClient();
        BasicHttpContext httpContext = new BasicHttpContext();
        String content = "";
        String host = "";
        try {
            if (StringUtils.isNotBlank(url)) {
                URI uri = new URIBuilder(url).build();
                // 判断url是否包含端口
                if (uri.getPort() != -1) {
                    host = uri.getHost() + ":" + uri.getPort();
                } else {
                    host = uri.getHost();
                }
            }
        } catch (URISyntaxException e1) {
            log.info("url格式不正确");
        }
        // 分POST和GET两种类型进行讨论,每个类型里包含三个部分，分别是请求头，返回头和返回内容。首先讨论POST
        if (StringUtils.isNotBlank(type)) {

            if (type.equalsIgnoreCase("post")) {
                ArrayList<String> httpPostRequestHead = new ArrayList<String>();
                HttpPost httpPost = new HttpPost(url);
                // 这里开始处理请求头
                // 首先在httpPostRequestHead（请求头）中加入请求状态
                httpPostRequestHead.add(httpPost.getRequestLine().toString());
                httpPost.addHeader("Accept", "*/*");

                // 处理post name value对，将其放入formEntity
                UrlEncodedFormEntity formEntity = null;
                try {
                    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                    if (StringUtils.isNotBlank(postNVP)) {
                        String[] nameValuePairs = postNVP.split(",");
                        for (int i = 0; i < nameValuePairs.length; i++) {
                            String[] split = nameValuePairs[i].split("=");
                            // 此处为防止post数据格式不正确，采取的判断，即若“，”之间的数据不是数值对，则不处理。
                            if (split.length == 2) {
                                params.add(new BasicNameValuePair(split[0], split[1]));
                            }
                        }
                    }
                    formEntity = new UrlEncodedFormEntity(params, "utf-8");
                    httpPost.setEntity(formEntity);
                } catch (UnsupportedEncodingException e) {
                    log.info("post参数格式不对");
                }

                httpPost.addHeader(httpPost.getEntity().getContentType());
                httpPost.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:27.0) Gecko/20100101 Firefox/27.0");
                httpPost.addHeader("Host", host);

                // 添加cookie
                ArrayList<NameValuePair> cookies = new ArrayList<NameValuePair>();
                if (StringUtils.isNotBlank(cookieNVP)) {
                    String[] nameValuePairs = cookieNVP.split(";");
                    for (int i = 0; i < nameValuePairs.length; i++) {
                        httpPost.addHeader("Set-Cookie", nameValuePairs[i]);
                    }
                }

                // httpPost.addHeader("Set-Cookie",
                // "main[UTMPNUM]=6658; path=/; domain=.newsmth.net");
                // 下面的操作会影响post的执行，content-lenth不能随便指定。
                // httpPost.addHeader("Content-Length",
                // Long.toString(httpPost.getEntity().getContentLength()));

                // 其次加入httpPostRequestHead（请求头）中模拟浏览器的所有头信息
                HeaderIterator postRequestHeaderIt = httpPost.headerIterator();
                while (postRequestHeaderIt.hasNext()) {
                    httpPostRequestHead.add(postRequestHeaderIt.next().toString());
                }
                httpPostRequestHead.add("Content-Length: " + Long.toString(httpPost.getEntity().getContentLength()));
                // 最后加入formEntity中的变量信息，此部分为post独有
                try {
                    httpPostRequestHead.add(EntityUtils.toString(httpPost.getEntity()));
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                map.put("requestHead", httpPostRequestHead);
                // 至此请求头部分加载完毕

                // 处理返回头部分
                try {
                    // httpPost = new HttpPost(url);
                    // httpPost.setEntity(formEntity);
                    HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
                    ArrayList<String> httpPostResponseHead = new ArrayList<String>();
                    httpPostResponseHead.add(httpResponse.getStatusLine().toString());
                    HttpEntity entity = httpResponse.getEntity();
                    if (entity != null) {
                        HeaderIterator postResponseHeaderIt = httpResponse.headerIterator();
                        while (postResponseHeaderIt.hasNext()) {
                            httpPostResponseHead.add(postResponseHeaderIt.next().toString());
                        }
                        map.put("responseHead", httpPostResponseHead);
                        // 添加返回内容
                        // content = IOUtils.toString(entity.getContent());
                        content = EntityUtils.toString(entity);
                        map.put("content", content);
                        // 至此用于post的map构成完毕
                    }

                } catch (ClientProtocolException e) {
                    log.info("协议异常");
                } catch (IOException e) {
                    log.info("IO异常");
                }
            } else {
                // 按get方式进行请求和返回,此部分代码对应上面post部分，主要区别是，get不用formEntity

                ArrayList<String> httpGetRequestHead = new ArrayList<String>();
                HttpGet httpGet = new HttpGet(url);
                // 这里开始处理请求头
                // 首先在httpGetRequestHead（请求头）中加入请求状态
                httpGetRequestHead.add(httpGet.getRequestLine().toString());
                httpGet.addHeader("Accept", "*/*");

                httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
                httpGet.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:27.0) Gecko/20100101 Firefox/27.0");
                httpGet.addHeader("Host", host);

                // 添加cookie
                ArrayList<NameValuePair> cookies = new ArrayList<NameValuePair>();
                if (StringUtils.isNotBlank(cookieNVP)) {
                    String[] nameValuePairs = cookieNVP.split(";");
                    for (int i = 0; i < nameValuePairs.length; i++) {
                        httpGet.addHeader("Set-Cookie", nameValuePairs[i]);
                    }
                }

                // 其次加入httpGetRequestHead（请求头）中模拟浏览器的所有头信息
                HeaderIterator getRequestHeaderIt = httpGet.headerIterator();
                while (getRequestHeaderIt.hasNext()) {
                    httpGetRequestHead.add(getRequestHeaderIt.next().toString());
                }
                map.put("requestHead", httpGetRequestHead);
                // 至此请求头部分加载完毕

                // 处理返回头部分
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
                    ArrayList<String> httpGetResponseHead = new ArrayList<String>();
                    httpGetResponseHead.add(httpResponse.getStatusLine().toString());
                    HttpEntity entity = httpResponse.getEntity();
                    if (entity != null) {
                        HeaderIterator getResponseHeaderIt = httpResponse.headerIterator();
                        while (getResponseHeaderIt.hasNext()) {
                            httpGetResponseHead.add(getResponseHeaderIt.next().toString());
                        }
                        map.put("responseHead", httpGetResponseHead);
                        // 添加返回内容
                        content = EntityUtils.toString(entity);
                        map.put("content", content);
                        // 至此用于get的map构成完毕
                    }

                } catch (ClientProtocolException e) {
                    log.info("协议异常");
                } catch (IOException e) {
                    log.info("IO异常");
                }

            }
        }
        return map;
    }

    /**
     * 用于连接返回json的链接
     * 
     * @param url
     * @param nameValuePairs
     * @return
     */
    public static Map<String, String> postForJson(String url, String... nameValuePairs) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            for (int i = 0; i < nameValuePairs.length; i++) {
                String[] split = nameValuePairs[i].split("=");
                params.add(new BasicNameValuePair(split[0], split[1]));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "utf-8");

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);

            BasicHttpContext httpContext = new BasicHttpContext();

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Gson gson = new Gson();
                String json = IOUtils.toString(entity.getContent());
                map = gson.fromJson(json, HashMap.class);
            }
        } catch (IOException e) {
        }
        return map;
    }

    /**
     * 用于连接返回object的请求
     * 
     * @param url
     * @param nameValuePairs
     * @return
     */
    public static <T> T postForObject(String url, String... nameValuePairs) {
        String content = null;
        try {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            for (int i = 0; i < nameValuePairs.length; i++) {
                String[] split = nameValuePairs[i].split("=");
                params.add(new BasicNameValuePair(split[0], split[1]));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "utf-8");

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);

            BasicHttpContext httpContext = new BasicHttpContext();

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                content = null;
            } else {
                content = IOUtils.toString(entity.getContent());
            }
        } catch (IOException e) {

        }
        return (T) SerializerUtils.readObjectFromString(content);
    }
    
    
    /**
     * 用于连接返回object的请求
     * 
     * @param url
     * @param nameValuePairs
     * @return
     */
    public static <T> T postForObjects(String url, String... nameValuePairs) {
        String content = null;
        try {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            for (int i = 0; i < nameValuePairs.length; i++) {
                String[] split = nameValuePairs[i].split("=");
                params.add(new BasicNameValuePair(split[0], split[1]));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "utf-8");

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);

            BasicHttpContext httpContext = new BasicHttpContext();

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                content = null;
            } else {
                content = IOUtils.toString(entity.getContent());
            }
        } catch (IOException e) {

        }
        return (T) SerializerUtils.readObjectFromXml(content,Map.class);
    }

    /**
     * 用于连接返回json的链接
     * 
     * @param url
     * @param map
     * @return
     */
    public static Map<String, String> post(String url, Map<String, String> map) {
        log.info("请求的URL为：" + url);
        Map<String, String> result = new HashMap<String, String>();
        try {
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

            Set<Entry<String, String>> entrySet = map.entrySet();
            for (Entry<String, String> entry : entrySet) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, "utf-8");

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);

            BasicHttpContext httpContext = new BasicHttpContext();

            HttpClient httpClient = new DefaultHttpClient();
            if (url.indexOf("https") >= 0) {
                httpClient = HttpClientEnableTLSTrust(httpClient);
            }
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Gson gson = new Gson();
                String json = IOUtils.toString(entity.getContent());
                log.info("请求返回：" + json);
                System.out.println(json);
                result = gson.fromJson(json, HashMap.class);
            } else {
                log.info("请求返回为空");
            }
        } catch (IOException e) {
        }
        return result;
    }

    public static HttpClient HttpClientEnableTLSTrust(final HttpClient base) {
        try {
            final SSLContext ctx = SSLContext.getInstance("TLS");
            final TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] xcs, final String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] xcs, final String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            final SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            final ClientConnectionManager ccm = base.getConnectionManager();
            final SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));
            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception e) {
            return null;
        }
    }

}
