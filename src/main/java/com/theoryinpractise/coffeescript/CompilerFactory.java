package com.theoryinpractise.coffeescript;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;

/**
 *
 * @author thrykol
 */
public abstract class CompilerFactory {
	public static Compiler newInstance(String version, boolean bare) {
		Compiler compiler = null;

		try {
			if(SystemUtils.IS_OS_LINUX && _linux()) {
				compiler = new NodeCompiler("coffee", bare);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		if(compiler == null)
			compiler = new CoffeeScriptCompiler(version, bare);

		return compiler;
	}

	private static boolean _linux() throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(
						new String[]{"which","coffee"});

		p.waitFor();
		StringWriter writer = new StringWriter();
		IOUtils.copy(p.getInputStream(), writer);

		return p.exitValue() == 0;
	}
}
