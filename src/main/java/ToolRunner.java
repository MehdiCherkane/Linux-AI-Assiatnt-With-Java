public class ToolRunner {
    
    private MessageBuilder messageBuilder;
    private ToolRegistry toolRegistry;
    private ResponseParser responseParser;

    public ToolRunner() {

        this.messageBuilder = new MessageBuilder();
        this.toolRegistry = new ToolRegistry();
        this.responseParser = new ResponseParser();

       
    }

    public void runTool(){

    }
    
}
