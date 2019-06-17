package tk.minecraftroyale.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that a class is a command. It also provides the command's name and whether or not it should
 * only be accessible to users who have explicitly enabled development commands.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
    String value();
    boolean playerOnly() default false;
    boolean development() default false;
}
