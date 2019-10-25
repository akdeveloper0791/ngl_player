package com.ibetter.www.adskitedigi.adskitedigi.green_content.drop_box;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

public class DropboxClientFactory
{

    private static String ACCESS_TOKEN="mkW1hX251tAAAAAAAAAK8TvQzlCj0VwXFCAyHAWCI18CNSpPNDU4a1EUC6Xxt3R9";
    private static DbxClientV2 sDbxClient;

    public static void init() {
        if (sDbxClient == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("examples-v2-demo")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();

            sDbxClient = new DbxClientV2(requestConfig, ACCESS_TOKEN);
        }
    }

    public static DbxClientV2 getClient()
    {
        if (sDbxClient == null) {

            init();
        }
        return sDbxClient;
    }

}
