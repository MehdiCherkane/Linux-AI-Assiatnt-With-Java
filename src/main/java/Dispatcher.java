import java.util.HashMap;

public class Dispatcher {
    private HashMap<String, IntentHandler> handlers = new HashMap<>();
    private Interface userInterface = new FXInterface();

    public Dispatcher() {
        
        registerHandler("CHAT: ", new ChatHandler());
        registerHandler("YOUTUBE: ", new YouTubeHandler());
        registerHandler("REM: ", new LongMemoryHandler());
        registerHandler("INVALID: ", new InvalidHandler());
        registerHandler("EXIT: ", new ExitHandler());
        registerHandler("CODE: ", new CodeHandler());
        // BRING handler is registered later once IntentRunner is available.
        
    }

    public void registerHandler(String intentName, IntentHandler handler) {
        handlers.put(intentName, handler);
    }

    public void dispatch(Intent intent) {
        IntentHandler handler = handlers.get(intent.getIntentType());
        if (handler != null) {
            handler.handle(intent);
        }

        else{
            // Handle unknown intent
            userInterface.sendOutput("Unkown input");
        }
    }
}
