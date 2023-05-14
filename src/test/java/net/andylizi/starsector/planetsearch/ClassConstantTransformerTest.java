package net.andylizi.starsector.planetsearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ClassConstantTransformerTest {
    byte[] input;

    @BeforeEach
    void setup() throws IOException {
        input = ClassConstantTransformer.readClassBuffer(ClassConstantTransformer.class.getClassLoader(),
                ClassConstantTransformer.class.getPackage().getName().concat(".ExpandablePlanetFilter"));
    }

    @Test
    @Disabled
    void testTransform() throws Throwable {
        ClassConstantTransformer transformer = new ClassConstantTransformer(Arrays.asList(
                ClassConstantTransformer.newTransform("com/fs/starfarer/campaign/ui/intel/PlanetFilter",
                        "com/example/PlanetFilter"),
                ClassConstantTransformer.newTransform("com/fs/starfarer/campaign/ui/intel/PlanetsPanel",
                        "com/example/this/is/very/long/much/longer/than/the/original/type/it/s/so/long/PlanetFilter")
        ));
        byte[] output = transformer.apply(input);
//        java.nio.file.Files.write(new java.io.File("ExpandablePlanetFilter_transformed.class").toPath(), output);
        MessageDigest digest = MessageDigest.getInstance("SHA256");
        String actual = bytesToHex(digest.digest(output));
        assertEquals("AEC3483E9C05DF849A926973C503C01A2D503341F9AE06A4D8EE3706B50CB80D", actual);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
