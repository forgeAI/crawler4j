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

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */

public interface DocIDServer {


    /**
     * Returns the docid of an already seen url.
     *
     * @param url the URL for which the docid is returned.
     * @return the docid of the url if it is seen before. Otherwise -1 is returned.
     */
    public String getDocId(WebURL url);

    /**
     * 
     * @param url
     * @return
     */
    public String getNewDocID(WebURL url);
    
    /**
     * 
     * @param url
     * @return
     */
    public WebURL getWebURL(String url);

    /**
     * 
     * @param url
     * @param docId
     * @throws Exception
     */
    public void addUrlAndDocId(WebURL url, String docId) throws Exception;

    /**
     * 
     * @param url
     * @return
     */
    public boolean isSeenBefore(String url);

    /**
     * 
     * @return
     */
    public int getDocCount();

    /**
     * 
     */
    public void close();
}