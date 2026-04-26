import java.util.List;

public class IntentRunner {
    private Memory memory = new Memory();
    private Runner runner;
    private IntentParser parser = new IntentParser();
    private Interface userInterface;
    private SafetyCheck safetyCheck = new SafetyCheck();
    private CodeHandler codeHandler;

    public IntentRunner() {
        this(new Interface(), new Runner(), new CodeHandler(new Interface()));
    }

    public IntentRunner(Interface userInterface, Runner runner, CodeHandler codeHandler) {
        this.userInterface = userInterface;
        this.runner = runner;
        this.codeHandler = codeHandler;
    }

    public void run(String response){

        List<Intent> intents = parser.parseIntents(response);

        //loop and excute each intent
        for (Intent intent : intents) {
            
            if(intent.getIntentType().equals("SHELL: ")){
                if (safetyCheck.isSafe(intent.getIntentRsponse())) {
                    userInterface.sendOutput("Command to execute: " + intent.getIntentRsponse());
                    ProcessResult result = runner.excute(intent.getIntentRsponse());
                    userInterface.sendOutput("Exit code: " + result.getExitCode());
                    if (!result.getStdout().isBlank()) {
                        userInterface.sendOutput(result.getStdout());
                    }
                    if (!result.getStderr().isBlank()) {
                        userInterface.sendOutput("Error output: " + result.getStderr());
                    }
                } else {
                    userInterface.sendOutput("Command to execute: " + intent.getIntentRsponse());
                    boolean userConfirmation = userInterface.validateComand(intent.getIntentRsponse());
                    if (userConfirmation) {
                        ProcessResult result = runner.excute(intent.getIntentRsponse());
                        userInterface.sendOutput("Exit code: " + result.getExitCode());
                        if (!result.getStdout().isBlank()) {
                            userInterface.sendOutput(result.getStdout());
                        }
                        if (!result.getStderr().isBlank()) {
                            userInterface.sendOutput("Error output: " + result.getStderr());
                        }
                    } else {
                        userInterface.sendOutput("Command execution cancelled by the user.");
                    }
                }
            }
    
            else if (intent.getIntentType().equals("YOUTUBE: ")) {
                String query = intent.getIntentRsponse();
                String url = "https://www.youtube.com/results?search_query=" 
                     + query.replace(" ", "+");
                ProcessResult result = runner.excute("brave \"" + url + "\"");
                userInterface.sendOutput("Browser command finished with exit code: " + result.getExitCode());
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

                if (filePath != null) {
                    ProcessResult result = runner.excute("code \"" + filePath + "\"");
                    userInterface.sendOutput("VS Code launch exit code: " + result.getExitCode());
                    if (!result.getStderr().isBlank()) {
                        userInterface.sendOutput("Code launch error: " + result.getStderr());
                    }
                }

            }

            else if (intent.getIntentType().equals("INVALID")) {
                userInterface.sendOutput("I can't do that Boss, Sorry :(");
            }
            else if (intent.getIntentType().equals("EXIT: ")) {
                memory.clearShortMemory();
                userInterface.sendOutput("Goodbye!");
                System.exit(0);
            }
        }
    }

}