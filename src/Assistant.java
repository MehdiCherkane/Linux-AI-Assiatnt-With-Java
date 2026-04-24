import java.util.List;

public class Assistant {

    public static void main(String[] args) {

        Memory memory = new Memory();
        Interface userInterface = new Interface();
        IntentRunner runner = new IntentRunner();
        Messanger msgr = new Messanger();
        while (true) {

            String prompt = userInterface.getPrompt();
            if (prompt.equals("exit")) {
                break;
            }
            String LLMresponse = msgr.getLLMrespnse(prompt);
            memory.updateShortTermMemory(prompt, LLMresponse);
            runner.run(LLMresponse);
        }

        memory.clearShortMemoery();
    }
}