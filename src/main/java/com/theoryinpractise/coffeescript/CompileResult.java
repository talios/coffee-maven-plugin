package com.theoryinpractise.coffeescript;

/** The javascript, and optional source map, produced by compiling a CoffeeScript source. */
public class CompileResult {

    private String js;
    private String map;

    /**
     * Creates a result holding the output of a single compilation.
     *
     * @param js the compiled javascript
     * @param map the source map, or {@code null} if none was requested
     */
    public CompileResult(String js, String map) {
        this.js = js;
        this.map = map;
    }

    /**
     * The javascript produced by the compiler.
     *
     * @return the compiled javascript
     */
    public String getJs() {
        return js;
    }

    /**
     * The source map produced by the compiler.
     *
     * @return the source map, or {@code null} if none was requested
     */
    public String getMap() {
        return map;
    }
}
