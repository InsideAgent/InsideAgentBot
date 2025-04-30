package unit;

import dev.jacrispys.JavaBot.utils.SecretData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BasicTests {

    @Test
    void verifyTrue() {
        Assertions.assertTrue(true);
    }

    @Test
    void verifyFalse() {
        Assertions.assertFalse(false);
    }

    @Test
    void verifyInfoLogging() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Logger logger = LoggerFactory.getLogger(BasicTests.class);
        logger.info("Hello, World!");

        final String logMessage = out.toString();
        Assertions.assertTrue(logMessage.contains("Hello, World!"));

    }

    @Test
    void verifyDebugLogging() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Logger logger = LoggerFactory.getLogger(BasicTests.class);
        logger.debug("Hello, World!");

        final String logMessage = out.toString();
        Assertions.assertTrue(logMessage.contains("Hello, World!"));

    }

    @Test
    void verifyNoTraceLogging() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Logger logger = LoggerFactory.getLogger(BasicTests.class);
        logger.trace("Hello, World!");

        final String logMessage = out.toString();
        Assertions.assertFalse(logMessage.contains("Hello, World!"));

    }

    @Test
    void configGenerated() throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        SecretData.initLoginInfo();
        Class<SecretData> clazz = SecretData.class;
        Method m = clazz.getDeclaredMethod("getClassPath");
        m.setAccessible(true);
        String classPath = (String) m.invoke(null);
        m.setAccessible(false);
        String path = classPath + File.separator + "config" + File.separator + "loginInfo.yml";
        File file = new File(path);
        Assertions.assertTrue(file.exists());
    }
}

class TraceLoggingTest {
    @Test
    void verifyTraceLogging() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Logger logger = LoggerFactory.getLogger(TraceLoggingTest.class);
        logger.trace("Hello, World!");

        final String logMessage = out.toString();
        Assertions.assertTrue(logMessage.contains("Hello, World!"));

    }
}
