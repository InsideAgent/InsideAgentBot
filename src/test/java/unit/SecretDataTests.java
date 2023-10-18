package unit;

import dev.jacrispys.JavaBot.utils.SecretData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SecretDataTests {

    private static final String PATH_DIR = "src/test/java/unit/resources/";
    private static final UUID uuid = UUID.randomUUID();

    @Test
    void generateNewSecretFile() throws IOException {
        String path = PATH_DIR + "_" + uuid + ".yml";
        SecretData.initLoginInfo(path);
        File file = new File(path);
        Assertions.assertTrue(file.exists());
    }

    @AfterAll
    public static void clean() {
        File dir = new File(PATH_DIR);
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

}
