public class YouTubeHandler implements IntentHandler{
        private Interface userInterface = new FXInterface();
        private Runner runner = new Runner();
    
        @Override
        public void handle(Intent intent) {
            String query = intent.getIntentRsponse();
            String url = "https://www.youtube.com/results?search_query=" 
                    + query.replace(" ", "+");
            ProcessResult result = runner.execute("brave \"" + url + "\"");
            userInterface.sendOutput("Browser command finished with exit code: " + result.getExitCode());
        }
}
