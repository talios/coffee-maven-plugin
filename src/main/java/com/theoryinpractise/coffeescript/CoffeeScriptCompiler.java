package com.theoryinpractise.coffeescript;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import java.io.File;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.provider.StrongCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Copyright 2011 Mark Derricutt.
 * <p/>
 * Contributing authors:
 * Daniel Bower
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * Wrapper around the coffee-script compiler from https://github.com/jashkenas/coffee-script/
 */
public class CoffeeScriptCompiler implements Compiler {

    private boolean bare;
    private String version;
    private final Scriptable globalScope;
    private Scriptable coffeeScript;

    public CoffeeScriptCompiler(String version, boolean bare) {
        this.bare = bare;
        this.version = version;

        try {
            Context context = createContext();
            globalScope = context.initStandardObjects();
            final Require require = getSandboxedRequire(context, globalScope, true);
            coffeeScript = require.requireMain(context, "coffee-script");
        } catch (Exception e1) {
            throw new CoffeeScriptException("Unable to load the coffeeScript compiler into Rhino", e1);
        } finally {
            Context.exit();
        }

    }

    private void compileFile(Context context, String sourcePath, String fileName) throws IOException {
        InputSupplier<InputStreamReader> supplier = Resources.newReaderSupplier(getClass().getResource(sourcePath), Charsets.UTF_8);
        context.evaluateReader(globalScope, supplier.getInput(), fileName, 0, null);
    }

    public String compile(File source) {
        Context context = Context.enter();
        try {
						String coffeeScriptSource = Files.toString(source, Charsets.UTF_8);
						Scriptable compileScope = context.newObject(coffeeScript);
						compileScope.setParentScope(coffeeScript);
						compileScope.put("coffeeScript", compileScope, coffeeScriptSource);

						String options = bare ? "{bare: true}" : "{}";

						return (String) context.evaluateString(
										compileScope,
										String.format("compile(coffeeScript, %s);", options),
										"source", 0, null);
				} catch (IOException e) {
						throw new CoffeeScriptException(e.getMessage(), e);
				} catch (JavaScriptException e) {
						throw new CoffeeScriptException(e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    private Context createContext() {
        Context context = Context.enter();
        context.setOptimizationLevel(9); // Enable optimization
        return context;
    }

    private Require getSandboxedRequire(Context cx, Scriptable scope, boolean sandboxed) throws URISyntaxException {
        return new Require(cx, cx.initStandardObjects(),
                new StrongCachingModuleScriptProvider(
                        new UrlModuleSourceProvider(Collections.singleton(
                                getDirectory()), null)), null, null, sandboxed);
    }

    private URI getDirectory() throws URISyntaxException {
        final String resourcePath = String.format("/coffee-script-%s/", version);
        return getClass().getResource(resourcePath).toURI();
    }

}
