package com.theoryinpractise.coffeescript;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.jcoffeescript.JCoffeeScriptCompileException;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Goal which touches a timestamp file.
 *
 * @goal coffee
 * @phase compile
 */
public class CoffeeScriptCompilerMojo extends AbstractMojo {

     /**
     * @parameter expression="${basedir}/src/main/coffee"
     * @required
     */
    protected File coffeeDir;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}/coffee"
     * @required
     */
    private File outputDirectory;

    /**
     * Should we compile as bare?
     * @parameter default-value="false"
     */
    private Boolean bare;


    public void execute() throws MojoExecutionException {

        JCoffeeScriptCompiler coffeeScriptCompiler = bare
                ? new JCoffeeScriptCompiler(Sets.immutableEnumSet(Option.BARE))
                : new JCoffeeScriptCompiler();


        if (!coffeeDir.exists()) {
            throw new MojoExecutionException("Coffee source directory not found: " + coffeeDir.getPath());
        }

        try {
            compileCoffeeFilesInDirector(coffeeScriptCompiler, coffeeDir);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }

    }

    private void compileCoffeeFilesInDirector(JCoffeeScriptCompiler coffeeScriptCompiler, File coffeeDir) throws IOException, JCoffeeScriptCompileException {

        File[] files = coffeeDir.listFiles();

        for (File file : files) {

            if (file.isDirectory()) {
                compileCoffeeFilesInDirector(coffeeScriptCompiler, file);
            } else {
                if (file.getPath().endsWith(".coffee")) {

                    compileCoffeeFile(coffeeScriptCompiler, file);

                }
            }
        }
    }

    private void compileCoffeeFile(JCoffeeScriptCompiler coffeeScriptCompiler, File file) throws IOException, JCoffeeScriptCompileException {

        String jsFileName = file.getPath().substring(coffeeDir.getPath().length()).replace(".coffee", ".js");

        getLog().info(String.format("Compiling %s into %s", file.getPath(), jsFileName ));

        InputSupplier<InputStreamReader> rs = Files.newReaderSupplier(file, Charsets.UTF_8);

        String js = coffeeScriptCompiler.compile(CharStreams.toString(rs));

        File jsFile = new File(outputDirectory, jsFileName);

        if (!jsFile.getParentFile().exists()) {
            jsFile.getParentFile().mkdirs();
        }

        Files.write(js, jsFile, Charsets.UTF_8);

    }
}
