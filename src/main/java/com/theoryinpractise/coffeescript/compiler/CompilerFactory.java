package com.theoryinpractise.coffeescript.compiler;

import org.apache.commons.lang.SystemUtils;

import java.io.IOException;

/**
 *
 * @author thrykol
 */
public abstract class CompilerFactory {

	/**
	 * Create a new compiler instance.
	 *
	 * @param version
	 * @param bare
	 * @return
	 */
	public static CoffeeScriptCompiler newInstance(String version, boolean bare) {
		CoffeeScriptCompiler compiler = null;

		try {
			if (nodeIsSupport() && nodeisAvailable()) {
				compiler = new NodeCompiler("coffee", bare);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (compiler == null) {
			compiler = new RhinoCompiler(version, bare);
		}

		return compiler;
	}

    private static boolean nodeIsSupport() {
        return SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX;
    }

    private static boolean nodeisAvailable() throws IOException, InterruptedException, InstantiationException, IllegalAccessException {
		boolean result = true;
		for (String cmd : NodeCompiler.class.newInstance().commands()) {
			Process p = Runtime.getRuntime().exec(
							new String[]{"which", cmd});

			p.waitFor();
			result &= p.exitValue() == 0;
		}

		return result;
	}
}
