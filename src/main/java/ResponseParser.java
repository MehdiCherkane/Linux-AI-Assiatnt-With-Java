import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class ResponseParser {

    private String stopReason;
    private String textContent;
    private JsonArray toolCalls; // raw tool_calls array if present
    private JsonObject rawMessage; // the full assistant message, for history

    public ResponseParser parse(String responseBody) {
        
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject choice = root.getAsJsonArray("choices").get(0).getAsJsonObject();

        this.stopReason = choice.get("finish_reason").getAsString();
        this.rawMessage = choice.getAsJsonObject("message");

        // Text response
        if (rawMessage.has("content") && !rawMessage.get("content").isJsonNull()) {
            this.textContent = rawMessage.get("content").getAsString();
        }

        // Tool call response
        if (rawMessage.has("tool_calls")) {
            this.toolCalls = rawMessage.getAsJsonArray("tool_calls");
        }

        return this;
    }

    public boolean isToolCall() {
        return "tool_calls".equals(stopReason);
    }

    public boolean isDone() {
        return "stop".equals(stopReason);
    }

    public String getText() { return textContent; }
    public JsonArray getToolCalls() { return toolCalls; }
    public JsonObject getRawMessage() { return rawMessage; } // needed to append to history
    
    // Helper: extract name and arguments from a single tool call
    public static String getToolName(JsonObject toolCall) {
        return toolCall.getAsJsonObject("function").get("name").getAsString();
    }

    public static JsonObject getToolArgs(JsonObject toolCall) {
        String argsString = toolCall.getAsJsonObject("function").get("arguments").getAsString();
        return JsonParser.parseString(argsString).getAsJsonObject(); // args come as a string, parse it
    }

    public static String getToolCallId(JsonObject toolCall) {
        return toolCall.get("id").getAsString();
    }
}