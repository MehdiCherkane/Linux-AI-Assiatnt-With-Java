import java.util.List;

public class IntentRunner {
    private Dispatcher dispatcher = new Dispatcher();
    private IntentParser parser = new IntentParser();
    public void run(String response){
        List<Intent> intents = parser.parseIntents(response);
        for (Intent intent : intents) {
             dispatcher.dispatch(intent);
        }
    }
}