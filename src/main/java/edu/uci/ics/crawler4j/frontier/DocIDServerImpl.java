/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.frontier;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar
 */

public class DocIDServerImpl extends Configurable implements DocIDServer {
    private static final Logger logger = LoggerFactory.getLogger(DocIDServerImpl.class);

    private final Database docIDsDB;
    private static final String DATABASE_NAME = "DocIDs";

    private final Object mutex = new Object();

    private int lastDocID;

    public DocIDServerImpl(Environment env, CrawlConfig config) {
        super(config);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(config.isResumableCrawling());
        dbConfig.setDeferredWrite(!config.isResumableCrawling());
        lastDocID = 0;
        docIDsDB = env.openDatabase(null, DATABASE_NAME, dbConfig);
        if (config.isResumableCrawling()) {
            int docCount = getDocCount();
            if (docCount > 0) {
                logger.info("Loaded {} URLs that had been detected in previous crawl.", docCount);
                lastDocID = docCount;
            }
        }
    }

    /**
     * Returns the docid of an already seen url.
     *
     * @param url the URL for which the docid is returned.
     * @return the docid of the url if it is seen before. Otherwise -1 is returned.
     */
    public String getDocId(WebURL url) {
        synchronized (mutex) {
            OperationStatus result = null;
            DatabaseEntry value = new DatabaseEntry();
            try {
                DatabaseEntry key = new DatabaseEntry(url.getURL().getBytes());
                result = docIDsDB.get(null, key, value, null);

            } catch (Exception e) {
                logger.error("Exception thrown while getting DocID", e);
                return null;
            }

            if ((result == OperationStatus.SUCCESS) && (value.getData().length > 0)) {
                return new String(value.getData());
            }

            return null;
        }
    }

    public String getNewDocID(WebURL url) {
    	try {
    		// Make sure that we have not already assigned a docid for this URL
    		String docID = getDocId(url);
    		if (docID != null ) {
    			return docID;
    		}

    		docID = UUID.randomUUID().toString();
    		url.setDocid(docID);
    		docIDsDB.put(null, new DatabaseEntry(url.getURL().getBytes()),
                             new DatabaseEntry(url.getDocid().getBytes()));
    		
    		return docID;
    	} catch (Exception e) {
    		logger.error("Exception thrown while getting new DocID", e);
    		return null;
    	}
    }

    public void addUrlAndDocId(WebURL url, String docId) throws Exception {
        synchronized (mutex) {
        	
        	if(docId == null || docId.isEmpty()) {
                throw new Exception(
                		"DocId may not be null or empty");
        	}
        	

            // Make sure that we have not already assigned a docid for this URL
            String prevDocid = getDocId(url);
            if (prevDocid != null) {
                if (prevDocid.equals(docId)) {
                    return;
                }
                throw new Exception("Doc id: " + prevDocid + " is already assigned to URL: " + url);
            }

            docIDsDB.put(null, new DatabaseEntry(url.getURL().getBytes()),
                         new DatabaseEntry(docId.getBytes()));
        }
    }

    public boolean isSeenBefore(WebURL url) {
        return getDocId(url) != null;
    }

    public final int getDocCount() {
        try {
            return (int) docIDsDB.count();
        } catch (DatabaseException e) {
            logger.error("Exception thrown while getting DOC Count", e);
            return -1;
        }
    }

    public void close() {
        try {
            docIDsDB.close();
        } catch (DatabaseException e) {
            logger.error("Exception thrown while closing DocIDServer", e);
        }
    }


	@Override
	public WebURL getWebURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSeenBefore(String url) {
        String canonicalUrl = URLCanonicalizer.getCanonicalURL(url);
		WebURL webUrl = new WebURL();
		webUrl.setURL(canonicalUrl);
		return isSeenBefore(webUrl);
	}
}