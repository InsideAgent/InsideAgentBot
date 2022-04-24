package dev.jacrispys.JavaBot.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MessageFilter {

    /**
     *
     * @param message the message to be checked
     * @param blockedList the list of items to filter out of {@param message}
     * @return either the blocked word, or null if there is no blocked word
     */
    public static String filterMessage(String message, String... blockedList) {
        final ArrayList<String> blockedWords = new ArrayList<>(Arrays.stream(blockedList).toList());
        if(blockedWords.stream().map(String::toLowerCase).anyMatch(message::contains)) {
            return blockedWords.stream().filter(str -> message.contains(str.toLowerCase())).findFirst().get().toLowerCase(Locale.ROOT);
        }
        return null;
    }

    /**
     *
     * @param message the message to be checked
     * @param blockedList the list of items to filter out of {@param message}
     * @return either the blocked word, or null if there is no blocked word
     */
    public static String filterMessage(String message, ArrayList<String> blockedList) {
        if(blockedList.stream().map(String::toLowerCase).anyMatch(message::contains)) {
            return blockedList.stream().filter(str -> message.contains(str.toLowerCase())).findFirst().get().toLowerCase(Locale.ROOT);
        }
        return null;
    }
}
