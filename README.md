Coffeescript Maven Plugin

Configuration options shown are default values and can be ignored for normal use.

    <build>
      <plugins>
        <plugin>
          <groupId>com.theoryinpractise</groupId>
          <artifactId>coffee-maven-plugin</artifactId>
          <version>1.1.3</version>
          <configuration>
            <coffeeDir>src/main/coffee</coffeeDir>
            <outputDirectory>${project.build.directory}/coffee</outputDirectory>
            <bare>false</bare>
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

Coffee files can be joined into a single .js file but configuring <joinSet/> elements:

    <configuration>
      <joinSets>
        <joinSet>
          <id>main</id>
          <files>
            <file>file1.coffee</file>
            <file>file2.coffee</file>
          </files>
        </joinSet>
      </joinSets>
    </configuration>

The above configuration will join file1.coffee, and file2.coffee found in the <coffeeDir/>
and compile them into the file main.js.  Any other .coffee files found will compile into
their respective .js file as normal.
