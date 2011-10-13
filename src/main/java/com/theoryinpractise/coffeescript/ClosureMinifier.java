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
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

/**
 * Run the Closure Compiler tool on a directory of Javascripts.
 * 
 * This class supports no configuration in its current form.
 *
 */
public class ClosureMinifier {

	public ClosureMinifier(){}
	
	public ClosureMinifier(String compilationLevel){
		this.compilationLevel = compilationLevel;
	}
	
	private String compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS.toString();
	
	public void compile(String sourceDirectory, String destFileName){
		File destFile = new File(destFileName);
		if(destFile.exists()){
			destFile.delete();
		}
		
		CompilationLevel level = null;
		try {
			level = CompilationLevel.valueOf(this.compilationLevel);
		} catch (IllegalArgumentException e) {
			throw new ClosureException("Compilation level is invalid", e);
		}

		CompilerOptions options = new CompilerOptions();
		level.setOptionsForCompilationLevel(options);

		Compiler compiler = new Compiler();
		Result results = compiler.compile(getExterns(), getInputs(sourceDirectory), options);
		
		System.out.println(results.debugLog);
		
		for(JSError error : results.errors){
			System.out.println("Closure Minifier Error:  " + error.sourceName + "  Description:  " +  error.description);
		}
		
		for(JSError warning : results.warnings){
			System.out.println("Closure Minifier Warning:  " + warning.sourceName + "  Description:  " +  warning.description);
		}
		
		if (results.success) {
			try {
				Files.write(compiler.toSource(), destFile, Charsets.UTF_8);
			} catch (IOException e) {
				throw new ClosureException("Failed to write minified file to " + destFile, e);
			}
		}else{
			throw new ClosureException("Closure Compiler Failed - See error messages on System.err");
		}
	}
	
	/**
	 * Externs are defined in the Closure documentations as:  
	 * External variables are declared in 'externs' files. For instance, the file may include 
	 * definitions for global javascript/browser objects such as window, document.
	 * 
	 * This method sneaks into the CommandLineRunner class of the Closure command line tool 
	 * and pulls the default Externs there.  This class could be modified to instead look 
	 * somewhere more relevant to the project.
	 */
	private JSSourceFile[] getExterns(){
		
		List<JSSourceFile> externs = Lists.newArrayList();
		try {
			externs = CommandLineRunner.getDefaultExterns();
			
		} catch (IOException e) {
			throw new ClosureException("Unable to load default External variables Files. The files include definitions for global javascript/browser objects such as window, document.", e);
		}
		return externs.toArray(new JSSourceFile[externs.size()]);
	}


	private JSSourceFile[] getInputs(String sourceDirectory){
		List<JSSourceFile> files = Lists.newArrayList();
		
		File srcDir = new File(sourceDirectory);
		
		if(srcDir.exists() && srcDir.isDirectory()){
			File[] filesInSrcDir = srcDir.listFiles((FileFilter) new SuffixFileFilter(".js"));
			for(File file : filesInSrcDir){
				System.out.println("Adding File:  " + file.getName());
				files.add(JSSourceFile.fromFile(file));
			}
		}
		
		return files.toArray(new JSSourceFile[files.size()]);
	}

	public String getCompilationLevel() {
		return compilationLevel;
	}

	public void setCompilationLevel(String compilationLevel) {
		this.compilationLevel = compilationLevel;
	}
}
