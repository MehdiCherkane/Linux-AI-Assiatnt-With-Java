import com.google.gson.*;
public class Messanger {

    private ToolRegistry toolRegistry = new ToolRegistry();
    private LLMClient client = new LLMClient();
    private Memory memory = new Memory();
    private PromptV2 sysPrompt = new PromptV2();
    private FXInterface userInterface = new FXInterface();
    private ToolRunner toolRunner;

    public Messanger() {

        toolRegistry.register(
            new ToolDefinition("run_shell", "Execute a shell command")
                .addParameter("command", "string", true))
            .register(new ToolDefinition("write_code", "for writing code").addParameter("file_name", "string", true)
                .addParameter("file_content", "string", true))
            .register(new ToolDefinition("find_on_youtube", "the search query for YouTube")
                .addParameter("search_query", "string", true))
            .register(new ToolDefinition("update_long_term_memory", "when you recognize something that worths being remembered for long time")
                .addParameter("memory_category", "string", true)
                .addParameter("something_to_remeber", "string", true))
            .register(new ToolDefinition("exit_Neuon", "when you understand that I told to exit or end session...")
                .addParameter("exit_messege", "string", false))
            .register(new ToolDefinition("request_memories", "when you need some information/memory").addParameter("requested_memories", "string", true))
            .register(new ToolDefinition("read_file", "when you need to read the content of a file")
                .addParameter("file_path", "string", true));
        toolRunner = new ToolRunner();
    }

    public String getLLMResponse(String userPrompt) {
        try {
            return runConversation(userPrompt);
        } catch (Exception e) {
            return "[ERROR] " + e.toString();
        }
    }

    private String runConversation(String userPrompt) throws Exception {

        MessageBuilder messageBuilder = new MessageBuilder(); // fresh every call
        JsonArray tools = toolRegistry.toJson();

        messageBuilder.addSystem(sysPrompt.getPrompt());

        for (String[] pair : memory.loadShortMemory()) {
            messageBuilder.addUser(pair[0]);
            messageBuilder.addAssistant(pair[1]);
        }

        messageBuilder.addUser(userPrompt);

        int maxSteps = 10;
        int steps = 0;

        while (steps++ < maxSteps) {
            userInterface.sendOutput("loop run %d times".formatted(steps));

            JsonObject body = RequestBuilder.build(messageBuilder.build(), tools);
            String raw = client.ask(body);
            ResponseParser parser = new ResponseParser().parse(raw);

            messageBuilder.addRaw(parser.getRawMessage());

            if (parser.isDone()) {
                return parser.getText();
            }

            if (parser.isToolCall()) {
                
                JsonArray toolCalls = parser.getToolCalls();
                for (int i = 0; i < toolCalls.size(); i++) {
                    JsonObject toolCall = toolCalls.get(i).getAsJsonObject();
                    String name = ResponseParser.getToolName(toolCall);
                    JsonObject args = ResponseParser.getToolArgs(toolCall);
                    String id = ResponseParser.getToolCallId(toolCall);
                    String result = toolRunner.execute(name, args);
                    messageBuilder.addToolResult(id, result);
                }
                // loop continues, sends results back to LLM
            }
        }

        return "[ERROR] max steps reached";
    }
}
