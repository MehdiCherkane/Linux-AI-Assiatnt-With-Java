import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
public class CodeToolHandler implements ToolHandler{
    private Interface userInterface = new FXInterface();
    private Runner runner = new Runner();
    private String codeDirectory = "/home/mehdi-cherkane/Desktop/AI_CODE/";

    @Override 
    public String execute(JsonObject parameters){

        String filePath = parameters.get("file_path").getAsString();
        String fileContent = parameters.get("file_content").getAsString();
        // Write to file
        try {
            Files.write(Path.of(filePath), fileContent.getBytes());
            userInterface.sendOutput("Code written to file: " + filePath);
            return "Content written sucessfully to" + filePath;

        } catch (IOException e) {
            return e.getMessage();
        }
    }
}