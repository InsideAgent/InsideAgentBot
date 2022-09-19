package dev.jacrispys.JavaBot.api.libs.utils.mysql;

public enum StatType {
    PLAY_COUNTER(Long.class),
    PAUSE_COUNTER(Long.class),
    PLAYTIME_MILLIS(Long.class),
    HIJACK_COUNTER(Short.class),
    COMMAND_COUNTER(Long.class);

    public final Class<?> clazz;

    StatType(Class<?> clazz) {
        this.clazz = clazz;
    }

}
