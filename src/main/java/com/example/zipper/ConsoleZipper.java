package com.example.zipper;

import com.example.zipper.service.ZipService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ConsoleZipper {
    private static final int EXIT = 0;
    private static final int ZIP = 1;
    private static final int UNZIP = 2;
    private static final int UNKNOWN_COMMAND = -1;

    private BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    private ZipService zipService = new ZipService();

    public void run() {
        boolean isRun = true;
        while (isRun) {
            int command = readCommand();
            switch (command) {
                case EXIT:
                    isRun = false;
                    break;
                case ZIP:
                    zip();
                    break;
                case UNZIP:
                    unzip();
                    break;
                default:
                    System.out.println("Unknown Commad");
            }
        }
    }

    public int readCommand() {
        System.out.println("Please enter " + ZIP + " for zipping files");
        System.out.println("Please enter " + UNZIP + " for unzipping zip");
        System.out.println("Please enter " + EXIT + " for exit");
        int command;
        try {
            command = Integer.parseInt(bufferedReader.readLine());
        } catch (NumberFormatException | IOException e) {
            command = UNKNOWN_COMMAND;
        }
        return command;
    }

    public void zip() {
        try {
            System.out.println("Please enter directory's and file's names separated by whitespace");
            String input = bufferedReader.readLine();
            List<String> locations = getLocations(input);
            System.out.println("Please enter zip name with .zip extention ");
            String zipName = bufferedReader.readLine();
            String zip = Paths.get(zipName).toAbsolutePath().toString();
            zipService.zip(locations, zip);
            System.out.println("Selected files were successfully archived");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unzip() {
        try {
            System.out.println("Please enter zip name .zip extention ");
            String zipName = bufferedReader.readLine();
            String zipPath = Paths.get("").resolve(zipName).toAbsolutePath().toString();
            String unZipPath = Paths.get(zipPath).getParent().toAbsolutePath().toString();
            zipService.unzip(zipPath, unZipPath);
            System.out.println("The zip was successfully unpacked");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getLocations(String fileNames) {
        String[] filesStr = fileNames.split(" ");
        List<String> files = new LinkedList<>();
        for (String fileStr : filesStr) {
            files.add(Paths.get("").resolve(fileStr).toAbsolutePath().toString());
        }
        return files;
    }

    public static void main(String[] args) {
        new ConsoleZipper().run();
    }
}
