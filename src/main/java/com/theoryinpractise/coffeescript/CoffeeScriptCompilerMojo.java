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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
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
     *
     * This can help when trying to diagnose a compilation error
     * @parameter default-value="false"
     */
    private Boolean compileIndividualFiles;

    /**
     * JoinSet definitions to join groups of coffee files into a single .js file
     * Individual Joinsets contain an id element to name the file that will be output and a maven FileSet to define what files are included.
     *
     * @parameter
     * @required
     */
    private List<JoinSet> coffeeJoinSets;


    // following parameters are for minification


    /**
	 * @parameter expression= "${project.build.directory}/coffee/${project.artifactId}-${project.version}.min.js"
	 * @required
	 */
    private String minifiedFile;

    /**
     * Location of the Files to Minify.  Defaults to ${build.directory}/coffee
     *
     * @parameter expression="${project.build.directory}/coffee"
     */
    private File directoryOfFilesToMinify;

    /**
     * The set of files that should be minified.  Be sure to specify the path to the compiled
     *
     * Only one or the other of setOfFilesToMinify or directoryOfFilesToMinify should be specified.  Only setOfFilesToMinify is used if both are specified.
     *
     * @parameter
     */
    private FileSet setOfFilesToMinify;


    public void execute() throws MojoExecutionException {

        CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler(version, bare);

        try {
        	if(compileIndividualFiles){
        		getLog().info("Starting individual compilations of files");

        		for (JoinSet joinSet : coffeeJoinSets) {
        			for(File file : joinSet.getFiles()){
        				getLog().info("Compiling File " + file.getName() +  " in JoinSet:" + joinSet.getId());
                		coffeeScriptCompiler.compile(joinSet.getConcatenatedStringOfFiles());
        			}
            	}
        	}

        	for (JoinSet joinSet : coffeeJoinSets) {
        		getLog().info("Compiling JoinSet: " + joinSet.getId() + " with files:  " + joinSet.getFileNames());

        		String compiled = coffeeScriptCompiler.compile(joinSet.getConcatenatedStringOfFiles());

        		write(joinSet.getId(), compiled);
        	}

        	minify();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void write(final String fileName, final String contents) throws IOException{
        //Create the new Javascript file path
        File jsFile = new File(coffeeOutputDirectory, fileName + ".js");
        if (!jsFile.getParentFile().exists()) {
            jsFile.getParentFile().mkdirs();
        }

        Files.write(contents, jsFile, Charsets.UTF_8);
    }

    /**
     * This is pretty much just a copy of the code in the JavaScriptMinifierMojo class, will remove once I figure out how to run goals in sequence.
     */
    public void minify() throws MojoExecutionException, MojoFailureException, IOException {
	    getLog().info("Minifying all Javascript Files in the Output Directory");
	   	ClosureMinifier minifier = new ClosureMinifier(getLog());

	   	List<File> filesToMinify;
	   	if(null!=setOfFilesToMinify){
	   		getLog().debug("Configured a fileset for minification");
	   		filesToMinify = FileUtilities.fileSetToFileList(setOfFilesToMinify);
    	}else{
    		getLog().debug("Configured a directory for minification");
    		filesToMinify = FileUtilities.directoryToFileList(directoryOfFilesToMinify.getAbsolutePath());
    	}

	   	//check for dest file in source files, if present remove it.
	   	List<File> filesToMinifyMinusDestFile = Lists.newArrayList();
	   	for(File file : filesToMinify){
	   		if(!file.getAbsolutePath().equals(minifiedFile)){
	   			filesToMinifyMinusDestFile.add(file);
	   		}
	   	}

    	getLog().info("About to minify the following files:  " + FileUtilities.getCommaSeparatedListOfFileNames(filesToMinifyMinusDestFile));

    	minifier.compile(filesToMinifyMinusDestFile, minifiedFile);
	}
}
