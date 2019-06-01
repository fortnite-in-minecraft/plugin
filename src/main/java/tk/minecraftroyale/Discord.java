package tk.minecraftroyale;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import sun.misc.IOUtils;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Discord implements EventListener {
    public static void main()
            throws LoginException, InterruptedException
    {
        try {
            String x = new String(Files.readAllBytes(Paths.get("token.txt")));
            System.out.println(x);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Note: It is important to register your ReadyListener before building
        JDA jda = new JDABuilder("token")
                .addEventListener(new Discord())
                .build();

        // optionally block until JDA is ready
        jda.awaitReady();
    }

    @Override
    public void onEvent(Event event)
    {
        if (event instanceof ReadyEvent)
            System.out.println("API is ready!");
    }
}
