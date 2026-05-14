import com.google.gson.JsonObject;
public class RequestMemoryToolHandler implements ToolHandler {
    private Memory memory = new Memory();
    @Override
    public String execute(JsonObject parameters){
        String requestedMemories = parameters.get("requested_memories").getAsString();
        return memory.requestMemories(requestedMemories); 
    }
    
}
