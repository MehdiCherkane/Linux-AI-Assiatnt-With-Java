public class ShellHandler implements IntentHandler {

        private SafetyCheck safetyCheck = new SafetyCheck();
        private Runner runner = new Runner(); 
        private Interface userInterface = new FXInterface();

        @Override
        public void handle(Intent intent) {

            String command = intent.getIntentRsponse();
            if (safetyCheck.isInteractive(command)) interactive(command);
            else nonInteractive(command);
            
        }

        private void interactive(String command){

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
        }

        private void nonInteractive(String command){
            if (safetyCheck.isSafe(command)) {

                userInterface.sendOutput("Command to execute: " + command);
                ProcessResult result = runner.execute(command);
                userInterface.sendOutput("Exit code: " + result.getExitCode());
                if (!result.getStdout().isBlank()) userInterface.sendOutput(result.getStdout());
                if (!result.getStderr().isBlank()) userInterface.sendOutput("Error output: " + result.getStderr());

            } else {
                userInterface.sendOutput("Command to execute: " + command);
                boolean userConfirmation = userInterface.validateCommand(command);
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
