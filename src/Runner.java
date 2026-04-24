import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Runner {
    // I use this method in case of not using bash.
    private List<String> tokenize(String command) {
        command = "bash -c " + command;
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        boolean escaped = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (inQuotes) {
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"' || c == '\'') {
                inQuotes = true;
                quoteChar = c;
            } else if (Character.isWhitespace(c)) {
                if (current.length() > 0 || (i > 0 && (command.charAt(i-1) == '"' ||
                    command.charAt(i-1) == '\''))) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        // Add the final token if it exists
        if (current.length() > 0 || (command.length() > 0 && (command.endsWith("\"") 
            || command.endsWith("'")))) {
            tokens.add(current.toString());
        }
        tokens.replaceAll(token -> {
            if (token.startsWith("~/")) {
                return System.getProperty("user.home") + token.substring(1);
            }
            return token;
        });
        return tokens;
    }

    public String excute(String comand){
        try{
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", comand);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            return "Finished with exit code: " + exitCode;
        }
        catch(Exception e){
            e.printStackTrace();
            return "error occured";
        }

    }
}
