package IMDB2;

import Log.Log;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IMDB2Parse {

    private static String userDir = "";
    private static String dbFolder = "";

    private static Log log = new Log();

    private static HashMap<String, String> personsNameMap = new HashMap<>();
    private static HashMap<String, String> directorIdMap = new HashMap<>();
    private static HashMap<String, String> actorIdMap = new HashMap<>();
    private static HashMap<String, String> movieNameMap = new HashMap<>();


    private static String getMappedPersonName(String first_name, String last_name) {
        String name = "fn:'" + first_name + "' ln:'" + last_name + "'";
        if (!personsNameMap.containsKey(name)) {
            String mappedName = "person_" + personsNameMap.size();
            personsNameMap.put(name, mappedName);
        }
        return personsNameMap.get(name);
    }

    private static String getMappedMovieName(String id) {
        return "movie_" + id;
    }

    private static void readActors(IMDB2Data.DataSet dataSet, String line) {
        Pattern pattern = Pattern.compile("^\"(?<id>[^;]+)\";\"(?<firstName>[^;]+)\";\"(?<lastName>[^;]+)\";\"(?<gender>[^;]+)\"$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String id = matcher.group("id");
            String first_name = matcher.group("firstName");
            String last_name = matcher.group("lastName");

            String actor = getMappedPersonName(first_name, last_name);

            actorIdMap.put(id, actor);
            dataSet.addActor(actor);

            String gender_raw, gender;
            gender_raw = matcher.group("gender");
            switch (gender_raw) {
                case "M":
                    gender = IMDB2Data.GenderGroundAtom.predicateMale;
                    break;
                case "F":
                    gender = IMDB2Data.GenderGroundAtom.predicateFemale;
                    break;
                default:
                    throw new Error("Unknown Gender: '" + gender_raw + "'!");
            }

            dataSet.addGender(gender, actor);
        }
    }

    private static void readDirectors(IMDB2Data.DataSet dataSet, String line) {
        Pattern pattern = Pattern.compile("^\"(?<id>[^;]+)\";\"(?<firstName>[^;]+)\";\"(?<lastName>[^;]+)\"$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String id = matcher.group("id");
            String first_name = matcher.group("firstName");
            String last_name = matcher.group("lastName");

            String director = getMappedPersonName(first_name, last_name);

            directorIdMap.put(id, director);
            dataSet.addDirector(director);
            dataSet.addGender(IMDB2Data.GenderGroundAtom.predicateMale, director);
        }
    }

    private static void readDirectorsGenres(IMDB2Data.DataSet dataSet, String line) {
        Pattern pattern = Pattern.compile("^\"(?<id>[^;]+)\";\"(?<genre>[^;]+)\";\"(?<prob>[^;]+)\"$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String id = matcher.group("id");
            String genre = matcher.group("genre");
            String prob = matcher.group("prob");

            String director = directorIdMap.get(id);
            genre = genre.toLowerCase().replaceAll("[^a-zA-Z]", "");;

            dataSet.addGenre(director, genre);
        }
    }

    private static void readMovies(IMDB2Data.DataSet dataSet, String line) {
        Pattern pattern = Pattern.compile("^\"(?<id>[^;]+)\";\"(?<name>[^;]+)\";\"(?<year>[^;]+)\";\"(?<rank>[^;]+)\"$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String id = matcher.group("id");
            String name = matcher.group("name");
            String year = matcher.group("year");
            String rank = matcher.group("rank");

            String movie = getMappedMovieName(id);
            movieNameMap.put(movie, name);
        }
    }

    private static void readMoviesDirectors(IMDB2Data.DataSet dataSet, String line) {
        Pattern pattern = Pattern.compile("^\"(?<directorId>[^;]+)\";\"(?<movieId>[^;]+)\"$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String directorId = matcher.group("directorId");
            String movieId = matcher.group("movieId");

            String director = directorIdMap.get(directorId);
            String movie = getMappedMovieName(movieId);

            dataSet.addMovie(movie, director);
        }
    }

    private static void parseLine(IMDB2Data.DataSet dataSet, String tableName, String line) {
        switch (tableName) {
            case "actors":
                readActors(dataSet, line);
                break;
            case "directors":
                readDirectors(dataSet, line);
                break;
            case "directors_genres":
                readDirectorsGenres(dataSet, line);
                break;
            case "movies":
                readMovies(dataSet, line);
                break;
            case "movies_directors":
                readMoviesDirectors(dataSet, line);
                break;
            default:
                throw new Error("Unknown Table: '" + line + "'!");
        }
    }

    private static void parseFile(IMDB2Data.DataSet dataSet, String folderPath, String tableName) {
        String filePath = folderPath + tableName + ".csv";

        // open read stream
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // read GroundAtoms
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                parseLine(dataSet, tableName, line);
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

    private static void parseDataSet(IMDB2Data.DataSet dataSet) {
        String dbPath = userDir + dbFolder;

        log.write("Parsing actors... ");
        parseFile(dataSet, dbPath, "actors");
        log.writeln("done!");

        log.write("Parsing directors... ");
        parseFile(dataSet, dbPath, "directors");
        log.writeln("done!");

        log.write("Parsing directors_genres... ");
        parseFile(dataSet, dbPath, "directors_genres");
        log.writeln("done!");

        log.write("Parsing movies... ");
        parseFile(dataSet, dbPath, "movies");
        log.writeln("done!");

        log.write("Parsing movies_directors... ");
        parseFile(dataSet, dbPath, "movies_directors");
        log.writeln("done!");

//        readDataSet(dataSet,folderPath + "movies_directors.csv",true);
//        readDataSet(dataSet,folderPath + "movies_genres.csv",true);
//        readDataSet(dataSet,folderPath + "roles.csv",true);
//        readDataSet(dataSet,folderPath + "worked_under.csv",true);
    }

    private static void writeList(List<String> list, String filePath) {
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
            for (String line : list) {
                writer.write(line);
                writer.newLine();
            }
            writer.newLine();
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

    private static void writeMaps() {
        String mapsFolder = userDir + dbFolder + "maps/";

        List<String> personsNameList = new ArrayList<String>();
        personsNameList.add("name -> person");
        for (Map.Entry<String, String> entry : personsNameMap.entrySet()) {
            String name = entry.getKey();
            String person = entry.getValue();
            personsNameList.add(name + " -> " + person);
        }
        String personsNameFile = mapsFolder + "persons_name.txt";
        writeList(personsNameList, personsNameFile);

        List<String> actorIdList = new ArrayList<String>();
        actorIdList.add("id -> actor");
        for (Map.Entry<String, String> entry : actorIdMap.entrySet()) {
            String id = entry.getKey();
            String actor = entry.getValue();
            actorIdList.add(id + " -> " + actor);
        }
        String actorIdFile = mapsFolder + "actor_id.txt";
        writeList(actorIdList, actorIdFile);

        List<String> directorIdList = new ArrayList<String>();
        directorIdList.add("id -> director");
        for (Map.Entry<String, String> entry : directorIdMap.entrySet()) {
            String id = entry.getKey();
            String director = entry.getValue();
            directorIdList.add(id + " -> " + director);
        }
        String directorIdFile = mapsFolder + "director_id.txt";
        writeList(directorIdList, directorIdFile);

        List<String> movieNameList = new ArrayList<String>();
        directorIdList.add("id -> movie");
        for (Map.Entry<String, String> entry : movieNameMap.entrySet()) {
            String id = entry.getKey();
            String movie = entry.getValue();
            movieNameList.add(id + " -> " + movie);
        }
        String movieNameFile = mapsFolder + "movie_name.txt";
        writeList(movieNameList, movieNameFile);

    }

    public static void main(String[] args) {
        try {

            // Parse args
            Options options = new Options();

            options.addOption("dbFolder", true, "path to raw database folder");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Paths
            userDir = System.getProperty("user.dir");
            dbFolder = "/" + cmd.getOptionValue("dbFolder");

            // Open logger
            log.open(userDir + dbFolder + "../parse_log.txt");
            log.writeln("");
            log.writeln("IMDB2Parse main");
            log.writeln("Current directory: " + userDir);
            log.writeln("Database Folder: " + dbFolder);

            // Parse raw database
            log.writeln("");
            log.writeln("# Parse raw database");
            IMDB2Data.DataSet fullDataSet = new IMDB2Data.DataSet();
            parseDataSet(fullDataSet);
//            IMDBData.printDataSetDetails(log, fullDataSet);
//            writeMaps();

            // Exit
            log.writeln("");
            log.writeln("Exiting main...");
            log.close();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
