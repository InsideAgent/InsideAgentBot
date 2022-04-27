package dev.jacrispys.JavaBot.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public enum BlockedWords {
    CURSES("FUCK","SHIT", "NIGGER"),
    OTHER("POOP");

    final ArrayList<String> list = new ArrayList<>();
    BlockedWords(String... s) {
        this.list.addAll(Arrays.stream(s).toList());
    }

    public ArrayList<String> getList() {
        return list;
    }

}
