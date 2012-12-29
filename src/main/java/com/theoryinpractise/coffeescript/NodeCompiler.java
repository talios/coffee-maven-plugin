package com.theoryinpractise.coffeescript;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author thrykol
 */
public class NodeCompiler implements Compiler {

	boolean bare;

	public NodeCompiler(String executable, boolean bare) {
		this.bare = bare;
	}

	public String compile(File source) {
		Process p;

		String command = "cat " + source.getAbsolutePath() + " | coffee -sc";

		try {
			p = Runtime.getRuntime().exec(
							new String[]{"sh", "-c", command});

			p.waitFor();

			if (p.exitValue() != 0) {
				StringWriter writer = new StringWriter();
				IOUtils.copy(p.getErrorStream(), writer);
				throw new CoffeeScriptException(writer.toString());
			}

			StringWriter writer = new StringWriter();
			IOUtils.copy(p.getInputStream(), writer);

			return writer.toString();
		} catch (InterruptedException ex) {
			throw new CoffeeScriptException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new CoffeeScriptException(ex.getMessage(), ex);
		}
	}

	public String[] commands() {
		return new String[]{"sh","cat","coffee"};
	}
}
