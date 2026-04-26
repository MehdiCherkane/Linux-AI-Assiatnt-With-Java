import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.function.Consumer;

public class GuiInterface extends Interface {
    private final Consumer<String> outputConsumer;

    public GuiInterface(Consumer<String> outputConsumer) {
        this.outputConsumer = outputConsumer;
    }

    @Override
    public void sendOutput(String output) {
        Platform.runLater(() -> outputConsumer.accept(output));
    }

    @Override
    public boolean validateComand(String command) {
        final boolean[] approved = {false};
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm command execution");
            alert.setHeaderText("Shell command validation");
            alert.setContentText("Execute this command?\n" + command);

            Optional<ButtonType> result = alert.showAndWait();
            approved[0] = result.isPresent() && result.get() == ButtonType.OK;
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return approved[0];
    }
}
