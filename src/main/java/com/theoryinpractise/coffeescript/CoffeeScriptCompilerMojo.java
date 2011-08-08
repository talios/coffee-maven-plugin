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
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;

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

        CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler(bare);

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

    private class JoinSetSource {
        final String description;
        final InputSupplier<? extends Reader> inputSupplier;

        private JoinSetSource(String description, InputSupplier<? extends Reader> inputSupplier) {
            this.description = description;
            this.inputSupplier = inputSupplier;
        }
    }

    private void compileCoffeeFilesInDirector(final CoffeeScriptCompiler coffeeScriptCompiler, final File coffeeDir)
            throws IOException, MojoExecutionException {

        List<File> coffeeFiles = findCoffeeFilesInDirectory(coffeeDir);

        Map<File, JoinSetSource> coffeeSuppliers = Maps.newHashMap();

        Function<String,File> prependCoffeeDirToFiles = new Function<String, File>() {
            public File apply(String file) {
                return file == null ? null : new File(coffeeDir, file);
            }
        };

        Function<String,String> simpleFileName = new Function<String, String>() {
            public String apply(String file) {
                return file == null ? null : file.substring(file.lastIndexOf("/") + 1);
            }
        };

        // Map joinsets
        if (joinSets != null) {
          for (JoinSet joinSet : joinSets) {

            String description = String.format(
                    "joingset %s (containing %s)",
                    joinSet.getId(),
                    Joiner.on(", ").join(transform(joinSet.getFiles(), simpleFileName)));

            List<InputSupplier<InputStreamReader>> joinSetSuppliers = Lists.newArrayList();
            for (File file : transform(joinSet.getFiles(), prependCoffeeDirToFiles)) {
                if (!file.exists()) {
                    throw new MojoExecutionException(String.format("JoinSet %s references missing file: %s", joinSet.getId(), file.getPath()));
                }

                InputSupplier<InputStreamReader> readerSupplier = Files.newReaderSupplier(file, Charsets.UTF_8);
                joinSetSuppliers.add(readerSupplier);
                coffeeFiles.remove(file);
            }

            InputSupplier<Reader> joinSetSupplier = CharStreams.join(joinSetSuppliers);
            File jsFileName = new File(outputDirectory, joinSet.getId() + ".js");
            coffeeSuppliers.put(jsFileName, new JoinSetSource(description, joinSetSupplier));

          }
        }

        // Map remaining files
        for (File coffeeFile : coffeeFiles) {
            String jsFileName = coffeeFile.getPath().substring(coffeeDir.getPath().length()).replace(".coffee", ".js");
            File jsFile = new File(outputDirectory, jsFileName);
            coffeeSuppliers.put(jsFile, new JoinSetSource(coffeeFile.getName(), Files.newReaderSupplier(coffeeFile, Charsets.UTF_8)));
        }

        for (Map.Entry<File, JoinSetSource> entry : coffeeSuppliers.entrySet()) {
            getLog().info(String.format("Compiling %s", entry.getValue().description));
            compileCoffeeFile(coffeeScriptCompiler, entry.getKey(), entry.getValue().inputSupplier);
        }

    }

    private void compileCoffeeFile(CoffeeScriptCompiler coffeeScriptCompiler, File jsFile, InputSupplier<? extends Reader> coffeeSupplier) throws IOException {

        if (!jsFile.getParentFile().exists()) {
            jsFile.getParentFile().mkdirs();
        }

        String js = coffeeScriptCompiler.compile(CharStreams.toString(coffeeSupplier));

        Files.write(js, jsFile, Charsets.UTF_8);

    }
}
