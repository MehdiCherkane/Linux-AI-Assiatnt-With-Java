public class ExitHandler implements IntentHandler{
    private Memory memory = new Memory();

    @Override 
    public void handle(Intent intent){
        
        memory.clearShortMemory();
        System.exit(0);
    }
}