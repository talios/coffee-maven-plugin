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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

/**
 * Compile CoffeeScript with Maven
 *
 * @goal coffee
 * @phase compile
 */
public class CoffeeScriptCompilerMojo extends AbstractMojo {

    /**
     * Location of the output file.  Defaults to ${build.directory}/coffee
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
     * JoinSet definitions to join groups of coffee files into a single .js file
     * Individual Joinsets contain an id element to name the file that will be output and a maven FileSet to define what files are included.
     *
     * @parameter
     * @required
     */
    private List<JoinSet> joinSets;

    public void execute() throws MojoExecutionException {

        CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler(bare);
        
        try {
        	compileCoffeeFiles(gatherCoffeeFiles(), coffeeScriptCompiler);
            
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private Map<File, JoinSetSource> gatherCoffeeFiles() throws IOException, MojoExecutionException {
        List<File> coffeeFiles = Lists.newArrayList();
        Map<File, JoinSetSource> coffeeSuppliers = Maps.newHashMap();

        if (joinSets != null) {
          for (JoinSet joinSet : joinSets) {

            String description = joinSet.getId();
            
            List<InputSupplier<InputStreamReader>> joinSetSuppliers = Lists.newArrayList();
            for (File file : joinSet.getFiles()) {
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

        return coffeeSuppliers;
    }
   
    private void compileCoffeeFiles(final Map<File, JoinSetSource> coffeeSuppliers, 
    		final CoffeeScriptCompiler coffeeScriptCompiler) throws IOException {

    	for (Map.Entry<File, JoinSetSource> entry : coffeeSuppliers.entrySet()) {
            getLog().info(String.format("Compiling %s", entry.getValue().description));
            
            //Get the coffee file contents and compile it into some javascript.
            InputSupplier<? extends Reader> coffeeSupplier = entry.getValue().inputSupplier;
            String js = coffeeScriptCompiler.compile(CharStreams.toString(coffeeSupplier));

            //Create the new Javascript file path
            File jsFile = entry.getKey();
            if (!jsFile.getParentFile().exists()) {
                jsFile.getParentFile().mkdirs();
            }
            
            Files.write(js, jsFile, Charsets.UTF_8);
        }
    }
    
    private class JoinSetSource {
        final String description;
        final InputSupplier<? extends Reader> inputSupplier;

        private JoinSetSource(String description, InputSupplier<? extends Reader> inputSupplier) {
            this.description = description;
            this.inputSupplier = inputSupplier;
        }
    }
}
