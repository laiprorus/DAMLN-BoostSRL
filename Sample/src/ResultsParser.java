import java.io.*;
import java.util.*;

public class ResultsParser {

    private static class InferIteration {
        public int index = 0;

        public double inferAucRoc = 0.0;
        public double inferAucPr = 0.0;
        public double inferCll = 0.0;
        public int inferRuntime = 0;

        public double inferDssAucRoc = 0.0;
        public double inferDssAucPr = 0.0;
        public double inferDssCll = 0.0;
        public int inferDssRuntime = 0;
    }

    private static class SingleRunResult {
        public boolean error = false;

        public int sampleFold = 0;
        public int samplePercent = 0;
        public int sampleRun = 0;

        public int learnRuntime = 0;
        public int learnedOnTry = 0;
        public int totalLearnRuntime = 0;

        public ArrayList<InferIteration> inferIterations = new ArrayList();

        public static String getKey(int sampleFold, int sampleRun) {
            return "f" + sampleFold + "r" + sampleRun;
        }

        public String getKey() {
            return getKey(sampleFold, sampleRun);
        }

        public String toString() {
            return getKey();
        }
    }

    private static class CalculatedIntValues {
        public ArrayList<Integer> values = new ArrayList();
        public ArrayList<Integer> middleValues = new ArrayList();
        public int mean = 0;
        public int median = 0;
        public int lowerBound = 0;
        public int upperBound = 0;

        public void calculate() {
            if (values.size() == 0) return;
            // copy and sort
            middleValues = new ArrayList<>(values);
            Collections.sort(middleValues);

            // remove 10% from each end
            int toRemove = (int) Math.floor(middleValues.size() / 10);
            for (int i = 0; i < toRemove; i++) {
                middleValues.remove(0);
                middleValues.remove(middleValues.size() - 1);
            }

            // mean
            mean = 0;
            for (int value : middleValues) {
                mean += value;
            }
            mean /= middleValues.size();

            // median
            if (middleValues.size() % 2 == 0) {
                median = (middleValues.get(middleValues.size() / 2) + middleValues.get((middleValues.size() / 2) - 1)) / 2;
            } else {
                median = middleValues.get(middleValues.size() / 2);
            }

            // bounds
            lowerBound = middleValues.get(0);
            upperBound = middleValues.get(middleValues.size() - 1);
        }
    }

    private static class CalculatedDoubleValues {
        public ArrayList<Double> values = new ArrayList();
        public ArrayList<Double> middleValues = new ArrayList();
        public double mean = 0.0;
        public double median = 0.0;
        public double lowerBound = 0.0;
        public double upperBound = 0.0;

        public void calculate() {
            if (values.size() == 0) return;
            // copy and sort
            middleValues = new ArrayList<>(values);
            Collections.sort(middleValues);

            // remove 10% from each end
            int toRemove = (int) Math.floor(middleValues.size() / 10);
            for (int i = 0; i < toRemove; i++) {
                middleValues.remove(0);
                middleValues.remove(middleValues.size() - 1);
            }

            // mean
            mean = 0;
            for (double value : middleValues) {
                mean += value;
            }
            mean /= middleValues.size();

            // median
            if (middleValues.size() % 2 == 0) {
                median = (middleValues.get(middleValues.size() / 2) + middleValues.get((middleValues.size() / 2) - 1)) / 2;
            } else {
                median = middleValues.get(middleValues.size() / 2);
            }

            // bounds
            lowerBound = middleValues.get(0);
            upperBound = middleValues.get(middleValues.size() - 1);
        }
    }

    private static class SampleStepResults {
        public HashMap<String, SingleRunResult> singleRunResults = new HashMap<>();

        public int samplePercent = 0;

        public CalculatedIntValues totalLearnRuntime = new CalculatedIntValues();

        public CalculatedDoubleValues inferAucRoc = new CalculatedDoubleValues();
        public CalculatedDoubleValues inferAucPr = new CalculatedDoubleValues();
        public CalculatedDoubleValues inferCll = new CalculatedDoubleValues();
        public CalculatedIntValues inferRuntime = new CalculatedIntValues();

