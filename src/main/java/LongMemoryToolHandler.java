import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LongMemoryToolHandler implements ToolHandler {
    private Interface userInterface = new FXInterface();
    private Memory memory = new Memory();
    private String path = "/home/mehdi-cherkane/Desktop/ProjectAI/Linux AI Assistant/src/main/resources/LongTermMemory.json";

    @Override
    public String execute(JsonObject parameters){

        String newMemory = parameters.get("something_to_remember").getAsString();
        String memortCategory = parameters.get("memory_category").getAsString();

        try (FileReader reader = new FileReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray array = root.getAsJsonArray(memortCategory);
            array.add("- %s".formatted(newMemory)); 
            //Write the updated root object back to the file
            FileWriter writer = new FileWriter(path);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Some error happend while writig to memory";
        }
        userInterface.sendOutput("Long term memory updated.");
        return "Long memory updated succesfully.";

    }
}
