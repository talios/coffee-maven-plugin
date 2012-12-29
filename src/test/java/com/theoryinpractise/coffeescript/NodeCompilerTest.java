package com.theoryinpractise.coffeescript;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import java.io.File;
import java.util.Iterator;
import javax.annotation.Nullable;
import static org.junit.Assume.assumeTrue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

/**
 *
 * @author thrykol
 */
public class NodeCompilerTest {

	@DataProvider
	public Iterator<Object[]> provideVersions() {

		return Iterators.transform(new CoffeeScriptCompilerMojo().acceptableVersions.iterator(), new Function<String, Object[]>() {

			public Object[] apply(@Nullable String s) {
				return new Object[]{s};
			}
		});

	}

	public NodeCompilerTest() {
	}

	@Test(dataProvider = "provideVersions")
	public void testCompile(final String version) throws Exception {
		Compiler compiler = CompilerFactory.newInstance(version, false);

		if (compiler instanceof NodeCompiler) {
			File file = new File("target/test-classes/sample.coffee");
			String output = compiler.compile(file);

			System.out.println("Output: [" + output + "]");
		}
	}
}
