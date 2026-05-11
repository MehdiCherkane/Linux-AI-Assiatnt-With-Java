import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RequestBuilder {

    private static final String MODEL = "openai/gpt-oss-120b";

    public static JsonObject build(JsonArray messages, JsonArray tools) {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("max_tokens", 1024);
        body.add("messages", messages);

        if (tools != null && tools.size() > 0) {
            body.add("tools", tools);
            body.addProperty("tool_choice", "auto");
        }

        return body;
    }
}