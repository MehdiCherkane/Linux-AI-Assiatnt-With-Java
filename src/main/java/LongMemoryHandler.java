public class LongMemoryHandler implements IntentHandler {
    private Interface userInterface = new FXInterface();
    private Memory memory = new Memory();

    @Override
    public void handle(Intent intent) {
        String newMemory = intent.getIntentRsponse();
        memory.updateLongTermMemory(newMemory);
        userInterface.sendOutput("Long term memory updated.");
    }
}
