package com.theoryinpractise.coffeescript;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.theoryinpractise.coffeescript.CoffeeScriptCompilerMojo;
import com.theoryinpractise.coffeescript.compiler.RhinoCompiler;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.Iterator;

public class RhinoCompilerTest {

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
        RhinoCompiler compiler = new RhinoCompiler(version, true);

    }

}
