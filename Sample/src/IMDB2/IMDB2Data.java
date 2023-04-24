package IMDB2;

import IMDB.IMDBData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class IMDB2Data {

    private static String splitPattern = "\\(|\\,|\\)\\.";

    public static class GenderGroundAtom {
        public static String predicateMale = "male";
        public static String predicateFemale = "female";

        public static void readLine(DataSet dataSet, String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(GenderGroundAtom.predicateMale) || line.startsWith(GenderGroundAtom.predicateFemale)) {
                String[] splitLine = line.split(splitPattern);
                String gender = splitLine[0];
                String person = splitLine[1];
                values.add(gender);
                values.add(person);
            }
            if (values.size() > 0) {
                String gender = values.get(0);
                String person = values.get(1);
                dataSet.addGender(gender, person);
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

        public static void readLine(DataSet dataSet, String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(ActorGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String person = splitLine[1];
                values.add(person);
            }
            if (values.size() > 0) {
                String actor = values.get(0);
                dataSet.addActor(actor);
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

        public static void readLine(DataSet dataSet, String line) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(DirectorGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String person = splitLine[1];
                values.add(person);
            }
            if (values.size() > 0) {
                String director = values.get(0);
                dataSet.addDirector(director);
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

        public static void readLine(DataSet dataSet, String line, boolean isRaw) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(GenreGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String director = splitLine[1];
                String genre = splitLine[2];
                values.add(director);
                values.add(genre);
            }
            if (values.size() > 0) {
                String director = values.get(0);
                String genre = values.get(1);
                dataSet.addGenre(director, genre);
            }
        }

        public String _1_director;
        public String _2_genre;

        public static String getKey(String director, String genre) {
            return predicateName + "(" + director + ", " + genre + ").";
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

        public static void readLine(DataSet dataSet, String line, boolean isRaw) {
            ArrayList<String> values = new ArrayList<>();
            if (line.startsWith(MovieGroundAtom.predicateName)) {
                String[] splitLine = line.split(splitPattern);
                String movie = splitLine[1];
                String person = splitLine[2];
                values.add(movie);
                values.add(person);
            }
            if (values.size() > 0) {
                String movie = values.get(0);
                String person = values.get(1);
                dataSet.addMovie(movie, person);
            }
        }

        public static String getKey(String movie, String person) {
            return predicateName + "(" + movie + ", " + person + ").";
        }

        public String getKey() {
            return getKey(_1_movie, _2_person);
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
//        public HashMap<String, WorkedUnderGroundAtom> workedUnderGroundAtoms = new HashMap<>();

        public void addActor(String actor) {
            if (!actorGroundAtoms.containsKey(ActorGroundAtom.getKey(actor))) {
                personConstants.add(actor);
                ActorGroundAtom actorGroundAtom = new ActorGroundAtom();
                actorGroundAtom._1_person = actor;
                actorGroundAtoms.put(actorGroundAtom.getKey(), actorGroundAtom);
            }
        }

        public void addDirector(String director) {
            if (!directorGroundAtoms.containsKey(DirectorGroundAtom.getKey(director))) {
                personConstants.add(director);
                DirectorGroundAtom directorGroundAtom = new DirectorGroundAtom();
                directorGroundAtom._1_person = director;
                directorGroundAtoms.put(directorGroundAtom.getKey(), directorGroundAtom);
            }
        }

        public void addGender(String gender, String person) {
            if (!genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateFemale, person)) &&
                    !genderGroundAtoms.containsKey(GenderGroundAtom.getKey(GenderGroundAtom.predicateMale, person))) {
                personConstants.add(person);
                GenderGroundAtom genderGroundAtom = new GenderGroundAtom();
                genderGroundAtom.gender = gender;
                genderGroundAtom._1_person = person;
                genderGroundAtoms.put(genderGroundAtom.getKey(), genderGroundAtom);
            }
        }

        public void addGenre(String director, String genre) {
            if (!genreGroundAtoms.containsKey(GenreGroundAtom.getKey(director, genre))) {
                personConstants.add(director);
                genreConstants.add(genre);
                GenreGroundAtom genreGroundAtom = new GenreGroundAtom();
                genreGroundAtom._1_director = director;
                genreGroundAtom._2_genre = genre;
                genreGroundAtoms.put(genreGroundAtom.getKey(), genreGroundAtom);
            }
        }

        public void addMovie(String movie, String person) {
            if (!movieGroundAtoms.containsKey(MovieGroundAtom.getKey(movie, person))) {
                movieConstants.add(movie);
                personConstants.add(person);
                MovieGroundAtom movieGroundAtom = new MovieGroundAtom();
                movieGroundAtom._1_movie = movie;
                movieGroundAtom._2_person = person;
                movieGroundAtoms.put(movieGroundAtom.getKey(), movieGroundAtom);
            }
        }

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

}
