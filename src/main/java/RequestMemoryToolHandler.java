import java.io.FileReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RequestMemoryToolHandler implements ToolHandler {
    @Override
    public String execute(JsonObject parameters){

        String memory = parameters.get("requested_memories").getAsString();
        StringBuilder requestedMemories = new StringBuilder();

        try (FileReader reader = new FileReader("/home/mehdi-cherkane/Desktop/ProjectAI/Linux AI Assistant/src/main/resources/LongTermMemory.json")) {

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray array = root.getAsJsonArray(memory);
            for (JsonElement element : array) {
                requestedMemories.append(element);
                requestedMemories.append("\n");
            }
            return requestedMemories.toString();
        } 
        catch (Exception e) {
            e.printStackTrace();
            return "Some error happend while retreiving memory for!!";
        }
    }

}
