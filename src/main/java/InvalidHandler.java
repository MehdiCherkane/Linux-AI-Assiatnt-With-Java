public class InvalidHandler implements IntentHandler{
    private Interface userInterface = new FXInterface();
    @Override
    public void handle(Intent intent){
        userInterface.sendOutput("I can't do that Boss, Sorry :(");
    }
}
