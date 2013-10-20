package com.theoryinpractise.coffeescript;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
 * Wrap a Maven fileset to add properties for describing the group of files
 *
 */
public class JoinSet {

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

    public Boolean getCompileIndividualFiles() {
        return compileIndividualFiles;
    }

    public void setCompileIndividualFiles(Boolean compileIndividualFiles) {
        this.compileIndividualFiles = compileIndividualFiles;
    }

    public String getId() {
        return id;
    }

    /**
     * An id to group the Files in the Fileset.  This id becomes the name of the javascript file that is output.
     */
    public void setId(String id) {
        this.id = id;
    }

    public boolean isLiterate() {
        return literate;
    }

    public void setLiterate(boolean literate) {
        this.literate = literate;
    }

    public File getCoffeeOutputDirectory() {
        return coffeeOutputDirectory;
    }

    /**
     * A location where to put the output javascript(s) for this specific JoinSet.
     *
     * @param coffeeOutputDirectory
     */
    public void setCoffeeOutputDirectory(File coffeeOutputDirectory) {
        this.coffeeOutputDirectory = coffeeOutputDirectory;
    }

    /**
     * Pulls the list of files that will be used from the fileset.
     * @throws MojoExecutionException
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

    public String getFileNames() throws IOException {
    	StringBuilder joinSetFileNames = new StringBuilder();

		for(File file : getFiles()){
		    joinSetFileNames.append(file.getName());
		    joinSetFileNames.append(", ");
		}

    	return joinSetFileNames.toString();
    }

    public String getConcatenatedStringOfFiles() throws IOException{
    	if(null==concatenatedStringOfFiles){
    		StringBuilder sb = new StringBuilder();

        	for (File file : getFiles()) {
                if (!file.exists()) {
                    throw new IOException(String.format("JoinSet %s references missing file: %s", getId(), file.getPath()));
                }

                InputSupplier<InputStreamReader> readerSupplier = Files.newReaderSupplier(file, Charsets.UTF_8);
                sb.append(CharStreams.toString(readerSupplier));
                sb.append("\n");
            }

        	concatenatedStringOfFiles = sb.toString();
    	}

    	return concatenatedStringOfFiles;
    }

	public FileSet getFileSet() {
		return fileSet;
	}

	/**
	 * a maven FileSet to define what files are included, excluded, etc
	 */
	public void setFileSet(FileSet fileSet) {
		files = null;
		concatenatedStringOfFiles = null;
		this.fileSet = fileSet;

	}

    public List<File> getOrderedFiles() {
        return orderedFiles;
    }

    public void setOrderedFiles(List<File> orderedFiles) {
        files = null;
        concatenatedStringOfFiles = null;
        compileIndividualFiles = false;
        this.orderedFiles = orderedFiles;
    }
}
