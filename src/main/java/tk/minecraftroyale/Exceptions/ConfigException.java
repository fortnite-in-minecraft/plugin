package tk.minecraftroyale.Exceptions;

public class ConfigException extends Exception {
    private String path;

    public ConfigException(String path) {
        super("Invalid configuration field at path: " + path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
