package com.theoryinpractise.coffeescript;

import com.google.common.collect.Lists;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
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
 * Minify JavaScript with Maven
 */
@Mojo(name = "minify", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class JavaScriptMinifierMojo extends AbstractMojo {

  @Parameter(required = true, property = "minifiedFile", defaultValue = "${project.build.directory}/coffee/${project.artifactId}-${project.version}.min.js")
  private String minifiedFile;

  /**
   * Location of the Files to Minify.  Defaults to ${build.directory}/coffee
   */
  @Parameter(defaultValue = "${project.build.directory}/coffee")
  private File directoryOfFilesToMinify;

  /**
   * The set of files that should be minified.  Be sure to specify the path to the compiled
   * <p/>
   * Only one or the other of setOfFilesToMinify or directoryOfFilesToMinify should be specified.  Only setOfFilesToMinify is used if both are specified.
   */
  @Parameter
  private FileSet setOfFilesToMinify;

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      getLog().info("Minifying all Javascript Files in the Output Directory");
      ClosureMinifier minifier = new ClosureMinifier(getLog());

      List<File> filesToMinify;
      if (null != setOfFilesToMinify) {
        getLog().debug("Configured a fileset for minification");
        filesToMinify = FileUtilities.fileSetToFileList(setOfFilesToMinify);
      } else {
        getLog().debug("Configured a directory for minification");
        filesToMinify = FileUtilities.directoryToFileList(directoryOfFilesToMinify.getAbsolutePath());
      }

      //check for dest file in source files, if present remove it.
      List<File> filesToMinifyMinusDestFile = Lists.newArrayList();
      for (File file : filesToMinify) {
        if (!file.getAbsolutePath().equals(minifiedFile)) {
          filesToMinifyMinusDestFile.add(file);
        }
      }

      getLog().info("About to minify the following files:  " + FileUtilities
          .getCommaSeparatedListOfFileNames(filesToMinifyMinusDestFile));

      minifier.compile(filesToMinifyMinusDestFile, minifiedFile);
    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
