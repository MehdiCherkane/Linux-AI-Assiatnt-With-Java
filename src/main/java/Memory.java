import java.io.IOException;
import java.nio.file.*;

public class Memory {
    
    private String pathLong = "src/main/resources/LongMemory.txt";
    private String pathShort = "src/main/resources/ShortMemory.txt";
    private Path pathToLongMemory;
    private Path pathToShortMemory;

    public Memory() {
        pathToLongMemory = Path.of(pathLong);
        pathToShortMemory = Path.of(pathShort);
    }

    public void updateShortTermMemory(String memory){

        try{
            Files.write(this.pathToShortMemory, ("\n"+ memory).getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void updateShortTermMemory(String message, String response){

        try{
            Files.write(this.pathToShortMemory, ("[-Mehdi: " + message + " -Jarvis said: " + response + "]\n").getBytes(),
             StandardOpenOption.APPEND);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void updateLongTermMemory(String memory){
        pathToLongMemory = Path.of(pathLong);
        try{
            Files.write(pathToLongMemory, ("\n-"+ memory).getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }

    }
    public String loadShortMemory(){
        if (!Files.exists(pathToShortMemory)) {
            return "";
        }
        try{
            return Files.readString(pathToShortMemory);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return "";
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
        try{
            Files.write(pathToShortMemory, "".getBytes());
        }
        catch(IOException ioe){
        }
    }
}