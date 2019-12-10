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
package org.tallison.quaerite.core;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.tallison.quaerite.core.features.CustomHandler;
import org.tallison.quaerite.core.features.URL;
import org.tallison.quaerite.core.features.factories.CustomHandlerFactory;
import org.tallison.quaerite.core.features.factories.FeatureFactories;
import org.tallison.quaerite.core.features.factories.FeatureFactory;
import org.tallison.quaerite.core.features.factories.QueryFactory;
import org.tallison.quaerite.core.features.factories.StringFeatureFactory;
import org.tallison.quaerite.core.queries.Query;
import org.tallison.quaerite.core.scorers.AbstractJudgmentScorer;
import org.tallison.quaerite.core.scorers.Scorer;
import org.tallison.quaerite.core.serializers.FeatureFactorySerializer;
import org.tallison.quaerite.core.serializers.QuerySerializer;
import org.tallison.quaerite.core.serializers.ScorerListSerializer;
import org.tallison.quaerite.core.util.MathUtil;

public class ExperimentFactory {

    public static  final String SEARCH_SERVER_URLS = "urls";

    private GAConfig gaConfig = new GAConfig();

    List<Query> filterQueries = new ArrayList<>();
    List<Scorer> scorers;
    FeatureFactories featureFactories;

    public static ExperimentFactory fromJson(Reader reader) {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeHierarchyAdapter(Scorer.class,
                        new ScorerListSerializer.ScorerSerializer<>())
                .registerTypeAdapter(FeatureFactories.class, new FeatureFactorySerializer())
                .registerTypeAdapter(Query.class, new QuerySerializer())
                .create();
        return gson.fromJson(reader, ExperimentFactory.class);
    }
    private transient Scorer trainScorer;
    private transient Scorer testScorer;

    public List<Scorer> getScorers() {
        for (Scorer scorer : scorers) {
            scorer.reset();
        }
        return scorers;
    }

    public int getMaxRows() {
        return Math.max(getTrainScorer().getAtN(), getTestScorer().getAtN());
    }

    @Override
    public String toString() {
        return "ExperimentFactory{" +
                "gaConfig=" + gaConfig +
                ", filterQueries=" + filterQueries +
                ", scorers=" + scorers +
                ", featureFactories=" + featureFactories +
                ", trainScorer=" + trainScorer +
                ", testScorer=" + testScorer +
                '}';
    }

    public FeatureFactories getFeatureFactories() {
        return featureFactories;
    }

    public Scorer getTrainScorer() {
        if (trainScorer == null) {
            if (scorers.size() == 0) {
                trainScorer = scorers.get(0);
            } else {
                boolean found = false;
                for (Scorer scorer : scorers) {
                    if ((scorer instanceof AbstractJudgmentScorer) &&
                            ((AbstractJudgmentScorer)scorer).getUseForTrain()) {
                        if (found) {
                            throw new IllegalArgumentException(
                                    "Can't have more than one train score aggregator!");
                        }
                        trainScorer = scorer;
                        found = true;
                    }
                }
            }
        }
        return trainScorer;
    }

    public Scorer getTestScorer() {
        if (testScorer == null) {
            if (scorers.size() == 1) {
                testScorer = scorers.get(0);
            } else {
                boolean found = false;
                for (Scorer scorer : scorers) {
                    if (scorer instanceof AbstractJudgmentScorer &&
                            ((AbstractJudgmentScorer)scorer).getUseForTest()) {
                        if (found) {
                            throw new IllegalArgumentException(
                                    "Can't have more than one test score aggregator!");
                        }
                        testScorer = scorer;
                        found = true;
                    }
                }
            }
        }
        return testScorer;
    }

    public GAConfig getGAConfig() {
        return gaConfig;
    }


    public Experiment generateRandomExperiment(String name) {
        FeatureFactory urlFactory = featureFactories.get(SEARCH_SERVER_URLS);
        String searchUrl = urlFactory.random().toString();
        CustomHandler customHandler = null;
        CustomHandlerFactory customHandlerfactory =
                (CustomHandlerFactory)featureFactories.get(
                        CustomHandlerFactory.NAME);
        if (customHandlerfactory != null) {
            customHandler = customHandlerfactory.random();
        }
        QueryFactory queryListFactory = (QueryFactory)featureFactories.get(QueryFactory.NAME);
        Experiment rand = new Experiment(name, searchUrl, customHandler, queryListFactory.random());
        addFilterQueries(rand);
        return rand;
    }

    private void addFilterQueries(Experiment experiment) {
        if (filterQueries != null) {
            experiment.addFilterQueries(filterQueries);
        }
    }

