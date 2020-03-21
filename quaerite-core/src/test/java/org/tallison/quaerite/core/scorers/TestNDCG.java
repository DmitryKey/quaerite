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
package org.tallison.quaerite.core.scorers;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tallison.quaerite.core.Judgments;
import org.tallison.quaerite.core.QueryInfo;
import org.tallison.quaerite.core.QueryStrings;
import org.tallison.quaerite.core.SearchResultSet;
import org.tallison.quaerite.core.StoredDocument;


public class TestNDCG {
    static Judgments JUDGMENTS = new Judgments(new QueryInfo("0","",
            new QueryStrings(), 1));
    static SearchResultSet RESULT_SET;
    @BeforeAll
    public static void setUp() {

        JUDGMENTS.addJudgment("1", 3);
        JUDGMENTS.addJudgment("2", 2);
        JUDGMENTS.addJudgment("3", 3);
        JUDGMENTS.addJudgment("5", 1);
        JUDGMENTS.addJudgment("6", 2);
        JUDGMENTS.addJudgment("7", 3);
        JUDGMENTS.addJudgment("8", 2);
        List<StoredDocument> ids = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            ids.add(new StoredDocument(Integer.toString(i)));
        }
        RESULT_SET =
                new SearchResultSet(1000, 10, 100, ids);
    }


    @Test
    public void testCG() {
        CumulativeGain cumulativeGain = new CumulativeGain(10);
        Assertions.assertEquals(11.0f, cumulativeGain.score(JUDGMENTS, RESULT_SET), 0.001);
    }

    @Test
    public void testDCG2002() {
        DiscountedCumulativeGain2002 dcg2002 = new DiscountedCumulativeGain2002(10);
        Assertions.assertEquals(6.861, dcg2002.score(JUDGMENTS, RESULT_SET), 0.001);
    }

    @Test
    public void testNDCG() {
        NDCG ndcg =
                new NDCG(10);
        Assertions.assertEquals(0.785, ndcg.score(JUDGMENTS, RESULT_SET), 0.001);
    }
}
