import java.util.List;

public class IntentRunner {
    
    private Dispatcher dispatcher;
    private IntentParser parser = new IntentParser();

    public IntentRunner(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void run(String response){
        List<Intent> intents = parser.parseIntents(response);
        for (Intent intent : intents) {
             dispatcher.dispatch(intent);
        }
    }
}