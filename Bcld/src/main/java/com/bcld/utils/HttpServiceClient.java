package com.bcld.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServiceClient {
    private static Logger log = LoggerFactory.getLogger(HttpServiceClient.class);
    /**
     * 管理中心的URL
     */
    private String url;

    public HttpServiceClient(String url) {
        super();
        this.url = url + "/service/http_service";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url + "/service/http_service";
    }

}
