package WebKB;

import Log.Log;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class WebKBFolds {

    private static String userDir = "";
    private static String foldsFolder = "";

    private static Log log = new Log();

    private static WebKBData.DataSet readDataSet(String dbFileName) {
        log.writeln("");
        log.write("Reading Raw Dataset... ");

        // read db file and create dataset
        String dbFile = userDir + foldsFolder + dbFileName;
        WebKBData.DataSet dataSet = new WebKBData.DataSet();
        WebKBData.readDataSet(dataSet, dbFile, true);
        log.writeln("done!");

        WebKBData.printDataSetDetails(log, dataSet);

        return dataSet;
    }

    private static ArrayList<WebKBData.DataSet> createFolds(WebKBData.DataSet dataSet, int folds) {
        log.writeln("");
        log.write("Creating " + folds + " folds... ");

        ArrayList<WebKBData.DataSet> dataSetFolds = new ArrayList<>();

        // Create datasets
        for (int i = 0; i < folds; i++) {
            dataSetFolds.add(new WebKBData.DataSet());
        }

        // Split page constants
        ArrayList<String> pages = new ArrayList<>();
        for (String page : dataSet.page_constants) pages.add(page);
        Collections.shuffle(pages);
        for (int i = 0; i < pages.size(); i++) {
            int foldIndex = i % folds;
            dataSetFolds.get(foldIndex).page_constants.add(pages.get(i));
        }

        // Fill datasets
        for (WebKBData.DataSet dataSetFold : dataSetFolds) {
            WebKBData.fillDataSet(dataSetFold, dataSet);
        }

        log.writeln("done!");

        for (int i = 0; i < dataSetFolds.size(); i++) {
            WebKBData.DataSet dataSetFold = dataSetFolds.get(i);
            log.writeln("");
            log.writeln("Dataset fold " + (i + 1));
            WebKBData.printSampleDataSetDetails(log, dataSetFold, dataSet);
        }

        return dataSetFolds;
    }

    private static void writeFolds(WebKBData.DataSet fullDataSet, ArrayList<WebKBData.DataSet> dataSetFolds) {
        log.writeln("");
        log.writeln("Writing fold files");

        // full dataset
        {
            String fileName = "fold_all.txt";
            String filePath = userDir + foldsFolder + fileName;
            ArrayList<HashMap> facts = new ArrayList<>();
            facts.add(fullDataSet.category_groundAtoms);
            facts.add(fullDataSet.has_groundAtoms);
            facts.add(fullDataSet.linkTo_groundAtoms);
            log.write("Writing '" + fileName + "' file...");
            WebKBData.writeGroundAtoms(facts, filePath);
            log.writeln(" done!");
        }

        // folds
        for (int i = 0; i < dataSetFolds.size(); i++) {
            WebKBData.DataSet dataSetFold = dataSetFolds.get(i);
            String fileName = "fold_" + (i + 1) + ".txt";
            String filePath = userDir + foldsFolder + fileName;
            ArrayList<HashMap> facts = new ArrayList<>();
            facts.add(dataSetFold.category_groundAtoms);
            facts.add(dataSetFold.has_groundAtoms);
            facts.add(dataSetFold.linkTo_groundAtoms);
            log.write("Writing '" + fileName + "' file...");
            WebKBData.writeGroundAtoms(facts, filePath);
            log.writeln(" done!");
        }
    }

    public static void createGraph(WebKBData.DataSet dataSet, String fileName) {
        String filePath = userDir + foldsFolder + "graphs/" + fileName;
        log.write("Writing '" + fileName + "' file...");
        WebKBData.createGraph(dataSet, filePath);
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
            WebKBData.DataSet fullDataSet = readDataSet(dbFile);

            // Create folds
            ArrayList<WebKBData.DataSet> dataSetFolds = createFolds(fullDataSet, folds);

            // write folds files
            writeFolds(fullDataSet, dataSetFolds);

            // Create .dot graphs
            if (cmd.hasOption("createGraphs")) {
                log.writeln("");
                log.writeln("Creating fold graphs");
                createGraph(fullDataSet, "fold_all.dot");
                for (int i = 0; i < dataSetFolds.size(); i++) {
                    WebKBData.DataSet dataSetFold = dataSetFolds.get(i);
                    String fileName = "fold_" + (i + 1) + ".dot";
                    createGraph(dataSetFold, fileName);
                }
            }

            int count = 0;
            for (WebKBData.Category_GroundAtom category_groundAtom : fullDataSet.category_groundAtoms.values()) {
                if (category_groundAtom.category.equals(WebKBData.Category_GroundAtom.faculty.toLowerCase())) count++;
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
