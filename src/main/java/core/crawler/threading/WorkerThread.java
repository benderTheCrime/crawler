package core.crawler.threading;

import core.crawler.Crawler;
import core.util.ResultUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by josephgroseclose on 12/16/15.
 */
public class WorkerThread implements Runnable
{
    public WorkerThread() {}

    private Set<String> keywords = new HashSet<String>();
    private Set<String> inputURLs = new HashSet<String>();
    private int maxURLPatternReach = 0;
    private String urlContainsMatchPattern;
    private Map<String, Map<String, Integer>> keywordMatchesByURL =
            null;

    @Override
    public void run()
    {
        try
        {
            findKeyWordsAndSpiderLinks();
        } catch (Exception e) {

            // TODO we just don't want to throw a ton of errors when we force out
        }
    }

    private void findKeyWordsAndSpiderLinks()
    {
        URL parsedUrl;
        List results;
        Map<String, Integer> resultKeywords;
        Set<String> resultURLs;

        for (String url : inputURLs)
        {
            if (keywordMatchesByURL.containsKey(url))
            {
                continue;
            }

            try
            {
                parsedUrl = new URL(url);
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                continue;
            }

            results = ResultUtil.findKeywordsInHTMLResult(parsedUrl,
                    this.keywords);
            resultKeywords = (Map<String, Integer>) results.get(0);
            resultURLs = (Set<String>) results.get(1);

            if (!resultKeywords.isEmpty())
            {
                keywordMatchesByURL.put(url, resultKeywords);
            }

            Crawler.negotiateThreadAssignment(resultURLs);
        }
    }

    public WorkerThread setKeywords(Set<String> keywords)
    {
        this.keywords = keywords;
        return this;
    }

    public WorkerThread setInputURLs(Set<String> inputURLs)
    {
        this.inputURLs = inputURLs;
        return this;
    }

    public WorkerThread setMaxURLPatternReach(int maxURLPatternReach)
    {
        this.maxURLPatternReach = maxURLPatternReach;
        return this;
    }

    public WorkerThread setUrlContainsMatchPattern(String urlContainsMatchPattern)
    {
        this.urlContainsMatchPattern = urlContainsMatchPattern;
        return this;
    }

    public WorkerThread setKeywordMatchesByURL(Map<String,
            Map<String, Integer>> keywordMatchesByURL)
    {
        this.keywordMatchesByURL = keywordMatchesByURL;
        return this;
    }
}
