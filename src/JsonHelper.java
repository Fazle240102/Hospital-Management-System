import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading/writing lists of objects to JSON files using Gson.
 * Keeps file handling and JSON parsing in one place.
 */
public class JsonHelper {
    // Shared Gson instance (used for serialization/deserialization)
    private static final Gson gson = new Gson();

    /**
     * Load a list of objects from a JSON file.
     *
     * @param path file path (e.g., "src/data/patients.json")
     * @param type generic type information (e.g., listOf(Patient.class))
     * @param <T>  element type (Patient, Doctor, etc.)
     * @return list of objects, or empty list if file missing/empty
     */
    public static <T> List<T> loadList(String path, Type type) {
        File f = new File(path);
        if (!f.exists())
            return new ArrayList<>(); // no file → return empty list
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            List<T> data = gson.fromJson(r, type);
            return (data == null) ? new ArrayList<>() : data;
        } catch (IOException e) {
            throw new RuntimeException("Error loading data: " + e.getMessage());
        }
    }

    /**
     * Save a list of objects to a JSON file.
     *
     * @param path file path to save to
     * @param data list of objects (any type)
     */
    public static void saveList(String path, List<?> data) {
        File f = new File(path);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs(); // create parent directory if missing

        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            w.write(gson.toJson(data)); // write JSON text to file
        } catch (IOException e) {
            throw new RuntimeException("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Helper to create Type information for a List<T>.
     * Example: Type type = JsonHelper.listOf(Patient.class);
     */
    public static <T> Type listOf(Class<T> c) {
        return TypeToken.getParameterized(List.class, c).getType();
    }
}