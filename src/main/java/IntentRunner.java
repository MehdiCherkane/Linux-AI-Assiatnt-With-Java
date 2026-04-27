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
            // this hanles the shell command intent, it will check if the command is interactive or not and execute it accordingly.
            if (intent.getIntentType().equals("SHELL: ")) {
                String command = intent.getIntentRsponse();
                if (safetyCheck.isInteractive(command)) {
                    // Launch interactive session
                    userInterface.startInteractive(command, runner);
                    runner.executeInteractive(command, new ProcessHandler() {
                        @Override
                        public void onOutput(String line) {
                            userInterface.sendOutput("[stdout] " + line);
                        }
                        @Override
                        public void onError(String line) {
                            userInterface.sendOutput("[stderr] " + line);
                        }
                        @Override
                        public void onExit(int exitCode) {
                            userInterface.sendOutput("Interactive process finished with exit code " + exitCode);
                            userInterface.endInteractive();
                        }
                    });
                } else {
                    // Original non‑interactive flow (with safety check)
                    if (safetyCheck.isSafe(command)) {
                        userInterface.sendOutput("Command to execute: " + command);
                        ProcessResult result = runner.execute(command);
                        userInterface.sendOutput("Exit code: " + result.getExitCode());
                        if (!result.getStdout().isBlank()) userInterface.sendOutput(result.getStdout());
                        if (!result.getStderr().isBlank()) userInterface.sendOutput("Error output: " + result.getStderr());
                    } else {
                        userInterface.sendOutput("Command to execute: " + command);
                        boolean userConfirmation = userInterface.validateComand(command);
                        if (userConfirmation) {
                            ProcessResult result = runner.execute(command);
                            userInterface.sendOutput("Exit code: " + result.getExitCode());
                            if (!result.getStdout().isBlank()) userInterface.sendOutput(result.getStdout());
                            if (!result.getStderr().isBlank()) userInterface.sendOutput("Error output: " + result.getStderr());
                        } else {
                            userInterface.sendOutput("Command execution cancelled by the user.");
                        }
                    }
                }
            }

            // this handles the youtube intent, it will open the youtube search results for the given query in the default browser.
            else if (intent.getIntentType().equals("YOUTUBE: ")) {
                String query = intent.getIntentRsponse();
                String url = "https://www.youtube.com/results?search_query=" 
                     + query.replace(" ", "+");
                ProcessResult result = runner.execute("brave \"" + url + "\"");
                userInterface.sendOutput("Browser command finished with exit code: " + result.getExitCode());
            }

            // this handles the REM intent, it will update the long term memory with the given information.
            else if (intent.getIntentType().equals("REM: ")) {
                String newMemory = intent.getIntentRsponse();
                memory.updateLongTermMemory(newMemory);

            }
            // this handles the CHAT intent, it will just send the response to the user interface to be displayed to the user.
            else if (intent.getIntentType().equals("CHAT: ")) {
                String chatRespnse = intent.getIntentRsponse();
                userInterface.sendOutput(chatRespnse);
            }

            // handle code.
            else if (intent.getIntentType().equals("CODE: ")) {
                String filePath = codeHandler.handleCode(intent.getIntentRsponse());

                if (filePath != null) {
                    ProcessResult result = runner.execute("code \"" + filePath + "\"");
                    userInterface.sendOutput("VS Code launch exit code: " + result.getExitCode());
                    if (!result.getStderr().isBlank()) {
                        userInterface.sendOutput("Code launch error: " + result.getStderr());
                    }
                }

            }

            // handle invalid intent.
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