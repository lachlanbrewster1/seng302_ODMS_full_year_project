package odms.data;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UserDataIO {

    /**
     * Export full DonorDatabase object to specified JSON file.
     *
     * @param userDb Database to be exported to JSON
     * @param path The location of the saved file
     */
    public static void saveUsers(UserDatabase userDb, String path) {
        File file = new File(path);
        try {
            Gson gson = new Gson();
            BufferedWriter writeFile = new BufferedWriter(new FileWriter(file));

            writeFile.write(gson.toJson(userDb));

            writeFile.close();

            System.out.println("File  exported successfully!");

        } catch (IOException e) {
        }
    }

    /**
     * Reads a file from the provided filename or path, converts to string.
     *
     * @param file filename or path
     * @return String of contents of provided file
     */
    private static String fileToString(File file) {
        StringBuilder fileBuffer = new StringBuilder();
        String lineBuffer;

        try {
            BufferedReader readFile = new BufferedReader(new FileReader(file));

            while ((lineBuffer = readFile.readLine()) != null) {
                fileBuffer.append(lineBuffer);
            }

            readFile.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            System.out.println("File requested: " + file);
        } catch (IOException e) {
            System.out.println("IO exception, please check the specified file");
            System.out.println("File requested: " + file);
        }

        return fileBuffer.toString();

    }

    /**
     * Load the specified DonorDatabase JSON file instantiating a DonorDatabase Object.
     * @param path The location of the saved file
     * @return DonorDatabase
     */
    public static UserDatabase loadData(String path) {
        File file = new File(path);
        UserDatabase userDb = new UserDatabase();

        try {
            Gson gson = new Gson();

            return gson.fromJson(
                    UserDataIO.fileToString(file),
                    UserDatabase.class
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return userDb;
    }


}
