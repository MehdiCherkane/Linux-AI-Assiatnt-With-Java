import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class Memory {
    
    private String pathLong = "src/main/resources/LongMemory.txt";
    private int contextWindow = 8; // the number of last interactions to keep in short term memory
    private static ArrayList<String> shortTermMemory = new ArrayList<>();
    private Path pathToLongMemory;


    public Memory() {
        pathToLongMemory = Path.of(pathLong);
    }


    public void updateShortTermMemory(String message, String response){

        if (shortTermMemory.size() > contextWindow) {
            shortTermMemory.remove(0);
        }
        shortTermMemory.add("{role: user, content: " + message + "}");
        shortTermMemory.add("{role: assistant, content: " + response + "}");

    }
    public String loadShortMemory(){
        System.out.println(String.join(",\n", shortTermMemory));
        return String.join(",\n", shortTermMemory);
    }

    public void updateLongTermMemory(String memory){
        pathToLongMemory = Path.of(pathLong);
        try{
            Files.write(pathToLongMemory, ("\n- "+ memory).getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }

    }

    public String loadLongMemory(){
        if (!Files.exists(pathToLongMemory)) {
            return "";
        }
        try{
            return Files.readString(pathToLongMemory);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return "";
        }
    }
    public void clearShortMemory(){
        shortTermMemory.clear();
    }
}