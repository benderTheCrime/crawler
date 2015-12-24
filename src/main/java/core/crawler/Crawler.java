package core.crawler;

import core.crawler.threading.WorkerThread;

import core.util.MapUtil;
import core.util.RequestUtil;

import java.util.Date;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by josephgroseclose on 12/11/15.
 */
public class Crawler
{
    private static final String MAXIMUM_URL_PATTERN_REACH_KEYWORD = "--reach";
    private static final String MAXIMUM_WORKER_THREAD_COUNT_KEYWORD = "--threads";
    private static final String KEYWORDS_KEYWORD = "--keywords";
    private static final String URL_CONTAINS_MATCH_PATTERN_KEYWORD = "--match";

    private static int MAXIMUM_URL_PATTERN_REACH = 5000;
    private static int MAXIMUM_WORKER_THREAD_COUNT = 10;
    private static Set<String> KEYWORDS = new HashSet<String>();
    private static String URL_CONTAINS_MATCH_PATTERN = null;
    private static ThreadPoolExecutor executors;
    private static Map<String, Map<String, Integer>> keywordMatchesByURL =
            new HashMap<String, Map<String, Integer>>();
    private static final int MAX_COLLECTIVELY_VISITED_URL_COUNT = 300000;
    private static final Map<String, Integer> keywordCountByURL =
            new HashMap<String, Integer>();
    private static final LinkedList<Set<String>> urlsFoundPendingThreadAssignment =
            new LinkedList<Set<String>>();
    private static final Set<String> collectivelyVisistedURLs =
            new HashSet<String>();

    public static void main(String args[])
    {
        Date date = new Date();
        Set<String> rootURLs = new HashSet<String>();
        String rootURL;
        String key;
        String value;
        Long minutes;
        int i;

        if (args.length > 0)
        {
            rootURL = args[0];
            rootURLs.add(rootURL);
        }
        else
        {
            throw new IllegalStateException("Missing argument \"rootUrl\"");
        }

        for (i = 1; i < args.length; i += 2)
        {
            key = args[i];
            value = args[i + 1];

            switch (key)
            {
                case MAXIMUM_URL_PATTERN_REACH_KEYWORD:
                    MAXIMUM_URL_PATTERN_REACH = Integer.valueOf(value);
                    break;
                case MAXIMUM_WORKER_THREAD_COUNT_KEYWORD:
                    MAXIMUM_WORKER_THREAD_COUNT = Integer.valueOf(value);
                    break;
                case KEYWORDS_KEYWORD:
                    KEYWORDS.addAll(Arrays.asList(value.split(",")));
                    break;
                case URL_CONTAINS_MATCH_PATTERN_KEYWORD:
                    URL_CONTAINS_MATCH_PATTERN = value;
            }
        }

        // Allow cookies based on Chrome's cookie policy
        RequestUtil.setHttpClientCookiePolicy();


        executors = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                MAXIMUM_WORKER_THREAD_COUNT);

        urlsFoundPendingThreadAssignment.add(rootURLs);
        negotiateThreadAssignment(rootURLs);
        while (keywordMatchesByURL.size() < MAXIMUM_URL_PATTERN_REACH &&
                executors.getPoolSize() > 0 &&
                collectivelyVisistedURLs.size() <
                        MAX_COLLECTIVELY_VISITED_URL_COUNT)
        {
            if (executors.getPoolSize() < MAXIMUM_WORKER_THREAD_COUNT &&
                    executors.getQueue().size() > 0)
            {
                try
                {
                    executors.getQueue().take().run();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        executors.shutdown();

        while (!executors.isShutdown()) {}

        // Calculate the time difference
        minutes = (new Date().getTime() - date.getTime()) / (1000 * 60);

        keywordMatchesByURL.forEach((k, v) -> keywordCountByURL
                .put(k, v.values().stream().mapToInt(Number::intValue).sum()));

        System.out.println("Strength of Keyword Matches by URL:");
        System.out.println(MapUtil.sortByValue(keywordCountByURL));
        System.out.println("Keyword Matches by URL:");
        System.out.println(keywordMatchesByURL);
        System.out.println("Minutes to completion: " + minutes);
    }

    public static void negotiateThreadAssignment(Set<String> urls)
    {
        Set<String> copyCollectivelyVistedURLs =
                new HashSet<String>(collectivelyVisistedURLs);

        urls.removeAll(copyCollectivelyVistedURLs);

        if (!urls.isEmpty())
        {
            collectivelyVisistedURLs.addAll(urls);

            WorkerThread worker = new WorkerThread()
                    .setKeywords(KEYWORDS)
                    .setInputURLs(urls)
                    .setMaxURLPatternReach(MAXIMUM_URL_PATTERN_REACH)
                    .setUrlContainsMatchPattern(URL_CONTAINS_MATCH_PATTERN)
                    .setKeywordMatchesByURL(keywordMatchesByURL);

            if (executors.getPoolSize() < executors.getMaximumPoolSize())
            {
                executors.execute(worker);
            }
            else
            {
                try
                {
                    executors.getQueue().put(worker);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}