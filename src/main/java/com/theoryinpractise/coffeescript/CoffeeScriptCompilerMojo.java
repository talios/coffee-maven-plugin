package com.theoryinpractise.coffeescript;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Copyright 2011 Mark Derricutt.
 * <p/>
 * Contributing authors:
 * Daniel Bower
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * Compile CoffeeScript with Maven
 *
 * @goal coffee
 * @phase compile
 */
public class CoffeeScriptCompilerMojo extends AbstractMojo {

    /**
     * Default location of .coffee source files.
     *
     * @parameter expression="${basedir}/src/main/coffee"
     * @required
     */
    private File coffeeDir;

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
     * Should we generate source maps when compiling?
     *
     * @parameter default-value="false"
     */
    private Boolean map;

    /**
     * Should we generate source maps when compiling?
     *
     * @parameter default-value="true"
     */
    private Boolean header;

    /**
     * What version of Coffee-Script should we compile with?
     *
     * @parameter default-value="1.6.1"
     */
    private String version;

    /**
     * Should the files be compiled individually or as a whole.
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
     */
    private List<JoinSet> coffeeJoinSets;

    /**
     * The Sub Directory is preserved.
     * 
     * @parameter default-value="false"
     */
    private Boolean preserveSubDirectory;

    @VisibleForTesting
    List<String> acceptableVersions = ImmutableList.of("1.2.0", "1.3.1", "1.3.3", "1.4.0", "1.5.0", "1.6.1");

    public void execute() throws MojoExecutionException {

        if (compileIndividualFiles && map) {
            throw new MojoExecutionException("Unable to generate source maps when compiling joinsets individually");
        }

        if (map && !version.equals("1.6.1")) {
            throw new MojoExecutionException("CoffeeScript 1.6.1 is required for using source maps");
        }

        if (!acceptableVersions.contains(version)) {

            String error = String.format("Unsupported version of coffee-script specified (%s) - supported versions: %s",
                                                version,
                                                Joiner.on(", ").join(acceptableVersions));

            throw new MojoExecutionException(error);
        }

        getLog().info(String.format("coffee-maven-plugin using coffee script version %s", version));
        CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler(version);

        try {
            if (compileIndividualFiles) {
                getLog().info("Starting individual compilations of files");

                for (JoinSet joinSet : findJoinSets()) {
                    StringBuilder compiled = new StringBuilder();
                    for (File file : joinSet.getFiles()) {
                        getLog().info("Compiling File " + file.getName() + " in JoinSet:" + joinSet.getId());
                        compiled
                                .append(coffeeScriptCompiler.compile(Files.toString(file, Charsets.UTF_8), file.getName(), bare, getSourceMapType(), header, file.getName().endsWith(".litcoffee")).getJs())
                                .append("\n");
                    }
                    write(joinSet.getCoffeeOutputDirectory(), joinSet.getId(), new CompileResult(compiled.toString(), null));
                }
            } else {
                for (JoinSet joinSet : findJoinSets()) {
                    getLog().info("Compiling JoinSet: " + joinSet.getId() + " with files:  " + joinSet.getFileNames());

                    String sourceName = joinSet.getId() + (joinSet.isLiterate() ? ".litcoffee" : ".coffee");
                    CompileResult compiled = coffeeScriptCompiler.compile(joinSet.getConcatenatedStringOfFiles(), sourceName, bare, getSourceMapType(), header, joinSet.isLiterate());

                    write(joinSet.getCoffeeOutputDirectory(), joinSet.getId(), compiled);
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public CoffeeScriptCompiler.SourceMap getSourceMapType() {
        if (map) {
            return CoffeeScriptCompiler.SourceMap.V3;
        } else {
            return CoffeeScriptCompiler.SourceMap.NONE;
        }
    }

    private List<JoinSet> findJoinSets() {
        if (coffeeJoinSets != null && !coffeeJoinSets.isEmpty()) {
            return coffeeJoinSets;
        } else {
            // Generate a joinset for each .coffee file
            return ImmutableList.<JoinSet>builder()
                    .addAll(findJoinSetsInDirectory(coffeeDir, ".coffee", false))
                    .addAll(findJoinSetsInDirectory(coffeeDir, ".litcoffee", true))
                    .addAll(findJoinSetsInDirectory(coffeeDir, ".coffee.md", true))
                    .build();

        }
    }

    private List<JoinSet> findJoinSetsInDirectory(final File coffeeDir, final String suffix, final boolean literate) {
        return Lists.transform(findCoffeeFilesInDirectory(coffeeDir, suffix), new Function<File, JoinSet>() {
            public JoinSet apply(@Nullable File file) {
                if(preserveSubDirectory) {
                    return new StaticJoinSet(coffeeDir, file, literate);    
                }else {
                    return new StaticJoinSet(file.getParentFile(), file, literate);    
                }
            }
        });
    }

    private static class StaticJoinSet extends JoinSet {
        private File file;

        private StaticJoinSet(File parent, File file, boolean literate) {
            this.file = file;
            String name = file.getPath().substring(parent.getPath().length() + 1);
            name = name.substring(0, name.lastIndexOf("."));
            if (name.endsWith("coffee")) {
                name = name.substring(0, name.lastIndexOf("."));
            }

            name = name.replace(File.separatorChar, '/');
            setId(name);
            setLiterate(literate);
        }

        @Override
        public List<File> getFiles() throws IOException {
            return ImmutableList.of(file);
        }
    }


    private List<File> findCoffeeFilesInDirectory(File coffeeDir, final String suffix) {

        List<File> coffeeFiles = Lists.newArrayList();

        File[] files = coffeeDir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                coffeeFiles.addAll(findCoffeeFilesInDirectory(file, suffix));
            } else {
                if (file.getPath().endsWith(suffix)) {
                    coffeeFiles.add(file);
                }
            }
        }

        return coffeeFiles;
    }

    private void write(final File joinSetOutputDirectory, final String fileName, final CompileResult contents) throws IOException {
        //Create the new Javascript file path
        File outputDirectory = coffeeOutputDirectory;
        if (joinSetOutputDirectory != null) {
            outputDirectory = joinSetOutputDirectory;
        }
        File jsFile = new File(outputDirectory, fileName + ".js");
        if (!jsFile.getParentFile().exists()) {
            jsFile.getParentFile().mkdirs();
        }

        Files.write(contents.getJs(), jsFile, Charsets.UTF_8);
        if (contents.getMap() != null) {
            File mapFile = new File(outputDirectory, fileName + ".js.map");
            Files.write(contents.getMap(), mapFile, Charsets.UTF_8);
        }
    }

}
