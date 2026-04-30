
public class Assistant {
    public static void main(String[] args) throws InterruptedException {
        VoiceHandler voiceHandler = new VoiceHandler();
        // String transcription = voiceHandler.recordAndTranscribe();
        // System.out.println("Transcription: " + transcription);
        // Thread.sleep(3000);
        Boolean voice = voiceHandler.speak("Hello, I am your assistant. How can I help you today?");
        

        // System.out.println("Hello, I am your assistant. How can I help you today?");
        // JarvisFXApp.launch(JarvisFXApp.class, args);
    }
}