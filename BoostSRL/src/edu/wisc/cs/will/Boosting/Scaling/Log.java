package edu.wisc.cs.will.Boosting.Scaling;

import edu.wisc.cs.will.Utils.condor.CondorFileWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class Log {

    private BufferedWriter writer;

    public void open(String filePath) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new CondorFileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        writeln("Log stream opened: " + filePath);
    }

    public void write(String string) {
        System.out.print(string);
        try {
            writer.write(string);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void writeln(String line) {
        System.out.println(line);
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void close() {
        writeln("Log stream closing...");
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void write(String filePath, String string) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new CondorFileWriter(file));
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeln(String filePath, String string) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new CondorFileWriter(file));
            writer.write(string);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void appendWrite(String filePath, String string) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new CondorFileWriter(file, true));
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void appendWriteln(String filePath, String string) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new CondorFileWriter(file, true));
            writer.write(string);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void clearFile(String filePath) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new CondorFileWriter(file));
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
