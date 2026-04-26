import javafx.application.Platform;
import javafx.scene.web.WebEngine;

public class JarvisBridge {
    private Messanger messanger = new Messanger();
    private Memory memory = new Memory();
    private IntentRunner intentRunner;
    private WebEngine engine;

    public JarvisBridge(WebEngine engine) {
        this.engine = engine;
        Interface webInterface = new WebInterface(engine);
        this.intentRunner = new IntentRunner(webInterface, new Runner(), new CodeHandler(webInterface));
        
        // Log that bridge is ready
        Platform.runLater(() -> 
            engine.executeScript("logTo('ai-out', 'BRIDGE INITIALIZED')")
        );
    }

    public void sendPrompt(String prompt) {
        // Show thinking animation
        Platform.runLater(() -> 
            engine.executeScript("jarvisPulse('PROCESSING...')")
        );
        
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Processing prompt: " + prompt); // Debug
                String response = messanger.getLLMrespnse(prompt);
                System.out.println("Got response: " + response); // Debug
                memory.updateShortTermMemory(prompt, response);
                intentRunner.run(response);
            } catch (Exception e) {
                e.printStackTrace(); // Print full stack trace
                String escaped = e.getMessage()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "");
                Platform.runLater(() ->
                    engine.executeScript("logTo('ai-out', 'ERROR: " + escaped + "')")
                );
            } finally {
                Platform.runLater(() -> 
                    engine.executeScript("jarvisPulse('SYSTEM IDLE')")
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}