import java.util.Scanner;

public class Interface {
    private String prompt;
    private Scanner sc = new Scanner(System.in);

    public String getPrompt(){
            System.out.println("How can I serve you sir?");
            System.out.print("---> ");
            this.prompt = sc.nextLine();
            return prompt;
    }

}
