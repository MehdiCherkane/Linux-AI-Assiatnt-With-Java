import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public class CodeHandler implements IntentHandler{
    private Interface userInterface = new FXInterface();
    private Runner runner = new Runner();
    private String codeDirectory = "/home/mehdi-cherkane/Desktop/AI_CODE/";

    @Override 
    public void handle(Intent intent){
         String filePath = handleCode(intent.getIntentRsponse());

            if (filePath != null) {
                ProcessResult result = runner.execute("code \"" + filePath + "\"");
                userInterface.sendOutput("VS Code launch exit code: " + result.getExitCode());
                if (!result.getStderr().isBlank()) userInterface.sendOutput("Code launch error: " + result.getStderr());
            }

    }
    public String handleCode(String content){

        String fileName;
        String code;

        // Always split on first newline to separate potential file name from code
        if (content.contains("\n")) {
            String[] parts = content.split("\n", 2);
            String potentialFileName = parts[0].trim();
            // Check if it looks like a file name (has extension, no invalid chars)
            if (!potentialFileName.isEmpty() && potentialFileName.contains(".") && 
                !potentialFileName.contains("/") && !potentialFileName.contains("\\")) {

                fileName = potentialFileName;
                code = parts[1].trim();
            } else {
                // Not a valid file name, treat whole as code
                fileName = "generated_code.txt";
                code = content;
                return sendFilePath(fileName);
            }
        } else {
            // No newline, treat as code only
            fileName = "generated_code.txt";
            code = content;
            return sendFilePath(fileName);
        }

        // Write to file
        try {
            Files.write(Path.of(codeDirectory + fileName), code.getBytes());
            userInterface.sendOutput("Code written to file: " + fileName);
            return sendFilePath(fileName);

        } catch (IOException e) {
            userInterface.sendOutput("Error writing to file: " + e.getMessage());
            return null;
        }
    }

    public String sendFilePath(String fileName) {
        return codeDirectory + fileName;
    }
}