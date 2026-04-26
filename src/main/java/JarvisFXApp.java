import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import java.io.File;

public class JarvisFXApp extends Application {
    private Label statusLabel;
    private IntentRunner intentRunner;
    private Messanger messanger = new Messanger();
    private TextArea aiOutput;  // For internal logging if needed

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // inject the bridge AFTER the page fully loads
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                netscape.javascript.JSObject window =
                    (netscape.javascript.JSObject) webEngine.executeScript("window");
                window.setMember("java", new JarvisBridge(webEngine));
            }
        });

        File htmlFile = new File("src/main/resources/index.html");
        webEngine.load(htmlFile.toURI().toString());

        BorderPane root = new BorderPane();
        root.setCenter(webView);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Jarvis");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
