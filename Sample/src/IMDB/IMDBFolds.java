package IMDB;

import Log.Log;
import WebKB.WebKBData;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class IMDBFolds {

    private static String userDir = "";
    private static String foldsFolder = "";

    private static Log log = new Log();

    private static IMDBData.DataSet readDataSet(String dbFileName) {
        log.writeln("");
        log.write("Reading Raw Dataset... ");

        // read db file and create dataset
        String dbFile = userDir + foldsFolder + dbFileName;
        IMDBData.DataSet dataSet = new IMDBData.DataSet();
        IMDBData.readDataSet(dataSet, dbFile, true);
        log.writeln("done!");

        IMDBData.printDataSetDetails(log, dataSet);

        return dataSet;
    }

    private static ArrayList<IMDBData.DataSet> createFolds(IMDBData.DataSet dataSet, int folds) {
        log.writeln("");
        log.write("Creating " + folds + " folds... ");

        ArrayList<IMDBData.DataSet> dataSetFolds = new ArrayList<>();

        // Create datasets
        for (int i = 0; i < folds; i++) {
            dataSetFolds.add(new IMDBData.DataSet());
        }

        // Split person constants
        ArrayList<String> persons = new ArrayList<>();
        for (String person : dataSet.personConstants) persons.add(person);
        Collections.shuffle(persons);
        for (int i = 0; i < persons.size(); i++) {
            int foldIndex = i % folds;
            dataSetFolds.get(foldIndex).personConstants.add(persons.get(i));
        }

        // Fill datasets
        for (IMDBData.DataSet dataSetFold : dataSetFolds) {
            IMDBData.fillDataSet(dataSetFold, dataSet);
        }

        log.writeln("done!");

        for (int i = 0; i < dataSetFolds.size(); i++) {
            IMDBData.DataSet dataSetFold = dataSetFolds.get(i);
            log.writeln("");
            log.writeln("Dataset fold " + (i + 1));
            IMDBData.printSampleDataSetDetails(log, dataSetFold, dataSet);
        }

        return dataSetFolds;
    }

    private static void writeFolds(IMDBData.DataSet fullDataSet, ArrayList<IMDBData.DataSet> dataSetFolds) {
        log.writeln("");
        log.writeln("Writing fold files");

        // full dataset
        {
            String fileName = "fold_all.txt";
            String filePath = userDir + foldsFolder + fileName;
            ArrayList<HashMap> facts = new ArrayList<>();
            facts.add(fullDataSet.genderGroundAtoms);
            facts.add(fullDataSet.actorGroundAtoms);
            facts.add(fullDataSet.directorGroundAtoms);
            facts.add(fullDataSet.genreGroundAtoms);
            facts.add(fullDataSet.movieGroundAtoms);
            facts.add(fullDataSet.workedUnderGroundAtoms);
            log.write("Writing '" + fileName + "' file...");
            IMDBData.writeGroundAtoms(facts, filePath);
            log.writeln(" done!");
        }

        // folds
        for (int i = 0; i < dataSetFolds.size(); i++) {
            IMDBData.DataSet dataSetFold = dataSetFolds.get(i);
            String fileName = "fold_" + (i + 1) + ".txt";
            String filePath = userDir + foldsFolder + fileName;
            ArrayList<HashMap> facts = new ArrayList<>();
            facts.add(dataSetFold.genderGroundAtoms);
            facts.add(dataSetFold.actorGroundAtoms);
            facts.add(dataSetFold.directorGroundAtoms);
            facts.add(dataSetFold.genreGroundAtoms);
            facts.add(dataSetFold.movieGroundAtoms);
            facts.add(dataSetFold.workedUnderGroundAtoms);
            log.write("Writing '" + fileName + "' file...");
            IMDBData.writeGroundAtoms(facts, filePath);
            log.writeln(" done!");
        }
    }

    public static void createGraph(IMDBData.DataSet dataSet, String fileName) {
        String filePath = userDir + foldsFolder + "graphs/" + fileName;
        log.write("Writing '" + fileName + "' file...");
        IMDBData.createGraph(dataSet, filePath);
        log.writeln(" done!");
    }

    public static void createSampleGraph(IMDBData.DataSet dataSet, IMDBData.DataSet sampleDataSet, String fileName) {
        String filePath = userDir + foldsFolder + "graphs/" + fileName;
        log.write("Writing '" + fileName + "' file...");
        IMDBData.createSampleGraph(dataSet, sampleDataSet, filePath);
        log.writeln(" done!");
    }

    public static void main(String[] args) {
        try {

            // Parse args
            Options options = new Options();

            options.addOption("foldsFolder", true, "path to folds folder");
            options.addOption("dbFile", true, "relative path to db file");
            options.addOption("folds", true, "integer - number of folds");
            options.addOption("createGraphs", false, "create .dot graphs");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Paths
            userDir = System.getProperty("user.dir");
            foldsFolder = "/" + cmd.getOptionValue("foldsFolder");
            String dbFile = cmd.getOptionValue("dbFile");
            int folds = Integer.parseInt(cmd.getOptionValue("folds"));

            // Open logger
            log.open(userDir + foldsFolder + "/folds_log.txt");
            log.writeln("");
            log.writeln("WebKBSample main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Folds folder: " + foldsFolder);
            log.writeln("DB file: " + dbFile);
            log.writeln("Number of folds: " + folds);

            // Read raw dataset
            IMDBData.DataSet fullDataSet = readDataSet(dbFile);

            // Create folds
            ArrayList<IMDBData.DataSet> dataSetFolds = createFolds(fullDataSet, folds);

            // write folds files
            writeFolds(fullDataSet, dataSetFolds);

            // Create .dot graphs
            if (cmd.hasOption("createGraphs")) {
                log.writeln("");
                log.writeln("Creating fold graphs");
                createGraph(fullDataSet, "fold_all.dot");
                for (int i = 0; i < dataSetFolds.size(); i++) {
                    IMDBData.DataSet dataSetFold = dataSetFolds.get(i);
                    String fileName = "fold_" + (i + 1) + ".dot";
                    createSampleGraph(fullDataSet, dataSetFold, fileName);
                }
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
