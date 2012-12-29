package com.theoryinpractise.coffeescript.compiler;

import java.io.File;

/**
 *
 * @author thrykol
 */
public interface CoffeeScriptCompiler {
	
	/**
	 * Compiles the source coffee file to javascript.
	 *
	 * @param source CoffeeScript file to compile
	 * @return The compiled javascript
	 */
	public String compile(File source);

	/**
	 * System commands which must exist for this compiler to be available
	 * @return System commands required by the compiler
	 */
	public String[] commands();
}
