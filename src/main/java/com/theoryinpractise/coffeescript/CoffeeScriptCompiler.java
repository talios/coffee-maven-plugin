package com.theoryinpractise.coffeescript;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2011 Mark Derricutt.
 * <p>
 * Contributing authors:
 * Daniel Bower
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Wrapper around the coffee-script compiler from https://github.com/jashkenas/coffee-script/
 */
public class CoffeeScriptCompiler implements AutoCloseable {

    /** Wraps a module body in the standard CommonJS function signature. */
    private static final String MODULE_PREFIX = "(function (exports, require, module, __filename, __dirname) {";
    private static final String MODULE_SUFFIX = "\n});";

    private final String version;
    private final Context context;

    /** Calls a wrapped module body with {@code this} bound to its exports, as node does. */
    private final Value moduleInvoker;
    private final Value objectFactory;
    private final Value jsonStringify;

    /** Module id (without extension) to its CommonJS module object. */
    private final Map<String, Value> moduleCache = new HashMap<String, Value>();

    private final ProxyExecutable require = new ProxyExecutable() {
        public Object execute(Value... arguments) {
            if (arguments.length < 1 || !arguments[0].isString()) {
                throw new CoffeeScriptException("require() expects a single module id");
            }
            return requireModule(arguments[0].asString());
        }
    };

    private final Value coffeeScript;

    /**
     * Loads the bundled coffee-script compiler of the given version into a GraalJS context.
     *
     * @param version the coffee-script version to load, as bundled in {@code coffee-script/<version>}
     */
    public CoffeeScriptCompiler(String version) {
        this.version = version;

        try {
            this.context = createContext();
            this.moduleInvoker = context.eval("js",
                    "(function (fn, exports, require, module, filename, dirname) {"
                            + " fn.call(exports, exports, require, module, filename, dirname); })");
            this.objectFactory = context.eval("js", "(function () { return {}; })");
            this.jsonStringify = context.eval("js", "(function (value) { return JSON.stringify(value); })");
            this.coffeeScript = requireModule("./coffee-script");
        } catch (CoffeeScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new CoffeeScriptException("Unable to load the coffeeScript compiler into GraalJS", e);
        }
    }

    /**
     * Compile a CoffeeScript source to javascript.
     *
     * @param coffeeScriptSource the CoffeeScript source to compile
     * @param sourceName the file name reported in errors and source maps
     * @param bare {@code true} to omit the top level function safety wrapper
     * @param map the kind of source map to generate
     * @param header {@code true} to prefix the output with a generated-by header
     * @param literate {@code true} if the source is literate CoffeeScript
     * @return the compiled javascript, and the source map when one was requested
     */
    public CompileResult compile(String coffeeScriptSource, String sourceName, boolean bare, SourceMap map, boolean header, boolean literate) {
        try {
            boolean useMap = map != SourceMap.NONE;

            Value options = objectFactory.execute();
            options.putMember("bare", bare);
            options.putMember("sourceMap", useMap);
            options.putMember("literate", literate);
            options.putMember("header", header);
            options.putMember("filename", sourceName);

            Value result = coffeeScript.getMember("compile").execute(coffeeScriptSource, options);

            if (!useMap) {
                return new CompileResult(result.asString(), null);
            }

            return new CompileResult(result.getMember("js").asString(), asJson(result.getMember("v3SourceMap")));
        } catch (PolyglotException e) {
            throw new CoffeeScriptException(e.getMessage(), e);
        }
    }

    /**
     * coffee-script hands back the v3 source map already stringified, but older releases
     * have been known to return the raw object - serialise those on the fly.
     */
    private String asJson(Value sourceMap) {
        if (sourceMap == null || sourceMap.isNull()) {
            return null;
        }
        if (sourceMap.isString()) {
            return sourceMap.asString();
        }
        try {
            return jsonStringify.execute(sourceMap).asString();
        } catch (PolyglotException e) {
            return null;
        }
    }

    private Context createContext() {
        return Context.newBuilder("js")
                .allowHostAccess(HostAccess.NONE)
                .allowHostClassLookup(new java.util.function.Predicate<String>() {
                    public boolean test(String className) {
                        return false;
                    }
                })
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    /**
     * A minimal CommonJS loader over the coffee-script sources bundled on the classpath.
     * Only relative ids are resolvable - coffee-script reaches for node builtins such as
     * {@code fs} or {@code module} only on code paths the compiler itself never takes.
     */
    private Value requireModule(String id) {
        String name = normalise(id);

        Value cached = moduleCache.get(name);
        if (cached != null) {
            return cached.getMember("exports");
        }

        String resourcePath = String.format("/coffee-script-%s/%s.js", version, name);
        String body = readResource(resourcePath);

        Value module = objectFactory.execute();
        module.putMember("exports", objectFactory.execute());
        // cache before evaluating so that circular requires see the partial exports
        moduleCache.put(name, module);

        Source source = Source.newBuilder("js", MODULE_PREFIX + body + MODULE_SUFFIX, name + ".js")
                .buildLiteral();

        try {
            moduleInvoker.execute(context.eval(source), module.getMember("exports"), require, module,
                    resourcePath, String.format("/coffee-script-%s", version));
        } catch (RuntimeException e) {
            moduleCache.remove(name);
            throw e;
        }

        return module.getMember("exports");
    }

    private String normalise(String id) {
        if (!id.startsWith("./")) {
            throw new CoffeeScriptException(
                    String.format("Unable to resolve module '%s' - only the bundled coffee-script modules are available", id));
        }
        return id.substring(2);
    }

    private String readResource(String resourcePath) {
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new CoffeeScriptException("Unable to find " + resourcePath + " on the classpath");
        }
        try {
            StringBuilder content = new StringBuilder();
            Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
            return content.toString();
        } catch (IOException e) {
            throw new CoffeeScriptException("Unable to read " + resourcePath, e);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
                // nothing useful to do here
            }
        }
    }

    /** Closes the underlying GraalJS context. */
    public void close() {
        context.close();
    }

    /** The kinds of source map the compiler can generate. */
    public static enum SourceMap {
        /** Generate no source map. */
        NONE,
        /** Generate a version 3 source map. */
        V3
    }

}
