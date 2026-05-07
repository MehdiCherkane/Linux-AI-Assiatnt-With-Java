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

    public String ask(String userPrompt) throws Exception {
        

        JsonArray messages = new JsonArray();
        
        // 1. System message
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt.getPrompt());
        messages.add(systemMsg);
        
        // 2. Injecting history.
        for (String[] pair : memory.loadShortMemory()) {
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", pair[0]);
            messages.add(userMsg);
            
            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.addProperty("content", pair[1]);
            messages.add(assistantMsg);
        }
        
        // 3. Current user message last
        JsonObject currentMsg = new JsonObject();
        currentMsg.addProperty("role", "user");
        currentMsg.addProperty("content", userPrompt);
        messages.add(currentMsg);
        
        // 4. Build full request body
        JsonObject body = new JsonObject();
        body.addProperty("model", "llama-3.1-8b-instant");
        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();
        
        response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());
        
        return extractCommand(response.body());
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