import java.util.List;

public class IntentRunner {
    private Memory memory = new Memory();
    private Runner runner = new Runner();
    private IntentParser parser = new IntentParser();
    private Interface userInterface = new Interface();
    private SafetyCheck safetyCheck = new SafetyCheck();
    private CodeHandler codeHandler = new CodeHandler(userInterface);


    public void run(String response){

        List<Intent> intents = parser.parseIntents(response);

        //loop and excute each intent
        for (Intent intent : intents) {
            
            if(intent.getIntentType().equals("SHELL: ")){
                if (safetyCheck.isSafe(intent.getIntentRsponse())) {
                    userInterface.sendOutput("Command to excute: " + intent.getIntentRsponse());
                    runner.excute(intent.getIntentRsponse());
                } else {
                    userInterface.sendOutput("Command to excute: " + intent.getIntentRsponse());
                    boolean userConfirmation = userInterface.validateComand(intent.getIntentRsponse());
                    if (userConfirmation) {
                        runner.excute(intent.getIntentRsponse());
                    } else {
                        userInterface.sendOutput("Command execution cancelled by the user.");
                    }
                }
            }
    
            else if (intent.getIntentType().equals("YOUTUBE: ")) {
                String query = intent.getIntentRsponse();
                String url = "https://www.youtube.com/results?search_query=" 
                     + query.replace(" ", "+");
                      runner.excute("brave \"" + url + "\"");
            }

            else if (intent.getIntentType().equals("REM: ")) {
                String newMemory = intent.getIntentRsponse();
                memory.updateLongTermMemory(newMemory);

            }

            else if (intent.getIntentType().equals("CHAT: ")) {
                String chatRespnse = intent.getIntentRsponse();
                userInterface.sendOutput(chatRespnse);
            }
            // handle code.
            else if (intent.getIntentType().equals("CODE: ")) {
                String filePath = codeHandler.handleCode(intent.getIntentRsponse());

                // just to open vscode with the file, I can chane it later.
                if (filePath != null) {
                    runner.excute("code "+ filePath);
                }

            }

            else if (intent.getIntentType().equals("INVALID")) {
                userInterface.sendOutput("I can't do that Boss, Sorry :(");
            }
            else if (intent.getIntentType().equals("EXIT: ")) {
                memory.clearShortMemoery();
                userInterface.sendOutput("Goodbye!");
                System.exit(0);
            }
        }
    }

}