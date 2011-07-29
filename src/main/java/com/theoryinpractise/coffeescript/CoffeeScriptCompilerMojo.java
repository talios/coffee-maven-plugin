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
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.sun.istack.internal.Nullable;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.jcoffeescript.JCoffeeScriptCompileException;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

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
     *
     * @parameter default-value="false"
     */
    private Boolean bare;

    /**
     * An optional set of joinSet definitions which will join groups of coffee files into
     * a single .js file
     *
     * @parameter
     */
    private List<JoinSet> joinSets;

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

    private List<File> findCoffeeFilesInDirectory(File coffeeDir) {

        List<File> coffeeFiles = Lists.newArrayList();

        File[] files = coffeeDir.listFiles();

        for (File file : files) {

            if (file.isDirectory()) {
                coffeeFiles.addAll(findCoffeeFilesInDirectory(file));
            } else {
                if (file.getPath().endsWith(".coffee")) {
                    coffeeFiles.add(file);
                }
            }
        }

        return coffeeFiles;
    }


    private void compileCoffeeFilesInDirector(final JCoffeeScriptCompiler coffeeScriptCompiler, final File coffeeDir) throws IOException, JCoffeeScriptCompileException, MojoExecutionException {

        List<File> coffeeFiles = findCoffeeFilesInDirectory(coffeeDir);

        Map<File, InputSupplier<? extends Reader>> coffeeSuppliers = Maps.newHashMap();

        Function<String,File> prependCoffeeDirToFiles = new Function<String, File>() {
            public File apply(@Nullable String file) {
                return file == null ? null : new File(coffeeDir, file);
            }
        };

        // Map joinsets
        for (JoinSet joinSet : joinSets) {

            List<InputSupplier<InputStreamReader>> joinSetSuppliers = Lists.newArrayList();
            for (File file : Iterables.transform(joinSet.getFiles(), prependCoffeeDirToFiles)) {
                if (!file.exists()) {
                    throw new MojoExecutionException(String.format("JoinSet %s references missing file: %s", joinSet.getId(), file.getPath()));
                }

                InputSupplier<InputStreamReader> readerSupplier = Files.newReaderSupplier(file, Charsets.UTF_8);
                joinSetSuppliers.add(readerSupplier);
                coffeeFiles.remove(file);
            }

            InputSupplier<Reader> joinSetSupplier = CharStreams.join(joinSetSuppliers);
            coffeeSuppliers.put(new File(outputDirectory, joinSet.getId() + ".js"), joinSetSupplier);

        }

        // Map remaining files
        for (File coffeeFile : coffeeFiles) {
            String jsFileName = coffeeFile.getPath().substring(coffeeDir.getPath().length()).replace(".coffee", ".js");
            coffeeSuppliers.put(new File(outputDirectory, jsFileName), Files.newReaderSupplier(coffeeFile, Charsets.UTF_8));
        }

        for (Map.Entry<File, InputSupplier<? extends Reader>> entry : coffeeSuppliers.entrySet()) {
            compileCoffeeFile(coffeeScriptCompiler, entry.getKey(), entry.getValue());
        }

    }

    private void compileCoffeeFile(JCoffeeScriptCompiler coffeeScriptCompiler, File jsFile, InputSupplier<? extends Reader> coffeeSupplier) throws IOException, JCoffeeScriptCompileException {

        if (!jsFile.getParentFile().exists()) {
            jsFile.getParentFile().mkdirs();
        }

        String js = coffeeScriptCompiler.compile(CharStreams.toString(coffeeSupplier));

        Files.write(js, jsFile, Charsets.UTF_8);

    }
}
