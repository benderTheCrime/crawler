package core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.net.URL;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.http.client.ClientProtocolException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by josephgroseclose on 12/10/15.
 */
public class ResultUtil
{
    public static List findKeywordsInHTMLResult(URL url, Set<String> keywords)

    {
        Map<String, Integer> keywordOccurancesByURL = new HashMap<>();
        Set<String> urls = new HashSet<String>();

        List resultsList = new ArrayList()
        {{
            add(keywordOccurancesByURL);
            add(urls);
        }};

        Document document = null;
        HttpResponse<String> html;
        String body;
        InputStream htmlStream;
        Pattern pattern;
        Matcher matches;


        try
        {
            html = RequestUtil.getHtmlFromURL(url);
        }

        /**
         * Don't really care what error happens here...move on, plenty of fish
         * in the sea
         */
        catch (Exception e)
        {
            // e.printStackTrace();
            return resultsList;
        }

        if (html == null)
        {
            return resultsList;
        }
        else
        {
            htmlStream = new ByteArrayInputStream(html.getBody().getBytes());
        }

        try
        {
            document = Jsoup.parse(htmlStream, "UTF-8", url.toString());
        }
        catch (IOException e)
        {
            // e.printStackTrace();
        }

        body = document.text();

        for (String keyword : keywords)
        {
            if (body.indexOf(keyword) != -1)
            {
                int count = 0;

                pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
                matches = pattern.matcher(body);

                while (matches.find())
                {
                    count += 1;
                }

                keywordOccurancesByURL.put(keyword, count);
            }
        }

        urls.addAll(document.select("a[href]").parallelStream()
                .map(t -> t.attr("href"))
                .map(t -> t.indexOf("http") == -1 ? url + "/" + t : t)
                .collect(Collectors.toSet()));

        return resultsList;
    }
}
