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
			if (SystemUtils.IS_OS_LINUX && _linux(NodeCompiler.class)) {
				compiler = new NodeCompiler("coffee", bare);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (compiler == null) {
			compiler = new CoffeeScriptCompiler(version, bare);
		}

		return compiler;
	}

	private static boolean _linux(Class<? extends Compiler> klass) throws IOException, InterruptedException, InstantiationException, IllegalAccessException {
		boolean result = true;
		for (String cmd : klass.newInstance().commands()) {
			Process p = Runtime.getRuntime().exec(
							new String[]{"which", cmd});

			p.waitFor();
			result &= p.exitValue() == 0;
		}

		return result;
	}
}
