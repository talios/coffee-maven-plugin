package com.theoryinpractise.coffeescript;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.provider.StrongCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

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
public class CoffeeScriptCompiler {

    private final Scriptable globalScope;
    private String version;
    private Scriptable coffeeScript;

    public CoffeeScriptCompiler(String version) {
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

    public CompileResult compile(String coffeeScriptSource, String sourceName, boolean bare, SourceMap map, boolean header, boolean literate) {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(coffeeScript);
            compileScope.setParentScope(coffeeScript);
            compileScope.put("coffeeScript", compileScope, coffeeScriptSource);
            try {
                boolean useMap = map != SourceMap.NONE;
                String options = String.format("{bare: %s, sourceMap: %s, literate: %s, header: %s, filename: '%s'}", bare, useMap, literate, header, sourceName);
                Object result = context.evaluateString(
                        compileScope,
                        String.format("compile(coffeeScript, %s);", options),
                        sourceName, 0, null);

                if (map == SourceMap.NONE) {
                    return new CompileResult((String) result, null);
                } else {

                    NativeObject nativeObject = (NativeObject) result;
                    String js = nativeObject.get("js").toString();
                    String sourceMap;
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        sourceMap = objectMapper.writeValueAsString(nativeObject.get("v3SourceMap"));
                    } catch (Exception e) {
                        sourceMap = null;
                    }
                    
                    return new CompileResult(js, sourceMap);
                }

            } catch (JavaScriptException e) {
                throw new CoffeeScriptException(e.getMessage(), e);
            }
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

    public static enum SourceMap {NONE, V3}

}
