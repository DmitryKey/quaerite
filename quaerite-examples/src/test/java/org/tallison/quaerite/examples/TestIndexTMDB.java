/*
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
package org.tallison.quaerite.examples;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("need to have Solr/ES tmdb instance running" +
        "and need to set JSON_PATH to the location of the tmdb.json file")
public class TestIndexTMDB {

    private static final String JSON_PATH = "C:/data/tmdb.json";
    private static final String SOLR = "http://localhost:8983/solr/tmdb";
    private static final String ES = "http://localhost:9200/tmdb";

    @Test
    public void testLoadingSolr() throws Exception {
        IndexTMDB.main(
                new String[]{JSON_PATH, SOLR});
    }

    @Test
    public void testLoadingES() throws Exception {
        IndexTMDB.main(
                new String[]{JSON_PATH, ES});
    }
}