    public List<Experiment> permute(int maxExperiments) {
        List<Experiment> experiments = new ArrayList<>();
        for (URL url : ((StringFeatureFactory<URL>)(
                featureFactories.get(SEARCH_SERVER_URLS)))
                .permute(maxExperiments)) {
            List<Query> queries = (featureFactories.get(QueryFactory.NAME)).permute(maxExperiments);
            for (Query q : queries) {
                for (CustomHandler handler : permuteHandlers(maxExperiments)) {
                    if (experiments.size() >= maxExperiments) {
                        return experiments;
                    }
                    Experiment ex = new Experiment("permute_" + experiments.size(),
                            url.toString(), handler, q);
                    addFilterQueries(ex);
                    experiments.add(ex);
                }
            }
        }
        return experiments;
    }

    private List<CustomHandler> permuteHandlers(int maxSize) {
        if (featureFactories.get(CustomHandlerFactory.NAME) == null) {
            List<CustomHandler> customHandlers = new ArrayList<>();
            customHandlers.add(CustomHandler.DEFAULT_HANDLER);
            return customHandlers;
        }

        return ((CustomHandlerFactory)featureFactories.get(CustomHandlerFactory.NAME)).permute(maxSize);
    }

    public Pair<Experiment, Experiment> crossover(Experiment parentA, Experiment parentB) {
        StringFeatureFactory featureFactory = (StringFeatureFactory)featureFactories.get(SEARCH_SERVER_URLS);
        Pair<URL, URL> urls = featureFactory.crossover(
                new URL(parentA.getSearchServerUrl()),
                new URL(parentB.getSearchServerUrl()));
        Pair<CustomHandler, CustomHandler> customHandlers = Pair.of(null, null);
        if (featureFactories.get(CustomHandlerFactory.NAME) != null) {
            customHandlers = featureFactories.get(CustomHandlerFactory.NAME)
                    .crossover(parentA.getCustomHandler(), parentB.getCustomHandler());
        }

        QueryFactory queryFactory = (QueryFactory)featureFactories.get(
                QueryFactory.NAME);

        Pair<Query, Query> queries = queryFactory.crossover(parentA.getQuery(), parentB.getQuery());

        URL urlA = (MathUtil.RANDOM.nextFloat() <= 0.5) ? urls.getLeft()
                : urls.getRight();
        CustomHandler customHandlerA = (MathUtil.RANDOM.nextFloat() <= 0.5) ?
                customHandlers.getLeft() : customHandlers.getRight();
        Query queryA = (MathUtil.RANDOM.nextFloat() <= 0.5) ?
                queries.getLeft() : queries.getRight();
        Experiment childA = new Experiment("childA", urlA.toString(),
                customHandlerA, queryA);

        URL urlB = (MathUtil.RANDOM.nextFloat() <= 0.5) ? urls.getLeft() : urls.getRight();
        CustomHandler customHandlerB = (MathUtil.RANDOM.nextFloat() <= 0.5) ?
                customHandlers.getLeft() : customHandlers.getRight();
        Query queryB = (MathUtil.RANDOM.nextFloat() <= 0.5) ?
                queries.getLeft() : queries.getRight();
        Experiment childB = new Experiment("childB", urlB.toString(),
                customHandlerB, queryB);
        addFilterQueries(childA);
        addFilterQueries(childB);
        return Pair.of(childA, childB);

    }

    public Experiment mutate(Experiment parent, float mutationProbability, float mutationAmplitude) {
        Experiment mutated = parent.deepCopy();
        if (MathUtil.RANDOM.nextFloat() < mutationProbability) {
            FeatureFactory featureFactory = featureFactories.get(
                    SEARCH_SERVER_URLS);
            String serverUrl = featureFactory.mutate(
                    new URL(mutated.getSearchServerUrl()),
                    mutationProbability, mutationAmplitude).toString();
            mutated.setSearchServerUrl(serverUrl);
        }

        if (MathUtil.RANDOM.nextFloat() < mutationProbability &&
                featureFactories.get(CustomHandlerFactory.NAME) != null) {
            CustomHandler customHandler = mutated.getCustomHandler();
            CustomHandler mutatedHandler =
                    ((CustomHandlerFactory)featureFactories.get(CustomHandlerFactory.NAME))
                            .mutate(customHandler, mutationProbability, mutationAmplitude);
            mutated.setCustomHandler(mutatedHandler);
        }

        if (MathUtil.RANDOM.nextFloat() < mutationProbability) {
            Query q = mutated.getQuery();
            Query mutatedQuery =
                    ((QueryFactory)featureFactories.get(QueryFactory.NAME))
                            .mutate(q, mutationProbability, mutationAmplitude);
            mutated.setQuery(mutatedQuery);
        }
        addFilterQueries(mutated);
        return mutated;
    }
}
