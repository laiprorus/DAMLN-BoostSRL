package WebKB;

import Log.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class WebKBData {

    private static String splitPattern = "\\(|\\,|\\)";
    private static String lettersPattern = "[^a-zA-Z]";

    public static class Category_GroundAtom {
        public static String course = "Course";
        public static String department = "Department";
        public static String faculty = "Faculty";
        public static String person = "Person";
        public static String researchproject = "ResearchProject";
        public static String staff = "Staff";
        public static String student = "Student";

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(Category_GroundAtom.course) ||
                    line.startsWith(Category_GroundAtom.department) ||
                    line.startsWith(Category_GroundAtom.faculty) ||
                    line.startsWith(Category_GroundAtom.person) ||
                    line.startsWith(Category_GroundAtom.researchproject) ||
                    line.startsWith(Category_GroundAtom.staff) ||
                    line.startsWith(Category_GroundAtom.student)
            ) {
                String[] splitLine = line.split(splitPattern);
                String category = splitLine[0].toLowerCase();
                String page = splitLine[1].replaceAll(lettersPattern, "");
                values.add(category);
                values.add(page);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(Category_GroundAtom.course.toLowerCase()) ||
                    line.startsWith(Category_GroundAtom.department.toLowerCase()) ||
                    line.startsWith(Category_GroundAtom.faculty.toLowerCase()) ||
                    line.startsWith(Category_GroundAtom.person.toLowerCase()) ||
                    line.startsWith(Category_GroundAtom.researchproject.toLowerCase()) ||
                    line.startsWith(Category_GroundAtom.staff.toLowerCase()) ||
                    line.startsWith(Category_GroundAtom.student.toLowerCase())
            ) {
                String[] splitLine = line.split(splitPattern);
                String category = splitLine[0];
                String page = splitLine[1];
                values.add(category);
                values.add(page);
            }
            return values;
        }

        public static void readLine(DataSet dataSet, String line, boolean isRaw) {
            ArrayList<String> values;
            if (isRaw) {
                values = parseRawLine(line);
            } else {
                values = parseLine(line);
            }
            if (values.size() > 0) {
                String category = values.get(0);
                String page = values.get(1);
                if (!dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey(category, page))) {
                    dataSet.page_constants.add(page);
                    Category_GroundAtom category_groundAtom = new Category_GroundAtom();
                    category_groundAtom.category = category;
                    category_groundAtom._1_page = page;
                    dataSet.category_groundAtoms.put(category_groundAtom.getKey(), category_groundAtom);
                }
            }
        }

        public String category;
        public String _1_page;

        public String getRecursive() {
            return "r_" + getKey(category, _1_page);
        }

        public static String getKey(String category, String person) {
            return category + "(" + person + ").";
        }

        public String getKey() {
            return getKey(category, _1_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    public static class Has_GroundAtom {
        public static String rawName = "Has";
        public static String predicateName = "has";

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(Has_GroundAtom.rawName)) {
                String[] splitLine = line.split(splitPattern);
                String word = splitLine[1].replaceAll(lettersPattern, "");
                String page = splitLine[2].replaceAll(lettersPattern, "");
                values.add(page);
                values.add(word);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(Has_GroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String page = splitLine[1];
                String word = splitLine[2];
                values.add(page);
                values.add(word);
            }
            return values;
        }

        public static void readLine(DataSet dataSet, String line, boolean isRaw) {
            ArrayList<String> values;
            if (isRaw) {
                values = parseRawLine(line);
            } else {
                values = parseLine(line);
            }
            if (values.size() > 0) {
                String page = values.get(0);
                String word = values.get(1);
                if (!dataSet.has_groundAtoms.containsKey(Has_GroundAtom.getKey(page, word))) {
                    dataSet.word_constants.add(word);
                    dataSet.page_constants.add(page);
                    Has_GroundAtom has_groundAtom = new Has_GroundAtom();
                    has_groundAtom._1_page = page;
                    has_groundAtom._2_word = word;
                    dataSet.has_groundAtoms.put(has_groundAtom.getKey(), has_groundAtom);
                }
            }
        }

        public String _1_page;
        public String _2_word;

        public static String getKey(String page, String word) {
            return predicateName + "(" + page + "," + word + ").";
        }

        public String getKey() {
            return getKey(_1_page, _2_word);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    public static class LinkTo_GroundAtom {
        public static String rawName = "LinkTo";
        public static String predicateName = "linkto";

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(LinkTo_GroundAtom.rawName)) {
                String[] splitLine = line.split(splitPattern);
                String page_2 = splitLine[2].replaceAll(lettersPattern, "");
                String page_1 = splitLine[3].replaceAll(lettersPattern, "");
                values.add(page_1);
                values.add(page_2);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(LinkTo_GroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String page_1 = splitLine[1];
                String page_2 = splitLine[2];
                values.add(page_1);
                values.add(page_2);
            }
            return values;
        }

        public static void readLine(DataSet dataSet, String line, boolean isRaw) {
            ArrayList<String> values;
            if (isRaw) {
                values = parseRawLine(line);
            } else {
                values = parseLine(line);
            }
            if (values.size() > 0) {
                String page_1 = values.get(0);
                String page_2 = values.get(1);
                if (!dataSet.linkTo_groundAtoms.containsKey(LinkTo_GroundAtom.getKey(page_1, page_2))) {
                    dataSet.page_constants.add(page_1);
                    dataSet.page_constants.add(page_2);
                    LinkTo_GroundAtom linkTo_groundAtom = new LinkTo_GroundAtom();
                    linkTo_groundAtom._1_page = page_1;
                    linkTo_groundAtom._2_page = page_2;
                    dataSet.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);
                }
            }
        }

        public String _1_page;
        public String _2_page;

        public static String getKey(String _1_page, String _2_page) {
            return predicateName + "(" + _1_page + "," + _2_page + ").";
        }

        public String getKey() {
            return getKey(_1_page, _2_page);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    public static class DataSet {
        public HashSet<String> page_constants = new HashSet<>();
        public HashSet<String> word_constants = new HashSet<>();

        public HashMap<String, Category_GroundAtom> category_groundAtoms = new HashMap<>();
        public HashMap<String, Has_GroundAtom> has_groundAtoms = new HashMap<>();
        public HashMap<String, LinkTo_GroundAtom> linkTo_groundAtoms = new HashMap<>();
    }

    public static void printDataSetDetails(Log log, WebKBData.DataSet dataSet) {
        log.writeln("Page constants: " + dataSet.page_constants.size());
        log.writeln("Word constants: " + dataSet.word_constants.size());

        log.writeln("Category ground atoms: " + dataSet.category_groundAtoms.size());
        log.writeln("Has ground atoms: " + dataSet.has_groundAtoms.size());
        log.writeln("LinkTo ground atoms: " + dataSet.linkTo_groundAtoms.size());
    }

    public static void printSampleDataSetDetails(Log log, WebKBData.DataSet dataSet, WebKBData.DataSet fullDataSet) {
        log.writeln("Page constants: " + dataSet.page_constants.size() + "/" + fullDataSet.page_constants.size());
        log.writeln("Word constants: " + dataSet.word_constants.size() + "/" + fullDataSet.word_constants.size());

        log.writeln("Category ground atoms: " + dataSet.category_groundAtoms.size() + "/" + fullDataSet.category_groundAtoms.size());
        log.writeln("Has ground atoms: " + dataSet.has_groundAtoms.size() + "/" + fullDataSet.has_groundAtoms.size());
        log.writeln("LinkTo ground atoms: " + dataSet.linkTo_groundAtoms.size() + "/" + fullDataSet.linkTo_groundAtoms.size());
    }

    public static void readDataSet(DataSet dataSet, String filePath, boolean isRaw) {

        // open read stream
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // read GroundAtoms
        String rawLine, line;
        try {
            while ((rawLine = reader.readLine()) != null) {
                line = rawLine.replaceAll("\\s+", "");
                Category_GroundAtom.readLine(dataSet, line, isRaw);
                Has_GroundAtom.readLine(dataSet, line, isRaw);
                LinkTo_GroundAtom.readLine(dataSet, line, isRaw);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close read stream
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeGroundAtoms(ArrayList<HashMap> groundAtomSets, String filePath) {

        // open file stream
        BufferedWriter writer = null;
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // write ground atoms
        try {
            for (HashMap groundAtoms : groundAtomSets) {
                writer.newLine();
                for (Object groundAtom : groundAtoms.values()) {
                    writer.write(groundAtom.toString());
                    writer.newLine();
                    if (groundAtom instanceof Category_GroundAtom) {
                        writer.write(((Category_GroundAtom) groundAtom).getRecursive());
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close file stream
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void createGraph(DataSet dataSet, String filePath) {

        // open file stream
        BufferedWriter writer = null;
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // write domain sizes
        try {
            writer.write("graph MyWebKB {\n\n");

            writer.write("\t// Page nodes\n");

            writer.write("\tsubgraph Pages {\n");
            writer.write("\t\tnode[style=filled label=\"\"]\n");
            for (String page : dataSet.page_constants) {
                String attributes = "";
                if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("course", page))) {
                    attributes += "fillcolor=darkgreen";
                    attributes += " tooltip=\"" + page + " (course)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("department", page))) {
                    attributes += "fillcolor=blueviolet";
                    attributes += " tooltip=\"" + page + " (department)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("faculty", page))) {
                    attributes += "fillcolor=darkorange";
                    attributes += " tooltip=\"" + page + " (faculty)" + "\"";
//                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("person", page))) {
//                    attributes += "fillcolor=blue";
//                    attributes += " tooltip=\"" + page + " (course)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("researchproject", page))) {
                    attributes += "fillcolor=brown";
                    attributes += " tooltip=\"" + page + " (researchproject)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("staff", page))) {
                    attributes += "fillcolor=chartreuse3";
                    attributes += " tooltip=\"" + page + " (staff)" + "\"";
                } else if (dataSet.category_groundAtoms.containsKey(Category_GroundAtom.getKey("student", page))) {
                    attributes += "fillcolor=cornflowerblue";
                    attributes += " tooltip=\"" + page + " (student)" + "\"";
                } else {
                    attributes += " tooltip=\"" + page + "\"";
                }
                writer.write("\t\t" + page);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            writer.write("\t// LinkTo edges\n");
            for (LinkTo_GroundAtom linkTo_groundAtom : dataSet.linkTo_groundAtoms.values()) {
                String attributes = "";
                writer.write("\t" + linkTo_groundAtom._1_page + "--" + linkTo_groundAtom._2_page);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            writer.write("}\n");

        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close file stream
        try {
            writer.close();
        } catch (
                IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void fillDataSet(DataSet dataSet, DataSet fullDataSet) {
        // category
        for (WebKBData.Category_GroundAtom category_groundAtom : fullDataSet.category_groundAtoms.values()) {
            if (dataSet.page_constants.contains(category_groundAtom._1_page)) {
                dataSet.category_groundAtoms.put(category_groundAtom.getKey(), category_groundAtom);
            }
        }
        // has
        for (WebKBData.Has_GroundAtom has_groundAtom : fullDataSet.has_groundAtoms.values()) {
            if (dataSet.page_constants.contains(has_groundAtom._1_page)) {
                dataSet.word_constants.add(has_groundAtom._2_word);
                dataSet.has_groundAtoms.put(has_groundAtom.getKey(), has_groundAtom);
            }
        }
        // linkto
        for (WebKBData.LinkTo_GroundAtom linkTo_groundAtom : fullDataSet.linkTo_groundAtoms.values()) {
            if (dataSet.page_constants.contains(linkTo_groundAtom._1_page) &&
                    dataSet.page_constants.contains(linkTo_groundAtom._2_page)) {
                dataSet.linkTo_groundAtoms.put(linkTo_groundAtom.getKey(), linkTo_groundAtom);
            }
        }
    }

    public static DataSet sampleDataSet(DataSet dataSet, double sample) {
        DataSet sampleDataSet = new DataSet();

        // sample page constants
        ArrayList<String> pages = new ArrayList<>();
        for (String page : dataSet.page_constants) pages.add(page);
        Collections.shuffle(pages);
        int pageSampleSize = (int) Math.ceil(sample * (dataSet.page_constants.size()));
        for (int i = 0; i < pageSampleSize; i++) {
            if (pages.size() == 0) break;
            String page = pages.remove(0);
            sampleDataSet.page_constants.add(page);
        }

        // Fill dataset
        fillDataSet(sampleDataSet, dataSet);

        return sampleDataSet;
    }

}
