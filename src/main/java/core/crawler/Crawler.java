package core.crawler;

import core.util.MapUtil;
import core.util.RequestUtil;
import core.util.ResultUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by josephgroseclose on 12/11/15.
 */
public class Crawler
{
    private static int MAXIMUM_URL_PATTERN_REACH = 5000;
    private static int MAXIMUM_WORKER_THREAD_COUNT = 10;
    private static int CURRENT_WORKER_THREAD_COUNT = 0;
    private static final Map<String, Map<String, Integer>> keywordMatchesByURL =
            new HashMap<String, Map<String, Integer>>();
    private static final Map<String, Integer> keywordCountByURL =
            new HashMap<String, Integer>();
    private static final Map<String, Set<String>> urlsFoundInPageHTMLs =
            new HashMap<String, Set<String>>();
    private static final Set<String> urlsFoundCollectively =
            new HashSet<String>();

    public static void main(String args[])
    {
        /*
        TODO sort output, maximum count of matches across all keywords
        TODO word boundaries
        TODO number of threads, have to globalize the "stack" and pass off a
            certain number of urls to each thread
        // TODO separate concerns
        TODO interface
        TODO api
        */

        Set<String> rootURLs = new HashSet<String>();
        String rootURL;
        Set<String> keywords;

        if (args.length > 0)
        {
            rootURL = args[0];
            rootURLs.add(rootURL);
        }
        else
        {
            throw new IllegalStateException("Missing argument \"rootUrl\"");
        }

        if (args.length > 1)
        {
            keywords = new HashSet<String>(Arrays.asList(args[1].split(",")));
        }
        else
        {
            throw new IllegalStateException("Missing argument \"keywords\"");
        }

        if (args.length > 2)
        {
            MAXIMUM_URL_PATTERN_REACH = Integer.valueOf(args[2]);
        }

        if (args.length > 3)
        {
            MAXIMUM_WORKER_THREAD_COUNT = Integer.valueOf(args[3]);
        }

        System.out.println("Root URL            : " + rootURL);
        System.out.println("Maximum URL Count   : " + MAXIMUM_URL_PATTERN_REACH);
        System.out.println("Keywords            : " + keywords);

        RequestUtil.setHttpClientCookiePolicy();

        findKeyWordsAndSpiderLinks(rootURLs, keywords);

        // TODO Move this
        keywordMatchesByURL.forEach((k, v) -> keywordCountByURL
                .put(k, v.values().stream().mapToInt(Number::intValue).sum()));

        System.out.println(MapUtil.sortByValue(keywordCountByURL));
        System.out.println(keywordMatchesByURL);
    }

    @SuppressWarnings("unused")
    private Thread initializeThread()
    {
        return null;
    }

    private static Thread negotiateThreadAssignment()
    {
        // Checks to see how many threads are up
        // Spins up a new thread if one is not active
        // Closes unused threads
        // Keeps track of state of threads
        return null;
    }

    private static void findKeyWordsAndSpiderLinks(Set<String> urls,
            Set<String> keywords)
    {
        Set<String> urlSet = new HashSet<String>();
        boolean maximumURLPatternReachMet = false;
        URL parsedUrl;
        List results;
        Map<String, Integer> resultKeywords;
        Set<String> resultURLs;

        loop:
        for (String url : urls)
        {
            if (keywordMatchesByURL.containsKey(url)) {
                continue;
            }

            try {
                parsedUrl = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                continue;
            }

            results = ResultUtil.findKeywordsInHTMLResult(parsedUrl,
                    keywords);
            resultKeywords = (Map<String, Integer>) results.get(0);
            resultURLs = (Set<String>) results.get(1);

            if (!resultKeywords.isEmpty())
            {
                keywordMatchesByURL.put(url, resultKeywords);
            }

            /*
             * Keep track of the number of websites we've visited to obtain this
             * result
             */
            if (!resultURLs.isEmpty())
            {
                urlSet.addAll(resultURLs);
                urlsFoundInPageHTMLs.put(url, resultURLs);

                for (String resultURL : resultURLs)
                {
                   if (urlsFoundCollectively.size() <=
                           MAXIMUM_URL_PATTERN_REACH)
                   {
                       urlsFoundCollectively.add(resultURL);
                   }
                   else
                   {
                       maximumURLPatternReachMet = true;
                       break loop;
                   }
                }
            }
        }

        if (!maximumURLPatternReachMet)
        {
            findKeyWordsAndSpiderLinks(urlSet, keywords);
        }
    }
}