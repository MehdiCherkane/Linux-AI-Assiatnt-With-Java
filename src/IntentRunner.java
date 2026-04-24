import java.util.List;

public class IntentRunner {
    private Memory memory = new Memory();
    private Runner runner = new Runner();
    private IntentParser parser = new IntentParser();


    public void run(String response){

        List<Intent> intents = parser.parseIntents(response);
        for (Intent intent : intents) {
            System.out.println("Entent: %s -->  Content: %s".formatted(intent.getIntentType(), intent.getIntentRsponse()));
        }
        //loop and excute each intent
        for (Intent intent : intents) {
            
            if(intent.getIntentType().equals("SHELL: ")){
                runner.excute(intent.getIntentRsponse());
            }
    
            else if (intent.getIntentType().equals("YOUTUBE: ")) {
                String query = intent.getIntentRsponse();
                String url = "https://www.youtube.com/results?search_query=" 
                     + query.replace(" ", "+");
                      runner.excute("brave \"" + url + "\"");
            }

            else if (intent.getIntentType().equals("CONTEXT: ")) {
                String newMemory = intent.getIntentRsponse();
                memory.updateShorTermMemory(newMemory);

            }

            else if (intent.getIntentType().equals("REM: ")) {
                String newMemory = intent.getIntentRsponse();
                memory.updateLongTermMemory(newMemory);

            }

            else if (intent.getIntentType().equals("CHAT: ")) {
                String chatRespnse = intent.getIntentRsponse();
                System.out.println(chatRespnse);
            }
            else if (intent.getIntentType().startsWith("CODE")) {
                String code = intent.getIntentRsponse();
                System.err.println("++++++++++ Start Code +++++++");
                System.out.println(code);
                System.out.println("++++++++++ End Code +++++++++");
            }

            else if (intent.getIntentType().equals("INVALID")) {
                System.out.println("Nope");
            }
            else if (intent.getIntentType().equals("EXIT: ")) {
                System.err.println("Okay");
            }
        }
    }

}