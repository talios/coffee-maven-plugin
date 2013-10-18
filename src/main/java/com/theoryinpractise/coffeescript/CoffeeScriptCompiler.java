package com.theoryinpractise.coffeescript;

import com.google.common.io.CharStreams;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.ExecutionContext;

import java.io.InputStream;
import java.io.InputStreamReader;

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

    private String version;
    private DynJS runtime;
    private final ExecutionContext context;

    public CoffeeScriptCompiler(String version) {
        this.version = version;

        try {
            runtime = new DynJS();
            context = runtime.getExecutionContext();
            final String coffeeScriptTarget = String.format("/coffee-script-%s.js", version);
            final InputStream coffeeScriptStream = CoffeeScriptCompiler.class.getResourceAsStream(coffeeScriptTarget);
            final String source = CharStreams.toString(new InputStreamReader(coffeeScriptStream));
            runtime.execute(source);
        } catch (Exception e1) {
            throw new CoffeeScriptException("Unable to load the coffeeScript compiler", e1);
        }

    }

    public CompileResult compile(String coffeeScriptSource, String sourceName, boolean bare, SourceMap map, boolean header, boolean literate) {

        Scope scope = compileContext.getScope();
        scope.define("coffeeScript", coffeeScriptSource);
        boolean useMap = map != SourceMap.NONE;
        String options = String.format("{bare: %s, sourceMap: %s, literate: %s, header: %s, filename: '%s'}", bare, useMap, literate, header, sourceName);

        dynJs.eval(compileContext,
                   String.format("val target = compile(coffeeScript, %s);", options),
                   "source");

        if (map == SourceMap.NONE) {
            String result = (String) compileContext.getScope().resolve("target");
            return new CompileResult(result, null);
        } else {
            String result = (String) compileContext.getScope().resolve("target.js");
            String sourceMap;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                sourceMap = objectMapper.writeValueAsString(nativeObject.get(compileContext.getScope().resolve("target.v3SourceMap"));
            } catch (Exception e) {
                sourceMap = null;
            }
            
            return new CompileResult(js, sourceMap);
        }
    }

    public static enum SourceMap {NONE, V3}

}
