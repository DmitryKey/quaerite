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
package org.tallison.quaerite.core.features;

import java.util.List;

/**
 * Used in Solr's DisMax and EDisMax queries
 */
public class DisMaxBoost extends ParameterizableStringListFeature {

    private static final String NAME = "boost";

    public DisMaxBoost(List<ParameterizableString> strings) {
        super(NAME);
        addAll(strings);
    }

    @Override
    public DisMaxBoost deepCopy() {
        DisMaxBoost deepCopy = new DisMaxBoost(parameterizableStrings);
        return deepCopy;
    }
}
