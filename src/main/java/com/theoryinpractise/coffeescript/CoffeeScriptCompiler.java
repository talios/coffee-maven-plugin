package com.theoryinpractise.coffeescript;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Charsets;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wrapper around the coffee-script compiler from https://github.com/jashkenas/coffee-script/
 *
 */
public class CoffeeScriptCompiler {

	private String coffeeScriptCompilerScript = "coffee-script-1.1.2.js";
	
    private final Scriptable globalScope;
    private boolean bare;

    public CoffeeScriptCompiler(boolean bare) {

        this.bare = bare;
        InputSupplier<InputStreamReader> supplier = Resources.newReaderSupplier(getClass().getResource("/" + coffeeScriptCompilerScript), Charsets.UTF_8);
        Context context = Context.enter();
        context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
        try {
            globalScope = context.initStandardObjects();
            context.evaluateReader(globalScope, supplier.getInput(), coffeeScriptCompilerScript, 0, null);
        } catch (IOException e1) {
            throw new CoffeeScriptException("Unable to load the coffeeScript compiler into Rhino", e1);
        } finally {
            Context.exit();
        }

    }

    public String compile(String coffeeScriptSource) {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);
            compileScope.put("coffeeScript", compileScope, coffeeScriptSource);
            try {

                String options = bare ? "{bare: true}" : "{}";

                return (String) context.evaluateString(
                        compileScope,
                        String.format("CoffeeScript.compile(coffeeScript, %s);", options),
                        "source", 0, null);
            } catch (JavaScriptException e) {
                throw new CoffeeScriptException(e.getMessage(), e);
            }
        } finally {
            Context.exit();
        }
    }

}
