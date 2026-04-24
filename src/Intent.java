import java.util.List;

public class Intent {
    private String type; //"SHELL: ", "YOUTUBE: ", "CHAT: ", "INVALID: "
    private String response; // command, random chat, url ....

    public Intent(String type, String response){
        this.type = type;
        this.response = response;
    }
    public String getIntentType(){
        return this.type;
    }
    public String getIntentRsponse(){
        return this.response;
    }

}
