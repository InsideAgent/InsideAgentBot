package dev.jacrispys.JavaBot.Utils;

import net.dv8tion.jda.api.JDA;

import java.io.InputStreamReader;
import java.util.Scanner;


public class CLI {

    protected boolean isEnabled = false;

    private final JDA jda;

    public CLI(JDA jda) {
        this.jda = jda;
        new Thread(() -> {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));
            while(isEnabled) {
                System.out.print("> ");
                String input = scanner.nextLine();
                switch (input) {
                    case "quit", "exit", "stop", "end" -> {
                        isEnabled = false;
                        this.jda.shutdown();
                        System.exit(0);
                    }
                    default -> System.out.println("Unknown command! Please try again, or use the 'help' command!");
                }
            }
        }).start();

    }



}
