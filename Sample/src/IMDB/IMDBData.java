package IMDB;

import Log.Log;
import WebKB.WebKBData;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class IMDBData {

    private static String splitPattern = "\\(|\\,|\\)\\.";

    public static class GenderGroundAtom {
        public static String predicateRaw = "gender";
        public static String predicateMale = "male";
        public static String predicateFemale = "female";

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(GenderGroundAtom.predicateRaw)) {
                String[] splitLine = line.split(splitPattern);
                String gender = splitLine[2];
                String person = splitLine[1];
                values.add(gender);
                values.add(person);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(GenderGroundAtom.predicateMale) || line.startsWith(GenderGroundAtom.predicateFemale)) {
                String[] splitLine = line.split(splitPattern);
                String gender = splitLine[0];
                String person = splitLine[1];
                values.add(gender);
                values.add(person);
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
                String gender = values.get(0);
                String person = values.get(1);
                if (!dataSet.genderGroundAtoms.containsKey(GenderGroundAtom.getKey(gender, person))) {
                    dataSet.personConstants.add(person);
                    if (gender.equals(GenderGroundAtom.predicateFemale)) {
                        GenderGroundAtom genderGroundAtom = new GenderGroundAtom();
                        genderGroundAtom.gender = gender;
                        genderGroundAtom._1_person = person;
                        dataSet.genderGroundAtoms.put(genderGroundAtom.getKey(), genderGroundAtom);
                    }
                }
            }
        }

        public String gender;
        public String _1_person;

        public String getRecursive() {
            return "r_" + getKey(gender, _1_person);
        }

        public static String getKey(String gender, String person) {
            return gender + "(" + person + ").";
        }

        public String getKey() {
            return getKey(gender, _1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    public static class ActorGroundAtom {
        public static String predicateName = "actor";

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(ActorGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String person = splitLine[1];
                values.add(person);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(ActorGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String person = splitLine[1];
                values.add(person);
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
                String person = values.get(0);
                if (!dataSet.actorGroundAtoms.containsKey(ActorGroundAtom.getKey(person))) {
                    dataSet.personConstants.add(person);
                    ActorGroundAtom actorGroundAtom = new ActorGroundAtom();
                    actorGroundAtom._1_person = person;
                    dataSet.actorGroundAtoms.put(actorGroundAtom.getKey(), actorGroundAtom);
                }
            }
        }

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    public static class DirectorGroundAtom {
        public static String predicateName = "director";

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(DirectorGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String person = splitLine[1];
                values.add(person);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(DirectorGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String person = splitLine[1];
                values.add(person);
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
                String person = values.get(0);
                if (!dataSet.directorGroundAtoms.containsKey(DirectorGroundAtom.getKey(person))) {
                    dataSet.personConstants.add(person);
                    DirectorGroundAtom directorGroundAtom = new DirectorGroundAtom();
                    directorGroundAtom._1_person = person;
                    dataSet.directorGroundAtoms.put(directorGroundAtom.getKey(), directorGroundAtom);
                }
            }
        }

        public String _1_person;

        public static String getKey(String person) {
            return predicateName + "(" + person + ").";
        }

        public String getKey() {
            return getKey(_1_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class GenreGroundAtom {
        public static String predicateName = "genre";

        public String _1_director;
        public String _2_genre;

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(GenreGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String director = splitLine[1];
                String genre = splitLine[2];
                values.add(director);
                values.add(genre);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(GenreGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String director = splitLine[1];
                String genre = splitLine[2];
                values.add(director);
                values.add(genre);
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
                String director = values.get(0);
                String genre = values.get(1);
                if (!dataSet.genreGroundAtoms.containsKey(GenreGroundAtom.getKey(director, genre))) {
                    dataSet.personConstants.add(director);
                    dataSet.genreConstants.add(genre);
                    GenreGroundAtom genreGroundAtom = new GenreGroundAtom();
                    genreGroundAtom._1_director = director;
                    genreGroundAtom._2_genre = genre;
                    dataSet.genreGroundAtoms.put(genreGroundAtom.getKey(), genreGroundAtom);
                }
            }
        }

        public static String getKey(String director, String genre) {
            return predicateName + "(" + director + "," + genre + ").";
        }

        public String getKey() {
            return getKey(_1_director, _2_genre);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class MovieGroundAtom {
        public static String predicateName = "movie";

        public String _1_movie;
        public String _2_person;

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(MovieGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String movie = splitLine[1];
                String person = splitLine[2];
                values.add(movie);
                values.add(person);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(MovieGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String movie = splitLine[1];
                String person = splitLine[2];
                values.add(movie);
                values.add(person);
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
                String movie = values.get(0);
                String person = values.get(1);
                if (!dataSet.movieGroundAtoms.containsKey(MovieGroundAtom.getKey(movie, person))) {
                    dataSet.movieConstants.add(movie);
                    dataSet.personConstants.add(person);
                    MovieGroundAtom movieGroundAtom = new MovieGroundAtom();
                    movieGroundAtom._1_movie = movie;
                    movieGroundAtom._2_person = person;
                    dataSet.movieGroundAtoms.put(movieGroundAtom.getKey(), movieGroundAtom);
                }
            }
        }

        public static String getKey(String movie, String person) {
            return predicateName + "(" + movie + "," + person + ").";
        }

        public String getKey() {
            return getKey(_1_movie, _2_person);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    private static class WorkedUnderGroundAtom {
        public static String predicateName = "workedunder";

        public String _1_actor;
        public String _2_director;

        public static ArrayList<String> parseRawLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(WorkedUnderGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String actor = splitLine[1];
                String director = splitLine[2];
                values.add(actor);
                values.add(director);
            }
            return values;
        }

        public static ArrayList<String> parseLine(String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(WorkedUnderGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String actor = splitLine[1];
                String director = splitLine[2];
                values.add(actor);
                values.add(director);
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
                String actor = values.get(0);
                String director = values.get(1);
                if (!dataSet.workedUnderGroundAtoms.containsKey(WorkedUnderGroundAtom.getKey(actor, director))) {
                    dataSet.personConstants.add(actor);
                    dataSet.personConstants.add(director);
                    WorkedUnderGroundAtom workedUnderGroundAtom = new WorkedUnderGroundAtom();
                    workedUnderGroundAtom._1_actor = actor;
                    workedUnderGroundAtom._2_director = director;
                    dataSet.workedUnderGroundAtoms.put(workedUnderGroundAtom.getKey(), workedUnderGroundAtom);
                }
            }
        }

        public static String getKey(String actor, String director) {
            return predicateName + "(" + actor + "," + director + ").";
        }

        public String getKey() {
            return getKey(_1_actor, _2_director);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    public static class DataSet {
        public HashSet<String> personConstants = new HashSet<>();
        public HashSet<String> movieConstants = new HashSet<>();
        public HashSet<String> genreConstants = new HashSet<>();

        public HashMap<String, GenderGroundAtom> genderGroundAtoms = new HashMap<>();
        public HashMap<String, ActorGroundAtom> actorGroundAtoms = new HashMap<>();
        public HashMap<String, DirectorGroundAtom> directorGroundAtoms = new HashMap<>();
        public HashMap<String, GenreGroundAtom> genreGroundAtoms = new HashMap<>();
        public HashMap<String, MovieGroundAtom> movieGroundAtoms = new HashMap<>();
        public HashMap<String, WorkedUnderGroundAtom> workedUnderGroundAtoms = new HashMap<>();

        public int getMaleSize() {
            int count = 0;
            for (GenderGroundAtom genderGroundAtom : genderGroundAtoms.values()) {
                if (genderGroundAtom.gender.equals(GenderGroundAtom.predicateMale)) {
                    count++;
                }
            }
            return count;
        }

        public int getFemaleSize() {
            int count = 0;
            for (GenderGroundAtom genderGroundAtom : genderGroundAtoms.values()) {
                if (genderGroundAtom.gender.equals(GenderGroundAtom.predicateFemale)) {
                    count++;
                }
            }
            return count;
        }
    }

    public static void printDataSetDetails(Log log, DataSet dataSet) {
        log.writeln("Person constants: " + dataSet.personConstants.size());
        log.writeln("Movie constants: " + dataSet.movieConstants.size());
        log.writeln("Genre constants: " + dataSet.genreConstants.size());

        log.writeln("Gender ground atoms: " + dataSet.genderGroundAtoms.size());
        log.writeln("Male/Female: " + dataSet.getMaleSize() + "/" + dataSet.getFemaleSize());
        log.writeln("Actor ground atoms: " + dataSet.actorGroundAtoms.size());
        log.writeln("Director ground atoms: " + dataSet.directorGroundAtoms.size());
        log.writeln("Genre ground atoms: " + dataSet.genreGroundAtoms.size());
        log.writeln("Movie ground atoms: " + dataSet.movieGroundAtoms.size());
        log.writeln("WorkedUnder ground atoms: " + dataSet.workedUnderGroundAtoms.size());
    }

    public static void printSampleDataSetDetails(Log log, DataSet dataSet, DataSet fullDataSet) {
        log.writeln("Person constants: " + dataSet.personConstants.size() + "/" + fullDataSet.personConstants.size());
        log.writeln("Movie constants: " + dataSet.movieConstants.size() + "/" + fullDataSet.movieConstants.size());
        log.writeln("Genre constants: " + dataSet.genreConstants.size() + "/" + fullDataSet.genreConstants.size());

        log.writeln("Gender ground atoms: " + dataSet.genderGroundAtoms.size() + "/" + fullDataSet.genderGroundAtoms.size());
        log.writeln("Male/Female: " + dataSet.getMaleSize() + "/" + dataSet.getFemaleSize() + " / " + fullDataSet.getMaleSize() + "/" + fullDataSet.getFemaleSize());
        log.writeln("Actor ground atoms: " + dataSet.actorGroundAtoms.size() + "/" + fullDataSet.actorGroundAtoms.size());
        log.writeln("Director ground atoms: " + dataSet.directorGroundAtoms.size() + "/" + fullDataSet.directorGroundAtoms.size());
        log.writeln("Genre ground atoms: " + dataSet.genreGroundAtoms.size() + "/" + fullDataSet.genreGroundAtoms.size());
        log.writeln("Movie ground atoms: " + dataSet.movieGroundAtoms.size() + "/" + fullDataSet.movieGroundAtoms.size());
        log.writeln("WorkedUnder ground atoms: " + dataSet.workedUnderGroundAtoms.size() + "/" + fullDataSet.workedUnderGroundAtoms.size());
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
                GenderGroundAtom.readLine(dataSet, line, isRaw);
                ActorGroundAtom.readLine(dataSet, line, isRaw);
                DirectorGroundAtom.readLine(dataSet, line, isRaw);
                GenreGroundAtom.readLine(dataSet, line, isRaw);
                MovieGroundAtom.readLine(dataSet, line, isRaw);
                WorkedUnderGroundAtom.readLine(dataSet, line, isRaw);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        fillGenderGroundAtoms(dataSet);

        // close read stream
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void fillGenderGroundAtoms(DataSet dataSet) {
        for (String person : dataSet.personConstants) {
            if (!dataSet.genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateFemale, person))) {
                GenderGroundAtom genderGroundAtom = new GenderGroundAtom();
                genderGroundAtom.gender = GenderGroundAtom.predicateMale;
                genderGroundAtom._1_person = person;
                dataSet.genderGroundAtoms.put(genderGroundAtom.getKey(), genderGroundAtom);
            }
        }
    }

    public static void fillDataSet(DataSet dataSet, DataSet fullDataSet) {
        // gender
        for (GenderGroundAtom genderGroundAtom : fullDataSet.genderGroundAtoms.values()) {
            if (dataSet.personConstants.contains(genderGroundAtom._1_person)) {
                dataSet.genderGroundAtoms.put(genderGroundAtom.getKey(), genderGroundAtom);
            }
        }

        // director
        for (DirectorGroundAtom directorGroundAtom : fullDataSet.directorGroundAtoms.values()) {
            if (dataSet.personConstants.contains(directorGroundAtom._1_person)) {
                dataSet.directorGroundAtoms.put(directorGroundAtom.getKey(), directorGroundAtom);
            }
        }

        // actor
        for (ActorGroundAtom actorGroundAtom : fullDataSet.actorGroundAtoms.values()) {
            if (dataSet.personConstants.contains(actorGroundAtom._1_person)) {
                dataSet.actorGroundAtoms.put(actorGroundAtom.getKey(), actorGroundAtom);
            }
        }

        // genre
        for (GenreGroundAtom genreGroundAtom : fullDataSet.genreGroundAtoms.values()) {
            if (dataSet.personConstants.contains(genreGroundAtom._1_director)) {
                dataSet.genreConstants.add(genreGroundAtom._2_genre);
                dataSet.genreGroundAtoms.put(genreGroundAtom.getKey(), genreGroundAtom);
            }
        }

        // movie
        for (MovieGroundAtom movieGroundAtom : fullDataSet.movieGroundAtoms.values()) {
            if (dataSet.personConstants.contains(movieGroundAtom._2_person)) {
                dataSet.movieConstants.add(movieGroundAtom._1_movie);
                dataSet.movieGroundAtoms.put(movieGroundAtom.getKey(), movieGroundAtom);
            }
        }

        // workedunder
        for (WorkedUnderGroundAtom workedUnderGroundAtom : fullDataSet.workedUnderGroundAtoms.values()) {
            if (dataSet.personConstants.contains(workedUnderGroundAtom._1_actor) &&
                    dataSet.personConstants.contains(workedUnderGroundAtom._2_director)) {
                dataSet.workedUnderGroundAtoms.put(workedUnderGroundAtom.getKey(), workedUnderGroundAtom);
            }
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
                    if (groundAtom instanceof IMDBData.GenderGroundAtom) {
                        if (((GenderGroundAtom) groundAtom).gender.equals(GenderGroundAtom.predicateFemale)) {
                            writer.write(groundAtom.toString());
                            writer.newLine();
                        }
                        writer.write(((GenderGroundAtom) groundAtom).getRecursive());
                        writer.newLine();
                    } else {
                        writer.write(groundAtom.toString());
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

        Boolean Actors = true;
        Boolean Genres = false;

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
            writer.write("graph IMDB {\n\n");

            // Director nodes
            writer.write("\t// Director nodes\n");
            writer.write("\tsubgraph directors {\n");
            writer.write("\t\tnode[shape=diamond,style=filled]\n");
            for (DirectorGroundAtom directorGroundAtom : dataSet.directorGroundAtoms.values()) {
                String attributes = "";
                String directorName = directorGroundAtom._1_person;
                if (dataSet.genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateFemale, directorName))) {
                    attributes += "fillcolor=pink";
                } else {
                    attributes += "fillcolor=lightskyblue";
                }
                writer.write("\t\t" + directorName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Actor nodes
            if (Actors) {
                writer.write("\t// Actor nodes\n");
                writer.write("\tsubgraph actors {\n");
                writer.write("\t\tnode[style=filled]\n");
                for (ActorGroundAtom actorGroundAtom : dataSet.actorGroundAtoms.values()) {
                    String attributes = "";
                    String actorName = actorGroundAtom._1_person;
                    if (dataSet.genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateFemale, actorName))) {
                        attributes += "fillcolor=pink";
                    } else {
                        attributes += "fillcolor=lightskyblue";
                    }
                    writer.write("\t\t" + actorName);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // Movie nodes
            writer.write("\t// Movie nodes\n");
            writer.write("\tsubgraph movies {\n");
            writer.write("\t\tnode[shape=box,style=filled,fillcolor=coral]\n");
            for (String movie : dataSet.movieConstants) {
                String attributes = "";
                writer.write("\t\t" + movie);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Genre nodes
            if (Genres) {
                writer.write("\t// Genre nodes\n");
                writer.write("\tsubgraph genres {\n");
                writer.write("\t\tnode[shape=parallelogram,style=filled,fillcolor=limegreen]\n");
                for (String genre : dataSet.genreConstants) {
                    String attributes = "";
                    writer.write("\t\t" + genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // movie ground atom edges
            writer.write("\t// Movie edges\n");
            for (MovieGroundAtom movieGroundAtom : dataSet.movieGroundAtoms.values()) {
                if (!Actors) {
                    String actorKey = ActorGroundAtom.getKey(movieGroundAtom._2_person);
                    ActorGroundAtom actorGroundAtom = dataSet.actorGroundAtoms.get(actorKey);
                    if (actorGroundAtom != null) continue;
                    ;
                }
                writer.write("\t" + movieGroundAtom._1_movie + "--" + movieGroundAtom._2_person + "\n");
            }
            writer.write("\n");

            // genre ground atom edges
            if (Genres) {
                writer.write("\t// Genre edges\n");
                for (GenreGroundAtom genreGroundAtom : dataSet.genreGroundAtoms.values()) {
                    writer.write("\t" + genreGroundAtom._1_director + "--" + genreGroundAtom._2_genre + "\n");
                }
                writer.write("\n");
            }

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

    public static void createSampleGraph(DataSet dataSet, DataSet sampleDataSet, String filePath) {
        Boolean Actors = true;
        Boolean Genres = true;

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
            writer.write("graph IMDB {\n\n");

            // Director nodes
            writer.write("\t// Director nodes\n");
            writer.write("\tsubgraph directors {\n");
            writer.write("\t\tnode[shape=diamond style=filled]\n");
            for (DirectorGroundAtom directorGroundAtom : dataSet.directorGroundAtoms.values()) {
                String attributes = "";
                String directorName = directorGroundAtom._1_person;
                if (sampleDataSet.directorGroundAtoms.containsKey(directorGroundAtom.getKey())) {
                    attributes += " penwidth=5";
                    if (dataSet.genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateFemale, directorName))) {
                        attributes += " fillcolor=pink";
                    } else {
                        attributes += " fillcolor=lightskyblue";
                    }
                }
                writer.write("\t\t" + directorName);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Actor nodes
            if (Actors) {
                writer.write("\t// Actor nodes\n");
                writer.write("\tsubgraph actors {\n");
                writer.write("\t\tnode[style=filled]\n");
                for (ActorGroundAtom actorGroundAtom : dataSet.actorGroundAtoms.values()) {
                    String attributes = "";
                    String actorName = actorGroundAtom._1_person;
                    if (sampleDataSet.actorGroundAtoms.containsKey(actorGroundAtom.getKey())) {
                        attributes += " penwidth=5";
                        if (dataSet.genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateFemale, actorName))) {
                            attributes += " fillcolor=pink";
                        } else {
                            attributes += " fillcolor=lightskyblue";
                        }
                    }
                    writer.write("\t\t" + actorName);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // Movie nodes
            writer.write("\t// Movie nodes\n");
            writer.write("\tsubgraph movies {\n");
            writer.write("\t\tnode[shape=box style=filled]\n");
            for (String movie : dataSet.movieConstants) {
                String attributes = "";
                if (sampleDataSet.movieConstants.contains(movie)) {
                    attributes += " fillcolor=coral";
                    attributes += " penwidth=5";
                }
                writer.write("\t\t" + movie);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\t}\n\n");

            // Genre nodes
            if (Genres) {
                writer.write("\t// Genre nodes\n");
                writer.write("\tsubgraph genres {\n");
                writer.write("\t\tnode[shape=parallelogram,style=filled]\n");
                for (String genre : dataSet.genreConstants) {
                    String attributes = "";
                    if (sampleDataSet.genreConstants.contains(genre)) {
                        attributes += " fillcolor=limegreen";
                        attributes += " penwidth=5";
                    }
                    writer.write("\t\t" + genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\t}\n\n");
            }

            // movie ground atom edges
            writer.write("\t// Movie edges\n");
            for (MovieGroundAtom movieGroundAtom : dataSet.movieGroundAtoms.values()) {
                String attributes = "";
                if (!Actors) {
                    String actorKey = ActorGroundAtom.getKey(movieGroundAtom._2_person);
                    ActorGroundAtom actorGroundAtom = dataSet.actorGroundAtoms.get(actorKey);
                    if (actorGroundAtom != null) continue;
                    ;
                }
                if (sampleDataSet.movieGroundAtoms.containsKey(movieGroundAtom.getKey())) {
                    attributes += " penwidth=5";
                }
                writer.write("\t" + movieGroundAtom._1_movie + "--" + movieGroundAtom._2_person);
                if (attributes.length() != 0) writer.write("[" + attributes + "]");
                writer.write("\n");
            }
            writer.write("\n");

            // genre ground atom edges
            if (Genres) {
                writer.write("\t// Genre edges\n");
                for (GenreGroundAtom genreGroundAtom : dataSet.genreGroundAtoms.values()) {
                    String attributes = "";
                    if (sampleDataSet.genreGroundAtoms.containsKey(genreGroundAtom.getKey())) {
                        attributes += " penwidth=5";
                    }
                    writer.write("\t" + genreGroundAtom._1_director + "--" + genreGroundAtom._2_genre);
                    if (attributes.length() != 0) writer.write("[" + attributes + "]");
                    writer.write("\n");
                }
                writer.write("\n");
            }

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

    public static DataSet sampleDataSet(DataSet dataSet, double sample) {
        DataSet sampleDataSet = new DataSet();

        // sample person constants
        ArrayList<String> persons = new ArrayList<>();
        for (String page : dataSet.personConstants) persons.add(page);
        Collections.shuffle(persons);
        int pageSampleSize = (int) Math.ceil(sample * (dataSet.personConstants.size()));
        for (int i = 0; i < pageSampleSize; i++) {
            if (persons.size() == 0) break;
            String page = persons.remove(0);
            sampleDataSet.personConstants.add(page);
        }

        // Fill dataset
        fillDataSet(sampleDataSet, dataSet);

        return sampleDataSet;
    }


}
