package com.theoryinpractise.coffeescript;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
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
 *
 *
 * Run the Closure Compiler tool on a directory of Javascripts.
 *
 * This class supports no configuration in its current form.
 *
 */
public class ClosureMinifier {

	public ClosureMinifier(Log logger){
		this.logger = logger;
	}

	public ClosureMinifier(String compilationLevel, Log logger){
		this.compilationLevel = compilationLevel;
		this.logger = logger;
	}

	private Log logger;
	private String compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS.toString();

	public void compile(List<File> filesToCompile, String destFileName){
		File destFile = prepareDestFile(destFileName);

		Compiler compiler = new Compiler();
		Result results = compiler.compile(getExterns(), getInputs(filesToCompile), getCompilerOptions());

		logger.debug(results.debugLog);
		for(JSError error : results.errors){
			logger.error("Closure Minifier Error:  " + error.sourceName + "  Description:  " +  error.description);
		}
		for(JSError warning : results.warnings){
			logger.info("Closure Minifier Warning:  " + warning.sourceName + "  Description:  " +  warning.description);
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
	 * Prepare the Destination File, Remove if it already exists
	 * @param destFileName
	 */
	private File prepareDestFile(String destFileName){
		File destFile = new File(destFileName);
		if(destFile.exists()){
			destFile.delete();
		}
		return destFile;
	}

	/**
	 * Prepare options for the Compiler.
	 */
	private CompilerOptions getCompilerOptions(){
		CompilationLevel level = null;
		try {
			level = CompilationLevel.valueOf(this.compilationLevel);
		} catch (IllegalArgumentException e) {
			throw new ClosureException("Compilation level is invalid", e);
		}

		CompilerOptions options = new CompilerOptions();
		level.setOptionsForCompilationLevel(options);

		return options;
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

	private JSSourceFile[] getInputs(List<File> filesToProcess){
		List<JSSourceFile> files = Lists.newArrayList();

		for(File file : filesToProcess){
			files.add(JSSourceFile.fromFile(file));
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
