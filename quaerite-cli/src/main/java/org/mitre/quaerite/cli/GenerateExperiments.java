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
package org.mitre.quaerite.cli;

import static org.mitre.quaerite.core.util.CommandLineUtil.getPath;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mitre.quaerite.core.Experiment;
import org.mitre.quaerite.core.ExperimentFactory;
import org.mitre.quaerite.core.ExperimentSet;
import org.mitre.quaerite.core.scorers.Scorer;


public class GenerateExperiments extends AbstractCLI {

    enum MODE {
        PERMUTE,
        RANDOM
    }

    private static final int DEFAULT_MAX = 10000;

    static Options OPTIONS = new Options();

    static {
        OPTIONS.addOption(
                Option.builder("f")
                        .longOpt("experimentFactory")
                        .hasArg()
                        .desc("experiment factory json file")
                        .required().build()
        );
        OPTIONS.addOption(
                Option.builder("e")
                        .longOpt("experiments")
                        .hasArg()
                        .desc("experiments file to write to")
                        .required().build()
        );
        OPTIONS.addOption(
                Option.builder("p")
                        .longOpt("permute")
                        .hasArg(false)
                        .desc("all permutations (default)")
                        .required(false).build()
        );
        OPTIONS.addOption(
                Option.builder("r")
                        .longOpt("random")
                        .hasArg(true)
                        .desc("generate x random experiments based on the experiment factory")
                        .required(false).build()
        );
        OPTIONS.addOption(
                Option.builder("m")
                        .longOpt("max")
                        .hasArg(true)
                        .desc("maximum number of experiments to generate (default is "
                                + DEFAULT_MAX + ")")
                        .required(false).build()
        );
    }

    private int experimentCount = 0;

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = null;

        try {
            commandLine = new DefaultParser().parse(OPTIONS, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(
                    "java -jar org.mitre.quaerite.cli.GenerateExperiments",
                    OPTIONS);
            return;
        }
        int max = DEFAULT_MAX;
        if (commandLine.hasOption("m")) {
            max = Integer.parseInt(commandLine.getOptionValue("m"));
        } else if (commandLine.hasOption("r")) {
            max = Integer.parseInt(commandLine.getOptionValue("r"));
        }
        MODE mode = MODE.PERMUTE;
        if (commandLine.hasOption("r")) {
            mode = MODE.RANDOM;
        }
        Path input = getPath(commandLine, "f", true);
        Path output = getPath(commandLine, "e", false);
        GenerateExperiments generateExperiments = new GenerateExperiments();
        generateExperiments.execute(input, output, new GenerateConfig(mode, max));
    }

    private void execute(Path input, Path output, GenerateConfig generateConfig) throws Exception {
        ExperimentFactory experimentFactory = null;

        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            experimentFactory = ExperimentFactory.fromJson(reader);
        }
        ExperimentSet experimentSet = new ExperimentSet(experimentFactory.getGAConfig());
        for (Scorer scorer : experimentFactory.getScorers()) {
            experimentSet.addScorer(scorer);
        }
        if (generateConfig.mode == MODE.PERMUTE) {
            for (Experiment experiment : experimentFactory.permute(generateConfig.max)) {
                experimentSet.addExperiment(experiment);
            }
        } else {
            for (int i = 0; i < generateConfig.max; i++) {
                Experiment experiment = experimentFactory.generateRandomExperiment("rand_" + i);
                experimentSet.addExperiment(experiment);
            }
        }

        try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write(experimentSet.toJson());
            writer.flush();
        }
    }

    /**
     * private void recurse(int i, List<String> featureKeys,
     * ExperimentFactory experimentFactory,
     * Map<String, Feature> instanceFeatures,
     * ExperimentSet experimentSet, int max) {
     * if (i >= featureKeys.size()) {
     * addExperiments(instanceFeatures, experimentFactory.getFixedParameters(), experimentSet);
     * return;
     * }
     * if (experimentSet.getExperiments().size() >= max) {
     * return;
     * }
     * String featureName = featureKeys.get(i);
     * FeatureFactory featureFactory = experimentFactory.getFeatureFactories().get(featureName);
     * boolean hadContents = false;
     * List<Feature> permutations = featureFactory.permute(1000);
     * for (Feature feature : permutations) {
     * instanceFeatures.put(featureName, feature);
     * recurse(i+1, featureKeys, experimentFactory, instanceFeatures, experimentSet, max);
     * hadContents = true;
     * }
     * if (! hadContents) {
     * recurse(i+1, featureKeys, experimentFactory, instanceFeatures, experimentSet, max);
     * }
     * }
     */


    private static class GenerateConfig {
        final MODE mode;
        final int max;

        public GenerateConfig(MODE mode, int max) {
            this.mode = mode;
            this.max = max;
        }
    }


}
