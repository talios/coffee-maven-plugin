package com.theoryinpractise.coffeescript.compiler;

import com.theoryinpractise.coffeescript.compiler.NodeCompiler;
import com.theoryinpractise.coffeescript.compiler.RhinoCompiler;
import com.theoryinpractise.coffeescript.compiler.CoffeeScriptCompiler;
import java.io.IOException;
import org.apache.commons.lang.SystemUtils;

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
			if (SystemUtils.IS_OS_LINUX && _linux(NodeCompiler.class)) {
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

	private static boolean _linux(Class<? extends CoffeeScriptCompiler> klass) throws IOException, InterruptedException, InstantiationException, IllegalAccessException {
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
