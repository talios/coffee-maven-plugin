package com.theoryinpractise.coffeescript;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.Iterator;

public class CoffeeScriptCompilerTest {

    @DataProvider
    public Iterator<Object[]> provideVersions() {

        return Iterators.transform(new CoffeeScriptCompilerMojo().acceptableVersions.iterator(), new Function<String, Object[]>() {
            public Object[] apply(@Nullable String s) {
                return new Object[] {s};
            }
        });

    }

    @Test(dataProvider = "provideVersions")
    public void testCompiler(final String version) {
        new CoffeeScriptCompiler(version);
    }

    @Test(dataProvider = "provideVersions")
    public void testCompilation(final String version) {
        CoffeeScriptCompiler compiler = new CoffeeScriptCompiler(version);
        compiler.compile("string   = \"file3\"", "test", true, false);
    }

}
