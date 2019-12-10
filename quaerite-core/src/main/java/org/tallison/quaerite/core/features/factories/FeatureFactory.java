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
package org.tallison.quaerite.core.features.factories;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.tallison.quaerite.core.features.Feature;

public interface FeatureFactory<T extends Feature> {

    /**
     * For now, this returns a list of values
     * for a given Solr parameter
     * @return
     */
    //Set<Feature> getEachDefaultFeature();

    List<T> permute(int maxSize);

    T random();

    T mutate(T feature, double probability, double amplitude);

    Pair<T,T> crossover(T parentA, T parentB);
}
