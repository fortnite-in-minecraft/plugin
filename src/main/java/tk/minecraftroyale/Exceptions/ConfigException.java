package tk.minecraftroyale.Exceptions;

public class ConfigException extends RuntimeException {
    public ConfigException(String path) {
        super("Invalid configuration field at path: " + path);
    }
}
