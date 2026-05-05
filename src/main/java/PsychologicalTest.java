import java.util.Scanner;

public class PsychologicalTest{
    private static final String[] HAPPY_OUTCOMES = {"Have you ever felt like you're just going through the motions of life?", "Are you feeling empty inside?", "Do you feel unfulfilled in your relationships?"};
    private static final String[] BROKEN_OUTCOMES = {"Have you ever felt like you're losing control?", "Do you often feel anxious or on edge?"};
    private static final String[] HUMANITIES_OUTCOMES = {"Have you ever felt like you're just a number in a big machine?"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the psychological test! Please answer the following questions with 'yes' or 'no'.");

        boolean happiness = testHappiness(scanner);
        boolean brokenness = testBrokenness(scanner);
        boolean humanity = testHumanity(scanner);

        if (happiness) {
            printOutcome(HAPPY_OUTCOMES);
            System.out.println("You appear to be happy.");
        } else if (brokenness) {
            printOutcome(BROKEN_OUTCOMES);
            System.out.println("You appear to be broken.");
        } else {
            printOutcome(HUMANITIES_OUTCOMES);
            System.out.println("You appear to be disconnected from humanity.");
        }

        System.out.println("\nThank you for participating in the test!");
    }

    private static boolean testHappiness(Scanner scanner) {
        System.out.println("Do you feel a sense of purpose in life?");
        if (scanner.next().equals("no")) {
            System.out.println("Do you feel like you're just going through the motions of life?");
            if (scanner.next().equals("yes")) {
                return false;
            }
        }
        System.out.println("Do you feel happy when you're around other people?");
        if (scanner.next().equals("no")) {
            return false;
        }
        System.out.println("Do you feel like you're living the life you want?");
        if (scanner.next().equals("no")) {
            return false;
        }
        return true;
    }

    private static boolean testBrokenness(Scanner scanner) {
        System.out.println("Do you feel like you're losing control?");
        if (scanner.next().equals("yes")) {
            return true;
        }
        System.out.println("Do you often feel anxious or on edge?");
        if (scanner.next().equals("yes")) {
            System.out.println("Do you feel like you're just a shell of your former self?");
            if (scanner.next().equals("yes")) {
                return true;
            }
        }
        return false;
    }

    private static boolean testHumanity(Scanner scanner) {
        System.out.println("Do you feel like you're just a number in a big machine?");
        if (scanner.next().equals("yes")) {
            System.out.println("Do you feel like you're being treated like nothing more than a object?");
            if (scanner.next().equals("yes")) {
                return true;
            }
        }
        return false;
    }

    private static void printOutcome(String[] outcomes) {
        for (String outcome : outcomes) {
            System.out.println(outcome);
        }
    }
}