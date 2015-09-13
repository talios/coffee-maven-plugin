Coffeescript Maven Plugin

Changes from 1.1.x versions:
CoffeeScript files are joined together by default by JoinSets.
JoinSets now contain Maven FileSets instead of listing
Requires Java5 or above
Creates a minified version using the Closure Compiler


USAGE:

Add the main plugin elements:  groupId, artifactId, and version

    <plugin>
      <groupId>com.theoryinpractise</groupId>
      <artifactId>coffee-maven-plugin</artifactId>
      <version>1.4.15</version>
    </plugin>

Add the execution goal

    <executions>
      <execution>
        <id>coffee</id>
        <goals>
          <goal>coffee</goal>
        </goals>
      </execution>
    </executions>

Configure the destination of the resultant js files

    <coffeeOutputDirectory>${project.build.directory}/coffee</coffeeOutputDirectory>

Specify bare (see coffeescript compiler documentation)

    <bare>false</bare>

It can be tough to debug compilation errors with the files joined together.  If you would like to compile the Files individually

    <compileIndividualFiles>true</compileIndividualFiles>

If you wish to preserve the directory structure layout, so that compiled .js files follow their .coffee sources:

    <preserveSubDirectory>true</preserveSubDirectory>
    
Source Maps can be activated using the following flag:
    
    <map>true</map>
    
If Source Maps are activated, the compiler will by default add the header pragma to the JavaScript-artifacts that is used by a debugger to find Source Maps. You can deactivated the header by using:

    <header>false</header>

You can specify a CoffeeScript version in a configuration

    <version>1.9.3</version>

Finally, add JoinSets.  The id of the joinSet will be the name of the resultant javascript file.

    <coffeeJoinSets>
      <joinSet>
        <id>main</id>
        <coffeeOutputDirectory>${project.build.directory}/other-directory</coffeeOutputDirectory>
        <compileIndividualFiles/>
        <orderedFiles/>
        <fileSet>
        </fileSet>
      </joinSet>
    </coffeeJoinSets>

If a defined joinset is to contain order dependant source files, they can be listed in the `<orderedFiles/>`'s element and
they will be included at the start of the resulting fileset .js file in the order they're mentioned. Ordered files will also be excluded
from the provided fileset.

When ordered files are included in a joinset, it's `<compileIndividualFiles/>` field is set to `false`, overriding any global setting
of the plugin configuration.

The output directory for an individual joinsets can also be overridden by setting <coffeeOutputDirectory/> inside
the <joinSet/> element.

Optionally, specify a minified file location.  It defaults to:
    <minifiedFile>${project.build.directory}/coffee/${project.artifactId}-${project.version}.min.js</minifiedFile>

Finally, you can specify a directory of files to minify into one file,
    <directoryOfFilesToMinify></directoryOfFilesToMinify>

or a fileset of files to minify into one file

    <setOfFilesToMinify>
    </setOfFilesToMinify>

An Example Build Section:

    <build>
      <plugins>
        <plugin>
          <groupId>com.theoryinpractise</groupId>
          <artifactId>coffee-maven-plugin</artifactId>
          <version>1.4.15</version>

          <configuration>
             <minifiedFile>${project.build.directory}/coffee/${project.artifactId}-${project.version}.min.js</minifiedFile>
             <setOfFilesToMinify>
             	<directory>${project.build.directory}/coffee</directory>
             	<includes>
             		<include>*.js</include>
             	</includes>
             	<excludes>
             		<exclude>alternate.js</exclude>
             	</excludes>
             </setOfFilesToMinify>

             <version>1.9.3</version>
             <coffeeOutputDirectory>${project.build.directory}/coffee</coffeeOutputDirectory>
             <compileIndividualFiles>true</compileIndividualFiles>
             <coffeeJoinSets>
                <joinSet>
                    <id>main</id>
                    <fileSet>
                    	<directory>${basedir}/src/main/coffee</directory>
                    	<includes>
                    		<include>**/*.coffee</include>
                    	</includes>
                    	<excludes>
                    		<exclude>**/jointest3.*</exclude>
                    	</excludes>
                    </fileSet>
                </joinSet>
                <joinSet>
                    <id>alternate</id>
                    <fileSet>
                    	<directory>${basedir}/src/main/coffee</directory>
                    	<includes>
                    		<include>**/jointest3.*</include>
                    	</includes>
                    </fileSet>
                </joinSet>
             </coffeeJoinSets>
          </configuration>

          <executions>
            <execution>
              <id>coffee</id>
              <goals>
                <goal>coffee</goal>
                <goal>minify</goal>
              </goals>
            </execution>
          </executions>

        </plugin>
      </plugins>
    </build>
