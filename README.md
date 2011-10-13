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
      <version>1.2.1</version>
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

Configure the destination of the resultant js file

    <outputDirectory>${project.build.directory}/coffee</outputDirectory>

Specify bare (see coffeescript compiler documentation)

    <bare>false</bare>

Finally, add JoinSets.  The id of the joinSet will be the name of the resultant javascript file.

    <joinSets>
      <joinSet>
        <id>main</id>
      </joinSet>
    </joinSets>

Optionally, specify a minified file location.  It defaults to:  
  <minifiedFile>${project.build.directory}/coffee/${project.artifactId}-${project.version}.min.js</minifiedFile>

An Example Build Section:

    <build>
      <plugins>
        <plugin>
          <groupId>com.theoryinpractise</groupId>
          <artifactId>coffee-maven-plugin</artifactId>
          <version>1.2.1</version>
          
          <configuration>
            <outputDirectory>${project.build.directory}/coffee</outputDirectory>
            <bare>false</bare>
            <minifiedFile>${project.build.directory}/coffee/${project.artifactId}-${project.version}.min.js</minifiedFile>
            <joinSets>
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
            </joinSets>
          </configuration>
          
          <executions>
            <execution>
              <id>coffee</id>
              <goals>
                <goal>coffee</goal>
              </goals>
            </execution>
          </executions>
          
        </plugin>
      </plugins>
    </build>
