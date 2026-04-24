import java.util.ArrayList;
import java.util.List;

public class Messanger {

    private LLMClient client = new LLMClient();

    public String getLLMrespnse(String userPrompt){
        try{
            return client.ask(userPrompt);
        }
        catch(Exception e){
            return e.toString();
        }
    }
}
