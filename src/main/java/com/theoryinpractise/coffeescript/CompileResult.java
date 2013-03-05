package com.theoryinpractise.coffeescript;

public class CompileResult {

    private String js;
    private String map;

    public CompileResult(String js, String map) {
        this.js = js;
        this.map = map;
    }

    public String getJs() {
        return js;
    }

    public String getMap() {
        return map;
    }
}
