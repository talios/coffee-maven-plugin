package com.theoryinpractise.coffeescript;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.maven.model.FileSet;
import org.codehaus.plexus.util.FileUtils;

import com.google.common.collect.Lists;

/**
 * Utilities for working with Files and FileSets
 * @author daniel
 *
 */
public class FileUtilities {

	@SuppressWarnings("unchecked")
	public static List<File> getFilesFromFileSet(FileSet fileSet) throws IOException {
		List<File> files = Lists.newArrayList();
		
		try {
            File directory = new File(fileSet.getDirectory());
            String includes = getCommaSeparatedList(fileSet.getIncludes());
            String excludes = getCommaSeparatedList(fileSet.getExcludes());
            files = FileUtils.getFiles(directory, includes, excludes);
        } catch (IOException e) {
                throw new IOException("Unable to access files in fileSet", e);
        }
		
		return files;
	}

    /**
     * Turn a list of Strings into a concatenated string of filenames
     */
    public static String getCommaSeparatedList(List<String> list) {
        StringBuffer sb = new StringBuffer();
        
        for (Iterator<String> i = list.iterator(); i.hasNext(); ){
        	sb.append(i.next());
            if (i.hasNext()) {
            	sb.append(",");
            }
        }
                
        return sb.toString();
    }
    
    /**
     * Turn a list of files into a comma separated list of filenames
     */
    public static String getCommaSeparatedListOfFileNames(List<File> fileList){
    	StringBuffer sb = new StringBuffer();
    	
    	for(Iterator<File> i = fileList.iterator(); i.hasNext(); ){
    		sb.append(i.next().getAbsolutePath());
    		if(i.hasNext()){
    			sb.append(",");
    		}
    	}
    	
    	return sb.toString();
    }

	/**
	 * Convenience Method for turn a string path containing .js files into a list of files to be processed
	 * @param sourceDirectory
	 */
	public static List<File> directoryToFileList(String sourceDirectory){
		List<File> files = Lists.newArrayList();
		File srcDir = new File(sourceDirectory);
		
		if(srcDir.exists() && srcDir.isDirectory()){
			File[] filesInSrcDir = srcDir.listFiles((FileFilter) new SuffixFileFilter(".js"));
			for(File file : filesInSrcDir){
				files.add(file);
			}
		}
		
		return files;
	}
	
	/**
	 * Convenience Method for turning a string path for a single file into a list of files to be processed
	 * @param sourceFile
	 */
	public static List<File> fileToFileList(String sourceFile){
		List<File> files = Lists.newArrayList();
		File srcFile = new File(sourceFile);
		
		if(srcFile.exists() && srcFile.isFile()){
			files.add(srcFile);
		}
		
		return files;
	}
	
	/**
	 * Convenience Method for turning a FileSet into a list of files to be processed
	 */
	public static List<File> fileSetToFileList(FileSet fileset) throws IOException{
		List<File> files = Lists.newArrayList();
		
		files.addAll(FileUtilities.getFilesFromFileSet(fileset));
		
		return files;
	}
}
