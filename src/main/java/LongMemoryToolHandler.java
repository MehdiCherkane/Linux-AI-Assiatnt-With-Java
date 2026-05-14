import com.google.gson.JsonObject;
public class LongMemoryToolHandler implements ToolHandler {
    private Memory memory = new Memory();
    @Override
    public String execute(JsonObject parameters) {
        String newMemory = parameters.get("something_to_remeber").getAsString();
        String memoryCategory = parameters.get("memory_category").getAsString();
        return memory.updateLongTermMemory(memoryCategory, newMemory);
    }
}
