import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentParser {

    // 1. Pattern for Code Blocks: ```language code ```
    private static final String CODE_REGEX = "```(\\w+)\\s+(.*?)\\s*```";
    
    // 2. Pattern for Standard Markers: CHAT:, SHELL:, etc.
    // We use a "Lookahead" (?=...) to find the next marker without 'eating' it
    private static final String MARKER_NAMES = "CHAT:|YOUTUBE:|SHELL:|CONTEXT:|REM:|INVALID:|EXIT:";
    private static final String MARKER_REGEX = "(" + MARKER_NAMES + ")(.*?)(?=" + MARKER_NAMES + "|```|$)";

    // Combine both into one "Master Pattern" using the OR (|) operator
    private static final Pattern MASTER_PATTERN = Pattern.compile(CODE_REGEX + "|" + MARKER_REGEX, Pattern.DOTALL);

    public List<Intent> parseIntents(String response) {
        List<Intent> intents = new ArrayList<>();
        Matcher matcher = MASTER_PATTERN.matcher(response);

        while (matcher.find()) {
            // Check if we found a Code Block (Groups 1 and 2)
            if (matcher.group(1) != null) {
                String lang = matcher.group(1).toUpperCase();
                String code = matcher.group(2).trim();
                // Formatting to match your "INTENT: " requirement
                intents.add(new Intent("CODE: " + lang , code));
            } 
            // Otherwise, it's a Standard Marker (Groups 3 and 4)
            else if (matcher.group(3) != null) {
                String marker = matcher.group(3).trim(); // This already has the colon
                String payload = matcher.group(4).trim();
                intents.add(new Intent(marker + " ", payload));
            }
        }
        return intents;
    }
}
