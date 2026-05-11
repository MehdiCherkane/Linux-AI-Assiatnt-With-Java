import java.net.http.*;
import java.net.URI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class LLMClient {

    private HttpResponse<String> response;
    private PromptV2 systemPrompt = new PromptV2();
    private Memory memory = new Memory();

    private static final String API_KEY = System.getenv("GROQ_API_KEY");

    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";

    public String ask(JsonObject body) throws Exception {
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();
        
        response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }

    // This method is for debugging.
    public HttpResponse<String> getRawResponse(){
        return response;
    }
    
   private String extractCommand(String json) {
    try {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        
        // 1. Get the "choices" array
        if (jsonObject.has("choices")) {
            // choices is a list [], so we get the first item [0]
            JsonObject firstChoice = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject();
            
            // 2. Get the "message" object inside that choice
            JsonObject message = firstChoice.getAsJsonObject("message");
            
            // 3. Finally, get the "content" string
            if (message.has("content")) {
                return message.get("content").getAsString();
            }
        }
    } catch (Exception e) {
        System.err.println("Error digging into JSON: " + e.getMessage());
        // If the API returned an error, it might be in a different field
        System.err.println("Raw body was: " + json);
    }
    
    return "";
    }
}