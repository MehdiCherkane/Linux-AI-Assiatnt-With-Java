import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simple program to send an image file to the Neuon assistant API.
 * Adjust the API_ENDPOINT and AUTH_TOKEN as needed.
 */
public class ImagesHandler {
    private static final String API_ENDPOINT = "https://api.yourassistant.example.com/upload";
    private static final String AUTH_TOKEN = "YOUR_AUTH_TOKEN"; // replace with real token

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ImagesHandler <path-to-image>");
            return;
        }
        String imagePath = args[0];
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            uploadImage(imageBytes, Paths.get(imagePath).getFileName().toString());
        } catch (IOException e) {
            System.err.println("Failed to read image file: " + e.getMessage());
        }
    }

    private static void uploadImage(byte[] imageData, String fileName) throws IOException {
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("File-Name", fileName);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(imageData);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
