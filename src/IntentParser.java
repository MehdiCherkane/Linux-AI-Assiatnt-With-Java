import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentParser {

    // 1. Pattern for Standard Markers: CHAT:, SHELL:, etc.
    // We use a "Lookahead" (?=...) to find the next marker without 'eating' it
    private static final String MARKER_NAMES = "CHAT:|YOUTUBE:|SHELL:|CONTEXT:|REM:|INVALID:|EXIT:|CODE:"; // Add more markers as needed
    private static final String MARKER_REGEX = "(" + MARKER_NAMES + ")(.*?)(?=" + MARKER_NAMES + "|$)";

    // Master Pattern now only uses markers
    private static final Pattern MASTER_PATTERN = Pattern.compile(MARKER_REGEX, Pattern.DOTALL);

    public List<Intent> parseIntents(String response) {
        List<Intent> intents = new ArrayList<>();
        Matcher matcher = MASTER_PATTERN.matcher(response);

        while (matcher.find()) {
            // All matches are now Standard Markers (Groups 1 and 2)
            if (matcher.group(1) != null) {
                String marker = matcher.group(1).trim(); // This already has the colon
                String payload = matcher.group(2).trim();
                intents.add(new Intent(marker + " ", payload));
            }
        }
        return intents;
    }
}
