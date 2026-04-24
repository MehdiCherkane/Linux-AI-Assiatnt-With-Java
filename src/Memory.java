import java.io.IOException;
import java.nio.file.*;

public class Memory {
    private String pathLong = "src/LongMemoery.txt";
    private String pathShort = "src/ShortMemoey.txt";
    private Path pathToLongMemory = Path.of(pathLong);
    private Path pathToShortMemory = Path.of(pathShort);

    public void updateShorTermMemory(String memory){
        try{
            Files.write(this.pathToShortMemory, ("\n"+ memory).getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    } 

    public void updateLongTermMemory(String memory){
        try{
            Files.write(this.pathToLongMemory, ("\n-"+ memory).getBytes(), StandardOpenOption.APPEND);
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }

    }
    public String loadShortMemory(){
        String contentOfFile;
        try{
            contentOfFile = Files.readString(pathToShortMemory);
            return contentOfFile;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return null;
        }

    }
    public String loadLongMemory(){
        String contentOfFile;
        try{
            contentOfFile = Files.readString(pathToLongMemory);
            return contentOfFile;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return null;
        }

    }
    public void clearShortMemoery(){
        try{
            Files.write(pathToShortMemory, "".getBytes());
        }
        catch(IOException ioe){
        }
    }
}