        public CalculatedDoubleValues inferDssAucRoc = new CalculatedDoubleValues();
        public CalculatedDoubleValues inferDssAucPr = new CalculatedDoubleValues();
        public CalculatedDoubleValues inferDssCll = new CalculatedDoubleValues();
        public CalculatedIntValues inferDssRuntime = new CalculatedIntValues();

        public static int getKey(int samplePercent) {
            return samplePercent;
        }

        public int getKey() {
            return getKey(samplePercent);
        }

        public String toString() {
            return Integer.toString(getKey());
        }
    }

    private static SortedMap<Integer, SampleStepResults> readResults(String resultsFile) {
        SortedMap<Integer, SampleStepResults> sampleResults = new TreeMap();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(resultsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("sample_loop")) {
                    // read sample percent + run and create result object
                    String[] tokens = line.replaceAll("\\s+", "").split("\\:|\\,");
                    int sampleRun = Integer.parseInt(tokens[1].substring(1));
                    int sampleFold = Integer.parseInt(tokens[2].substring(1));
                    int samplePercent = Integer.parseInt(tokens[3].substring(1));

                    // create sample step
                    SampleStepResults sampleStepResults = sampleResults.get(SampleStepResults.getKey(samplePercent));
                    if (sampleStepResults == null) {
                        sampleStepResults = new SampleStepResults();
                        sampleStepResults.samplePercent = samplePercent;
                        sampleResults.put(sampleStepResults.getKey(), sampleStepResults);
                    }

                    // create single run
                    SingleRunResult singleRunResult = sampleStepResults.singleRunResults.get(SingleRunResult.getKey(sampleFold, sampleRun));
                    if (singleRunResult == null) {
                        singleRunResult = new SingleRunResult();
                        singleRunResult.sampleFold = sampleFold;
                        singleRunResult.samplePercent = samplePercent;
                        singleRunResult.sampleRun = sampleRun;
                        sampleStepResults.singleRunResults.put(singleRunResult.getKey(), singleRunResult);
                    }

                    line = reader.readLine();
                    // read number of tries
                    if (line.startsWith("fail")) {
                        // if learning failed, skip this result
                        singleRunResult.error = true;
                        continue;
                    }

                    // read learn time
                    line = reader.readLine(); // "learn" label
                    line = reader.readLine();
                    if (!line.startsWith("empty")) {
                        // runtime
                        tokens = line.split(": ");
                        int learnRuntime = Integer.parseInt(tokens[1]);
                        singleRunResult.learnRuntime = learnRuntime;
                    } else {
                        singleRunResult.error = true;
                    }
                    // read total learn time
                    line = reader.readLine();
                    if (!line.startsWith("empty")) {
                        // try
                        tokens = line.split(": ");
                        int learnedOnTry = Integer.parseInt(tokens[1]);
                        singleRunResult.learnedOnTry = learnedOnTry;
                        // total_runtime
                        line = reader.readLine();
                        tokens = line.split(": ");
                        int totalLearnRuntime = Integer.parseInt(tokens[1]);
                        singleRunResult.totalLearnRuntime = totalLearnRuntime;
                    } else {
                        singleRunResult.error = true;
                    }

                    // read infer results
                    line = reader.readLine();
                    if (line.startsWith("iterations")) {
                        tokens = line.split(": ");
                        int iterations = Integer.parseInt(tokens[1]);
                        for (int iteration = 1; iteration <= iterations; iteration++) {
                            InferIteration inferIteration = new InferIteration();
                            inferIteration.index = iteration;
                            singleRunResult.inferIterations.add(inferIteration);
                            // infer results
                            line = reader.readLine(); // "infer" label
                            tokens = line.split(": ");
                            int inferIndex = Integer.parseInt(tokens[1]) - 1;
                            line = reader.readLine();
                            if (!line.startsWith("empty")) {
                                // auc_roc
                                tokens = line.split(": ");
                                double inferAucRoc = Double.parseDouble(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferIndex).inferAucRoc = inferAucRoc;
                                // auc_pr
                                line = reader.readLine();
                                tokens = line.split(": ");
                                double inferAucPr = Double.parseDouble(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferIndex).inferAucPr = inferAucPr;
                                // cll
                                line = reader.readLine();
                                tokens = line.split(": ");
                                double inferCll = Double.parseDouble(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferIndex).inferCll = inferCll;
                                // runtime
                                line = reader.readLine();
                                tokens = line.split(": ");
                                int inferRuntime = Integer.parseInt(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferIndex).inferRuntime = inferRuntime;
                            } else {
                                singleRunResult.error = true;
                            }
                            // infer dss results
                            line = reader.readLine(); // "infer dss" label
                            tokens = line.split(": ");
                            int inferDssIndex = Integer.parseInt(tokens[1]) - 1;
                            line = reader.readLine();
                            if (!line.startsWith("empty")) {
                                // auc_roc
                                tokens = line.split(": ");
                                double inferDssAucRoc = Double.parseDouble(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferDssIndex).inferDssAucRoc = inferDssAucRoc;
                                // auc_pr
                                line = reader.readLine();
                                tokens = line.split(": ");
                                double inferDssAucPr = Double.parseDouble(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferDssIndex).inferDssAucPr = inferDssAucPr;
                                // cll
                                line = reader.readLine();
                                tokens = line.split(": ");
                                double inferDssCll = Double.parseDouble(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferDssIndex).inferDssCll = inferDssCll;
                                // runtime
                                line = reader.readLine();
                                tokens = line.split(": ");
                                int inferDssRuntime = Integer.parseInt(tokens[1].replace(",", "."));
                                singleRunResult.inferIterations.get(inferDssIndex).inferDssRuntime = inferDssRuntime;
                            } else {
                                singleRunResult.error = true;
                            }
                        }
                    }
                    line = reader.readLine();

                    if (singleRunResult.error)
                        System.out.println("Found Error in run #" + sampleRun + " fold " + sampleFold + " sample " + samplePercent + "%");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sampleResults;
    }

    private static SortedMap<Integer, SampleStepResults> calculateValues(SortedMap<Integer, SampleStepResults> sampleResults) {

        for (SampleStepResults sampleStepResults : sampleResults.values()) {
            // sum all values
            for (SingleRunResult singleRunResult : sampleStepResults.singleRunResults.values()) {
                if (!singleRunResult.error) {
                    sampleStepResults.totalLearnRuntime.values.add(singleRunResult.totalLearnRuntime);

                    for (InferIteration inferIteration : singleRunResult.inferIterations) {
                        sampleStepResults.inferAucRoc.values.add(inferIteration.inferAucRoc);
                        sampleStepResults.inferAucPr.values.add(inferIteration.inferAucPr);
                        sampleStepResults.inferCll.values.add(inferIteration.inferCll);

                        sampleStepResults.inferDssAucRoc.values.add(inferIteration.inferDssAucRoc);
                        sampleStepResults.inferDssAucPr.values.add(inferIteration.inferDssAucPr);
                        sampleStepResults.inferDssCll.values.add(inferIteration.inferDssCll);
                    }
                } else {
                    // this run has error, ignore it for now
                }
            }

            sampleStepResults.totalLearnRuntime.calculate();

            sampleStepResults.inferAucRoc.calculate();
            sampleStepResults.inferAucPr.calculate();
            sampleStepResults.inferCll.calculate();

            sampleStepResults.inferDssAucRoc.calculate();
            sampleStepResults.inferDssAucPr.calculate();
            sampleStepResults.inferDssCll.calculate();
        }
        return sampleResults;
    }

    private static void writeResults(String parsedResultsFile, SortedMap<Integer, SampleStepResults> sampleMap) {
        try {
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter(parsedResultsFile));

            writer.newLine();
            writer.write("Learn time");
            writer.newLine();
            writer.write("Sample");
            writer.write(", mean, median, lower, upper");
            writer.newLine();

            for (int sampleStep = 5; sampleStep <= 100; sampleStep += 5) {
                SampleStepResults sampleStepResults = sampleMap.get(sampleStep);
                if (sampleStepResults != null) {
                    writer.write(sampleStepResults.samplePercent + "%");
                    writer.write(", " + sampleStepResults.totalLearnRuntime.mean + ", " + sampleStepResults.totalLearnRuntime.median + ", " + sampleStepResults.totalLearnRuntime.lowerBound + ", " + sampleStepResults.totalLearnRuntime.upperBound);
                }
                writer.newLine();
            }

            writer.newLine();
            writer.write("AUC ROC");
            writer.newLine();
            writer.write("Sample");
            writer.write(", MLN mean, MLN median, MLN lower, MLN upper");
            writer.write(", DA-MLN mean, DA-MLN median, DA-MLN lower, DA-MLN upper");
            writer.newLine();
            for (int sampleStep = 5; sampleStep <= 100; sampleStep += 5) {
                SampleStepResults sampleStepResults = sampleMap.get(sampleStep);
                if (sampleStepResults != null) {
                    writer.write(sampleStepResults.samplePercent + "%");
                    writer.write(", " + sampleStepResults.inferAucRoc.mean + ", " + sampleStepResults.inferAucRoc.median + ", " + sampleStepResults.inferAucRoc.lowerBound + ", " + sampleStepResults.inferAucRoc.upperBound);
                    writer.write(", " + sampleStepResults.inferDssAucRoc.mean + ", " + sampleStepResults.inferDssAucRoc.median + ", " + sampleStepResults.inferDssAucRoc.lowerBound + ", " + sampleStepResults.inferDssAucRoc.upperBound);
                }
                writer.newLine();
            }

            writer.newLine();
            writer.write("AUC PR");
            writer.newLine();
            writer.write("Sample");
            writer.write(", MLN mean, MLN median, MLN lower, MLN upper");
            writer.write(", DA-MLN mean, DA-MLN median, DA-MLN lower, DA-MLN upper");
            writer.newLine();
            for (int sampleStep = 5; sampleStep <= 100; sampleStep += 5) {
                SampleStepResults sampleStepResults = sampleMap.get(sampleStep);
                if (sampleStepResults != null) {
                    writer.write(sampleStepResults.samplePercent + "%");
                    writer.write(", " + sampleStepResults.inferAucPr.mean + ", " + sampleStepResults.inferAucPr.median + ", " + sampleStepResults.inferAucPr.lowerBound + ", " + sampleStepResults.inferAucPr.upperBound);
                    writer.write(", " + sampleStepResults.inferDssAucPr.mean + ", " + sampleStepResults.inferDssAucPr.median + ", " + sampleStepResults.inferDssAucPr.lowerBound + ", " + sampleStepResults.inferDssAucPr.upperBound);
                }
                writer.newLine();
            }

            writer.newLine();
            writer.write("CLL");
            writer.newLine();
            writer.write("Sample");
            writer.write(", MLN mean, MLN median, MLN lower, MLN upper");
            writer.write(", DA-MLN mean, DA-MLN median, DA-MLN lower, DA-MLN upper");
            writer.newLine();
            for (int sampleStep = 5; sampleStep <= 100; sampleStep += 5) {
                SampleStepResults sampleStepResults = sampleMap.get(sampleStep);
                if (sampleStepResults != null) {
                    writer.write(sampleStepResults.samplePercent + "%");
                    writer.write(", " + sampleStepResults.inferCll.mean + ", " + sampleStepResults.inferCll.median + ", " + sampleStepResults.inferCll.lowerBound + ", " + sampleStepResults.inferCll.upperBound);
                    writer.write(", " + sampleStepResults.inferDssCll.mean + ", " + sampleStepResults.inferDssCll.median + ", " + sampleStepResults.inferDssCll.lowerBound + ", " + sampleStepResults.inferDssCll.upperBound);
                }
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println();
        System.out.println("ResultsParser main");

        String userDir = System.getProperty("user.dir");
        String folder = "datasets/results-parser/";
        String resultsFile = userDir + "/" + folder + "results.txt";
        String parsedResultsFile = userDir + "/" + folder + "results_parsed.txt";


        System.out.println("Current directory: " + userDir);
        System.out.println("Folder: " + folder);
        System.out.println("Results file: " + resultsFile);
        System.out.println("Parsed results file: " + parsedResultsFile);

        System.out.println();
        System.out.println("Reading results... ");
        SortedMap<Integer, SampleStepResults> sampleMap = readResults(resultsFile);
        System.out.println("done!");

        System.out.println();
        System.out.println("Calculating values... ");
        sampleMap = calculateValues(sampleMap);
        System.out.println("done!");

        System.out.println();
        System.out.println("Writing results... ");
        writeResults(parsedResultsFile, sampleMap);
        System.out.println("done!");

        System.out.println();
        System.out.println("Exiting main...");
    }

}
