import com.google.gson.JsonObject;

public class LongMemoryToolHandler implements ToolHandler {
    private Interface userInterface = new FXInterface();
    private Memory memory = new Memory();

    @Override
    public String execute(JsonObject parameters){
        String newMemory = parameters.get("something_to_remember").getAsString();
        memory.updateLongTermMemory(newMemory);
        userInterface.sendOutput("Long term memory updated.");
        return "Long memory updated succesfully.";
    }
}
