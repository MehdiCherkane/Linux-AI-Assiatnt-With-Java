public class SafetyCheck {
    private String harmfulCommands[] = {"rm", "mv", "chmod", "chown", "dd", "mkfs", "shutdown", "reboot", "init", "poweroff", "halt", "kill", "killall", "pkill"};

    public boolean isSafe(String command) {
        String harmfulPattern = "(rm|mv|chmod|chown|dd|mkfs|shutdown|reboot|init|poweroff|halt|kill|killall|pkill)\\s+";
        return !command.matches(".*" + harmfulPattern + ".*");
    }
}
