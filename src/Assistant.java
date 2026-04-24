import java.util.List;

public class Assistant {

    public static void main(String[] args) {
        // get user prompt (CLI now)  
        System.out.println("this a test");
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
            memory.updateShorTermMemory("[-Mehdi said: " + prompt + ". -Jarvis said: " + LLMresponse + ".]");
            System.out.println(LLMresponse);
            runner.run(LLMresponse);
        }
        memory.clearShortMemoery();
    }
}