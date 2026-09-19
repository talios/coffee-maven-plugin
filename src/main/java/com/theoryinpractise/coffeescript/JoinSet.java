package com.theoryinpractise.coffeescript;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.apache.maven.model.FileSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
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
 */

/** Wrap a Maven fileset to add properties for describing the group of files */
public class JoinSet {
    /** Creates an empty JoinSet, populated by maven from the plugin configuration. */
    public JoinSet() {
    }

    private String id;

    private boolean literate = false;

    private Boolean compileIndividualFiles;
    /**
     * Location of the output files from the Coffee Compiler. Defaults to the value for coffeeOutputDirectory specified
     * in the main configuration if it is not specified within this JoinSet.
     *
     * @parameter expression="${project.build.directory}/coffee"
     */
    private File coffeeOutputDirectory;

    private FileSet fileSet;

    private List<File> orderedFiles = new ArrayList<File>();

    /**
     * A cache of the list of files in the fileSet
     */
    private List<File> files;

    /**
     * A cache of the concatenated contents of the files in the fileset
     */
    private String concatenatedStringOfFiles;

    /**
     * Whether each file in this JoinSet is compiled to its own javascript file.
     *
     * @return {@code true} to compile individually, {@code false} to concatenate, or {@code null} to inherit the
     *         plugin wide setting
     */
    public Boolean getCompileIndividualFiles() {
        return compileIndividualFiles;
    }

    /**
     * Sets whether each file in this JoinSet is compiled to its own javascript file.
     *
     * @param compileIndividualFiles {@code true} to compile individually, {@code false} to concatenate, or
     *        {@code null} to inherit the plugin wide setting
     */
    public void setCompileIndividualFiles(Boolean compileIndividualFiles) {
        this.compileIndividualFiles = compileIndividualFiles;
    }

    /**
     * The id grouping the files in this JoinSet.
     *
     * @return the id, which is also the name of the javascript file that is output
     */
    public String getId() {
        return id;
    }

    /**
     * An id to group the Files in the Fileset.  This id becomes the name of the javascript file that is output.
     *
     * @param id the id of this JoinSet
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Whether the files in this JoinSet are literate CoffeeScript.
     *
     * @return {@code true} if the sources are literate CoffeeScript
     */
    public boolean isLiterate() {
        return literate;
    }

    /**
     * Sets whether the files in this JoinSet are literate CoffeeScript.
     *
     * @param literate {@code true} if the sources are literate CoffeeScript
     */
    public void setLiterate(boolean literate) {
        this.literate = literate;
    }

    /**
     * The directory the javascript for this JoinSet is written to.
     *
     * @return the output directory, or {@code null} to use the plugin wide output directory
     */
    public File getCoffeeOutputDirectory() {
        return coffeeOutputDirectory;
    }

    /**
     * A location where to put the output javascript(s) for this specific JoinSet.
     *
     * @param coffeeOutputDirectory the directory to write this JoinSet's javascript to
     */
    public void setCoffeeOutputDirectory(File coffeeOutputDirectory) {
        this.coffeeOutputDirectory = coffeeOutputDirectory;
    }

    /**
     * Pulls the list of files that will be used from the fileset.
     *
     * @return the ordered files followed by the remaining files of the fileset
     * @throws IOException if the files of the fileset cannot be read
     */
    public List<File> getFiles() throws IOException {
    	if(null==files){
            files = new ArrayList<File>();
            files.addAll(orderedFiles);
            FileSet set = getFileSet();
            if (set != null) {
                List<File> fileSetFiles = FileUtilities.getFilesFromFileSet(set);
                fileSetFiles.removeAll(orderedFiles);
	    	    files.addAll(fileSetFiles);
            }
	    }
    	return files;
    }

    /**
     * The names of the files in this JoinSet, for logging.
     *
     * @return a comma separated list of file names
     * @throws IOException if the files of the fileset cannot be read
     */
    public String getFileNames() throws IOException {
    	StringBuilder joinSetFileNames = new StringBuilder();

		for(File file : getFiles()){
		    joinSetFileNames.append(file.getName());
		    joinSetFileNames.append(", ");
		}

    	return joinSetFileNames.toString();
    }

    /**
     * The contents of every file in this JoinSet, joined in order.
     *
     * @return the concatenated contents of the files
     * @throws IOException if a file is missing or cannot be read
     */
    public String getConcatenatedStringOfFiles() throws IOException{
    	if(null==concatenatedStringOfFiles){
    		StringBuilder sb = new StringBuilder();

        	for (File file : getFiles()) {
                if (!file.exists()) {
                    throw new IOException(String.format("JoinSet %s references missing file: %s", getId(), file.getPath()));
                }

                BufferedReader reader = Files.newReader(file, Charsets.UTF_8);
                sb.append(CharStreams.toString(reader));
                sb.append("\n");
            }

        	concatenatedStringOfFiles = sb.toString();
    	}

    	return concatenatedStringOfFiles;
    }

	/**
	 * The maven FileSet defining what files are included, excluded, etc.
	 *
	 * @return the fileset, or {@code null} if only ordered files are used
	 */
	public FileSet getFileSet() {
		return fileSet;
	}

	/**
	 * a maven FileSet to define what files are included, excluded, etc
	 *
	 * @param fileSet the fileset selecting the files of this JoinSet
	 */
	public void setFileSet(FileSet fileSet) {
		files = null;
		concatenatedStringOfFiles = null;
		this.fileSet = fileSet;

	}

    /**
     * Files that are compiled ahead of, and in preference to, the files of the fileset.
     *
     * @return the explicitly ordered files
     */
    public List<File> getOrderedFiles() {
        return orderedFiles;
    }

    /**
     * Files to compile ahead of, and in preference to, the files of the fileset.
     *
     * @param orderedFiles the files to compile first, in order
     */
    public void setOrderedFiles(List<File> orderedFiles) {
        files = null;
        concatenatedStringOfFiles = null;
        compileIndividualFiles = false;
        this.orderedFiles = orderedFiles;
    }
}
