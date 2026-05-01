
public class Assistant {
    public static void main(String[] args) throws InterruptedException {
        VoiceHandler voiceHandler = new VoiceHandler();
        // String transcription = voiceHandler.recordAndTranscribe();
        // System.out.println("Transcription: " + transcription);
        // Thread.sleep(3000);
        System.out.println("Testing TTS...");
        
        boolean ok = VoiceHandler.speak("Hello, I am your assistant. How can I help you today?");
        
        if (ok) {
            System.out.println("TTS played successfully.");
        } else {
            System.err.println("TTS failed. Check error messages above.");
        }

        // System.out.println("Hello, I am your assistant. How can I help you today?");
        // JarvisFXApp.launch(JarvisFXApp.class, args);
    }
}