package edu.uci.ics.crawler4j.fetcher.politeness;

import edu.uci.ics.crawler4j.url.WebURL;

public interface PolitenessServer {

    public static int NO_POLITENESS_APPLIED = -1;
	
	/**
	 * @param url
	 * @return
	 */
    public long applyPoliteness(WebURL url);

    /**
     * This can be used to force cache clean up. Per default it performs small amounts of maintenance during write operations,
     * or during occasional read operations if writes are rare. See https://github.com/google/guava/wiki/CachesExplained
     */
    public void forceCleanUp();

    /**
     * @return
     */
    public long getSize();

}