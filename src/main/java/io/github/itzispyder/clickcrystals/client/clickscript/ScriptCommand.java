package io.github.itzispyder.clickcrystals.client.clickscript;

public abstract class ScriptCommand {

    private final String name;

    public ScriptCommand(String name) {
        this.name = name;
    }

    public abstract void onCommand(ScriptCommand command, String line, ScriptArgs args);

    public void dispatch(ClickScript script, String cmd) {
        try {
            if (cmd != null && !cmd.isEmpty() && cmd.startsWith(name)) {
                String[] args = cmd.replaceFirst(name, "").trim().split(" ");
                onCommand(this, cmd, new ScriptArgs(args));
            }
        }
        catch (Exception ex) {
            script.printErrorDetails(ex, cmd);
        }
    }

    public String getName() {
        return name;
    }

    public static ScriptCommand create(String name, Execution execution) {
        return new ScriptCommand(name) {
            @Override
            public void onCommand(ScriptCommand command, String line, ScriptArgs args) {
                execution.execute(command, line, args);
            }
        };
    }

    @FunctionalInterface
    public interface Execution {
        void execute(ScriptCommand command, String line, ScriptArgs args);
    }
}
