public class BringHandler implements IntentHandler{

    private Runner runner = new Runner();
    private SafetyCheck safetyCheck = new SafetyCheck();
    private Interface userInterface = new FXInterface();
    private Messanger messanger = new Messanger();
    private IntentRunner intentRunner;

    public BringHandler(IntentRunner intentRunner) {
        this.intentRunner = intentRunner;
    }

    @Override
    public void handle(Intent intent) {

        String command = intent.getIntentRsponse();
        
        if (safetyCheck.isSafe(command)) {
            userInterface.sendOutput("EXECUTING "+ command);
            ProcessResult result = runner.execute(command);
            System.out.println(""" 
                SENDING:
                    Here is the command you requested to see its output: %s
                    The standard output of this command is: %s
                    The standard error of this command is: %s
                    The exit code of this command is: %d    
                    """.formatted(command,result.getStdout(), result.getStderr(), result.getExitCode()));

            String LLMresponse = messanger.getLLMrespnse("""
                    Here is the command you requested to see its output: %s
                    The standard output of this command is: %s
                    The standard error of this command is: %s
                    The exit code of this command is: %d    
                    """.formatted(command,result.getStdout(), result.getStderr(), result.getExitCode()));
            
            intentRunner.run(LLMresponse);
        }

        
    }
}
