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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Wrap a Maven fileset to add properties for describing the group of files
 *
 */
public class JoinSet {

    private String id;

    private FileSet fileSet;

    public String getId() {
        return id;
    }

    /**
     * An id to group the Files in the Fileset.  This id becomes the name of the javascript file that is output.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Pulls the list of files that will be used from the fileset.
     * @throws MojoExecutionException
     */
    public List<File> getFiles() throws MojoExecutionException {
    	try {
                File directory = new File(getFileSet().getDirectory());
                String includes = getCommaSeparatedList(this.getFileSet().getIncludes());
                String excludes = getCommaSeparatedList(this.getFileSet().getExcludes());
                return FileUtils.getFiles(directory, includes, excludes);
                
        } catch (IOException e) {
                throw new MojoExecutionException("Unable to get paths to source files", e);
        }
    }
    
    /**
     * Helper utility to turn a list of File into a concatenated string of filenames
     */
    protected String getCommaSeparatedList(List<String> list) {
        StringBuffer sb = new StringBuffer();
        
        for (Iterator<String> i = list.iterator(); i.hasNext(); ){
        	sb.append(i.next());
            if (i.hasNext()) {
            	sb.append(",");
            }
        }
                
        return sb.toString();
    }

	public FileSet getFileSet() {
		return fileSet;
	}

	/**
	 * a maven FileSet to define what files are included, excluded, etc
	 */
	public void setFileSet(FileSet fileSet) {
		this.fileSet = fileSet;
	}
}
