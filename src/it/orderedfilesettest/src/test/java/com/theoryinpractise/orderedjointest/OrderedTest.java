package com.theoryinpractise.orderedjointest;

import org.testng.annotations.Test;

import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OrderedTest {

    @Test
    public void testOrderedJavaScript() throws IOException, ScriptException {

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.eval(new String(Files.readAllBytes(Paths.get("target", "coffee/main.js"))));

    }

}
