package com.inspera.parser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class ParserTest {

    private Parser parser = new Parser();
    private static JsonObject before;
    private static JsonObject after;

    private static JsonObject diff;

    @BeforeAll
    public static void setup() throws IOException {
        before = JsonParser.parseString(Files.readString(Paths.get("src/test/resources/before.json"))).getAsJsonObject();
        after = JsonParser.parseString(Files.readString(Paths.get("src/test/resources/after.json"))).getAsJsonObject();
        diff = JsonParser.parseString(Files.readString(Paths.get("src/test/resources/diff.json"))).getAsJsonObject();
    }

    @Test
    public void test() {
        JsonObject diffActual = parser.parse(before, after);
        assertEquals(diff, diffActual);
    }

}
