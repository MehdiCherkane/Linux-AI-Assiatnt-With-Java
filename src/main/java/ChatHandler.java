public class ChatHandler implements IntentHandler {
    private Interface userInterface = new FXInterface();
    private VoiceHandler voiceHandler = new VoiceHandler();

    @Override
    public void handle(Intent intent) {
        String chatRespnse = intent.getIntentRsponse();
        userInterface.sendOutput(chatRespnse);
        voiceHandler.speak(chatRespnse);
    }
    
}
