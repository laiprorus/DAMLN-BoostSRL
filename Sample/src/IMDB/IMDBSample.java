package IMDB;

import Log.Log;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.HashMap;

public class IMDBSample {

    private static String userDir = "";
    private static String datasetFolder = "";
    private static String foldsFolder = "";

    private static Log log = new Log();

    private static void readDataSet(IMDBData.DataSet dataSet, String fileName) {
        log.write("Reading '" + fileName + "' Dataset... ");
        // read db file and create dataset
        String dbFile = userDir + foldsFolder + fileName;
        IMDBData.readDataSet(dataSet, dbFile, false);
        log.writeln("done!");
    }

    private static IMDBData.DataSet sampleDataSet(IMDBData.DataSet dataSet, double sample) {
        log.writeln("Sample value: " + sample);
        log.write("Sampling Dataset... ");
        IMDBData.DataSet sampleDataSet = IMDBData.sampleDataSet(dataSet, sample);
        log.writeln("done!");
        return sampleDataSet;
    }

    public static void writeDataSet(IMDBData.DataSet dataSet, String prefix) {
        log.writeln("Write Dataset");
        log.writeln("Data prefix: " + prefix);

        // facts
        String factsFile = userDir + datasetFolder + "/" + prefix + "/" + prefix + "_facts.txt";
        ArrayList<HashMap> facts = new ArrayList<>();
        facts.add(dataSet.genderGroundAtoms);
        facts.add(dataSet.actorGroundAtoms);
        facts.add(dataSet.directorGroundAtoms);
        facts.add(dataSet.genreGroundAtoms);
        facts.add(dataSet.movieGroundAtoms);
        facts.add(dataSet.workedUnderGroundAtoms);
        log.write("Writing '" + prefix + "_facts.txt' file...");
        IMDBData.writeGroundAtoms(facts, factsFile);
        log.writeln(" done!");

        // pos
        String posFile = userDir + datasetFolder + "/" + prefix + "/" + prefix + "_pos.txt";
        ArrayList<HashMap> pos = new ArrayList<>();
        log.write("Writing '" + prefix + "_pos.txt' file...");
        IMDBData.writeGroundAtoms(pos, posFile);
        log.writeln(" done!");

        // neg
        String negFile = userDir + datasetFolder + "/" + prefix + "/" + prefix + "_neg.txt";
        ArrayList<HashMap> neg = new ArrayList<>();
        log.write("Writing '" + prefix + "_neg.txt' file...");
        IMDBData.writeGroundAtoms(neg, negFile);
        log.writeln(" done!");
    }

    public static void createGraph(IMDBData.DataSet dataSet, String fileName) {
        String filePath = userDir + datasetFolder + "graphs/" + fileName;
        log.write("Writing '" + fileName + "' file...");
        IMDBData.createGraph(dataSet, filePath);
        log.writeln(" done!");
    }

    public static void createSampleGraph(IMDBData.DataSet dataSet, IMDBData.DataSet sampleDataSet, String fileName) {
        String filePath = userDir + datasetFolder + "graphs/" + fileName;
        log.write("Writing '" + fileName + "' file...");
        IMDBData.createSampleGraph(dataSet, sampleDataSet, filePath);
        log.writeln(" done!");
    }

    public static void main(String[] args) {
        try {

            // Parse args
            Options options = new Options();

            options.addOption("dataset", true, "path to dataset folder");
            options.addOption("foldsFolder", true, "relative path to folds folder");
            options.addOption("folds", true, "integer - number of folds");
            options.addOption("trainFold", true, "integer - index of train fold");
            options.addOption("sample", true, "double - sample ratio");
            options.addOption("combineTest", false, "combine test folds into one dataset");
            options.addOption("createGraphs", false, "create .dot graphs");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Paths
            userDir = System.getProperty("user.dir");
            datasetFolder = "/" + cmd.getOptionValue("dataset");
            foldsFolder = "/" + cmd.getOptionValue("foldsFolder");
            int folds = Integer.parseInt(cmd.getOptionValue("folds"));
            int trainFold = Integer.parseInt(cmd.getOptionValue("trainFold"));

            // Sample value
            double sample = 1.0;
            if (cmd.hasOption("sample")) {
                sample = Double.parseDouble(cmd.getOptionValue("sample"));
            }

            // Open logger
            log.open(userDir + datasetFolder + "/sample_log.txt");
            log.writeln("");
            log.writeln("WebKBSample main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Dataset path: " + datasetFolder);
            log.writeln("Folds path: " + foldsFolder);
            log.writeln("Number of folds: " + folds);
            log.writeln("Train fold index: " + trainFold);

            // constants
            String foldFilePrefix = "fold_";

            // Create full dataset
            log.writeln("");
            log.writeln("# Create full dataset");
            IMDBData.DataSet fullDataSet = new IMDBData.DataSet();
            readDataSet(fullDataSet, foldFilePrefix + "all" + ".txt");
            IMDBData.printDataSetDetails(log, fullDataSet);

            // Create train dataset
            log.writeln("");
            log.writeln("# Create train dataset");
            IMDBData.DataSet trainFullDataSet = new IMDBData.DataSet();
            readDataSet(trainFullDataSet, foldFilePrefix + trainFold + ".txt");
            IMDBData.printDataSetDetails(log, trainFullDataSet);
            log.writeln("");
            IMDBData.DataSet trainSampleDataSet = sampleDataSet(trainFullDataSet, sample);
            IMDBData.printSampleDataSetDetails(log, trainSampleDataSet, trainFullDataSet);
            log.writeln("");
            writeDataSet(trainSampleDataSet, "train");

            // Create test dataset
            log.writeln("");
            log.writeln("# Create test dataset");

            if (cmd.hasOption("combineTest")) {
                // Combine test folds into one
                IMDBData.DataSet testDataSet = new IMDBData.DataSet();
                for (int testFoldIndex = 1; testFoldIndex <= folds; testFoldIndex++) {
                    if (testFoldIndex != trainFold) {
                        readDataSet(testDataSet, foldFilePrefix + testFoldIndex + ".txt");
                    }
                }
                IMDBData.fillDataSet(testDataSet, fullDataSet);
                IMDBData.printDataSetDetails(log, testDataSet);
                log.writeln("");
                writeDataSet(testDataSet, "test_1");
            } else {
                int testIndex = 1;
                for (int testFoldIndex = 1; testFoldIndex <= folds; testFoldIndex++) {
                    if (testFoldIndex != trainFold) {
                        log.writeln("");
                        log.writeln("Iteration: " + testIndex);
                        IMDBData.DataSet testDataSet = new IMDBData.DataSet();
                        readDataSet(testDataSet, foldFilePrefix + testFoldIndex + ".txt");
                        IMDBData.printDataSetDetails(log, testDataSet);
                        writeDataSet(testDataSet, "test_" + testIndex);
                        testIndex++;
                    }
                }
            }

            // Create .dot graphs
            if (cmd.hasOption("createGraphs")) {
                log.writeln("");
                log.writeln("Creating fold graphs");
                createSampleGraph(fullDataSet, trainSampleDataSet, "sample.dot");
            }

            // Exit
            log.writeln("");
            log.writeln("Exiting main...");
            log.close();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
