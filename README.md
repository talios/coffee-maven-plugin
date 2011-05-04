Coffeescript Maven Plugin

    <build>
      <plugins>
        <plugin>
          <groupId>com.theoryinpractise</groupId>
          <artifactId>coffee-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
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

Enjoy.

