package core.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by josephgroseclose on 12/10/15.
 */
// TODO Pull more of the Jsoup stuff in here
public class RequestUtil
{
    private static CloseableHttpClient client = null;

    public static HttpResponse<String> getHtmlFromURL(URL url)
            throws UnirestException, IllegalArgumentException,
            URISyntaxException, ClientProtocolException
    {
        Unirest.setHttpClient(client);

        // Apparently this is one of the better URL escaping options in Java...
        URI parsedURI = new URI(url.getProtocol(), url.getUserInfo(),
                url.getHost(), url.getPort(), url.getPath(), url.getQuery(),
                url.getRef());

        return Unirest.get(parsedURI.toString()).asString();
    }

    public static void setHttpClientCookiePolicy()
    {
        RequestConfig customizedRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
        HttpClientBuilder customizedClientBuilder = HttpClients
                .custom().setDefaultRequestConfig(customizedRequestConfig);
       client = customizedClientBuilder.build();
    }
}
