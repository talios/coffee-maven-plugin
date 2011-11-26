package com.theoryinpractise.coffeescript;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Copyright 2011 Mark Derricutt.
 *
 * Contributing authors:
 *   Daniel Bower
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
 *
 *
 * Compile CoffeeScript with Maven
 *
 * @goal coffee
 * @phase compile
 */
public class CoffeeScriptCompilerMojo extends AbstractMojo {

    /**
     * Location of the output files from the Coffee Compiler.  Defaults to ${build.directory}/coffee
     *
     * @parameter expression="${project.build.directory}/coffee"
     * @required
     */
    private File coffeeOutputDirectory;

    /**
     * Should we compile as bare?  A compiler option for the Coffee Compiler.
     *
     * @parameter default-value="false"
     */
    private Boolean bare;

    /**
     * Should we compile as bare?
     *
     * @parameter default-value="1.1.3"
     */
    private String version;

    /**
     * Should the files be compiled individually before compiling them as a whole.
     * <p/>
     * This can help when trying to diagnose a compilation error
     *
     * @parameter default-value="false"
     */
    private Boolean compileIndividualFiles;

    /**
     * JoinSet definitions to join groups of coffee files into a single .js file
     * Individual Joinsets contain an id element to name the file that will be output and a maven
     * FileSet to define what files are included.
     *
     * @parameter
     * @required
     */
    private List<JoinSet> coffeeJoinSets;

    public void execute() throws MojoExecutionException {

        CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler(version, bare);

        try {
            if (compileIndividualFiles) {
                getLog().info("Starting individual compilations of files");

                for (JoinSet joinSet : coffeeJoinSets) {
                    StringBuilder compiled = new StringBuilder();
                    for (File file : joinSet.getFiles()) {
                        getLog().info("Compiling File " + file.getName() + " in JoinSet:" + joinSet.getId());
                        compiled.append(coffeeScriptCompiler.compile(Files.toString(file, Charsets.UTF_8)));
                    }
                    write(joinSet.getId(), compiled.toString());
                }
            } else {
                for (JoinSet joinSet : coffeeJoinSets) {
                    getLog().info("Compiling JoinSet: " + joinSet.getId() + " with files:  " + joinSet.getFileNames());

                    String compiled = coffeeScriptCompiler.compile(joinSet.getConcatenatedStringOfFiles());

                    write(joinSet.getId(), compiled);
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void write(final String fileName, final String contents) throws IOException {
        //Create the new Javascript file path
        File jsFile = new File(coffeeOutputDirectory, fileName + ".js");
        if (!jsFile.getParentFile().exists()) {
            jsFile.getParentFile().mkdirs();
        }

        Files.write(contents, jsFile, Charsets.UTF_8);
    }

}
