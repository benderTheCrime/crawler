package src.main.java.core.crawler;

import java.net.MalformedURLException;
import java.net.URL;

public class Crawler {

    private int activeThreadCount = 0;

    public static void main(String args[])
    {
        // TODO The arguments could be many things here, but here are a few 
        // key players:
        // - Root URL
        // - Number of Threads to open max
        // - Number of Websites to "Crawl to"
        // - Max time duration
        // - + Any results you are trying to obtain

        // Pull our URL from the arguments chain
        // TODO probably a good idea to do some type safety checking here
        String rootUrl = args[0];
        URL parsedRootUrl = null;
        int maxNumberOfThreads = Integer.valueOf(args[1]);

        // Try and declare our new URL
        try
        {
            parsedRootUrl = new URL(rootUrl);
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        // Next, we are going to initialize threads
        // Call a class that extends thread
    }

    @SuppressWarnings("unused")
    private Thread initializeThread()
    {
        return null;
    }
}