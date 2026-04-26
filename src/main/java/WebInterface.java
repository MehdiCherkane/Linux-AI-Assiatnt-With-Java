import javafx.application.Platform;
import javafx.scene.web.WebEngine;

public class WebInterface extends Interface {
    private final WebEngine engine;

    public WebInterface(WebEngine engine) {
        this.engine = engine;
    }

    @Override
    public void sendOutput(String output) {
        // Properly escape the string for JavaScript execution
        String escaped = output
            .replace("\\", "\\\\")  // Backslash first
            .replace("\"", "\\\"")  // Double quotes
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "");
        Platform.runLater(() ->
            engine.executeScript("logTo('ai-out', '" + escaped + "')")
        );
    }

    
    public boolean validateComand(String command) {
        // for now just return true, I'll handle this properly later
        return true;
    }

    @Override
    public String getPrompt() {
        // not used anymore, JS pushes input to us
        return "";
    }
}