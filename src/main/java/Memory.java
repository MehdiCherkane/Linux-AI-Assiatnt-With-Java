import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class Memory {
    
    private int contextWindow = 8; // the number of last interactions to keep in short term memory
    private static ArrayList<String[]> shortTermMemory = new ArrayList<>();

    // add new pair of (userPrompt, LLMresponse) with respect to context window.
    public void updateShortTermMemory(String message, String response){
        if (shortTermMemory.size() >= contextWindow) {
            shortTermMemory.remove(0);
        }
        shortTermMemory.add(new String[]{message, response});
    }

    // get short memory (it will be sent to the LLM)
    public ArrayList<String[]> loadShortMemory(){
        return shortTermMemory;
    }

    // clearing our memory intetionally (even if don't close program)
    public void clearShortMemory(){
        shortTermMemory.clear();
    }
}