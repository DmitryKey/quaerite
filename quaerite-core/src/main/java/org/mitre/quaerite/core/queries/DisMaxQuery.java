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
package org.mitre.quaerite.core.queries;


import org.mitre.quaerite.core.features.BF;
import org.mitre.quaerite.core.features.BQ;
import org.mitre.quaerite.core.features.PF;

public class DisMaxQuery extends MultiFieldQuery {


    protected PF pf;
    protected BQ bq;
    protected BF bf;


    public DisMaxQuery() {
       super();
    }

    public DisMaxQuery(String queryString) {
        super(queryString);
    }

    public void setPf(PF pf) {
        this.pf = pf;
    }

    @Override
    public String getName() {
        return "dismax";
    }

    @Override
    public DisMaxQuery deepCopy() {
        DisMaxQuery cp = new DisMaxQuery();
        cp.pf = (pf == null) ? null : pf.deepCopy();
        cp.bq = (bq == null) ? null : bq.deepCopy();
        cp.bf = (bf == null) ? null : bf.deepCopy();
        cp.qf = (qf == null) ? null : qf.deepCopy();
        cp.tie = (tie == null) ? null : tie.deepCopy();
        cp.setQueryString(getQueryString());
        cp.setQueryStringName(getQueryStringName());
        cp.setQueryOperator(getQueryOperator());
        return cp;
    }

    public PF getPF() {
        return pf;
    }
}
