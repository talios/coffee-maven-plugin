## [2.0.2] - 2026-09-20

### 🐛 Bug Fixes

- Updated json, commons-io, and plexus-utils for security issues

### 📚 Documentation

- Updated CHANGELOG.md

### 💼 Other

- Deleted temp xml poms

Change-Id: I3283002cec91053e61b38a387bced6c96a6a6964
## [2.0.1] - 2026-09-19

### 💼 Other

- Use latest versions
- Merge branch 'hotfix/readme_update_ver' of github.com:nryotaro/coffee-maven-plugin into nryotaro-hotfix/readme_update_ver

Change-Id: I8df18e30a4f9c36e87439fafe0cfc156d925e5bb
- Updated README

Change-Id: Ic645bd4109da109a398a869d061e4112ed8b3c13
- Merge branch 'nryotaro-hotfix/readme_update_ver'

Change-Id: I47ad531b1ed84813e8cf4741d750aedb1e9ba58c
- Merge branch 'release/1.4.18'

Change-Id: I92620eb4b50af4dd7a958df20694ba48e453f5e6
- Merge branch 'master' into develop

Change-Id: Ia758503d704bef18e3dbdfc9d97ca38aec779524
- Updated dependencies

Change-Id: I48dddebc176c4b47bd6837c4199b2ed8e1dd18a9
- Replace Rhino with GraalJS as the CoffeeScript compiler runtime

Rhino (com.github.tntim96:rhino 1.7R5pre05) has been unmaintained for
years; swap it for GraalJS so the plugin runs on a supported JavaScript
engine.

CoffeeScriptCompiler is rewritten against org.graalvm.polyglot:

  * A sandboxed Context (HostAccess.NONE, no host class lookup) replaces
    Rhino's Context/Scriptable pair.
  * Rhino's commonjs.module.Require has no GraalJS equivalent, so the
    bundled coffee-script sources are loaded by a small CommonJS shim: it
    reads /coffee-script-<version>/<name>.js from the classpath, wraps it
    in the usual (function (exports, require, module, __filename,
    __dirname) {...}) envelope and invokes it with `this` bound to
    exports.  Modules are cached before evaluation so circular requires
    see the partial exports.  Only relative ids resolve - coffee-script's
    require('fs')/require('module') calls sit on code paths the compiler
    never takes, which is the same assumption Rhino's sandboxed require
    relied on.
  * compile() now calls the module's compile function directly with a real
    JS options object rather than evaluating a formatted snippet, which
    also removes the filename-into-a-JS-string-literal injection.
  * Added close()/AutoCloseable so the polyglot context can be released.

Behaviour change: the v3 source map is now emitted as raw JSON.
coffee-script already returns a JSON string, and the old code ran it
through ObjectMapper.writeValueAsString, double-encoding it into a quoted,
escaped blob - the generated .map files were not valid source maps.

Build changes:

  * Dependencies: rhino -> org.graalvm.polyglot:polyglot plus
    org.graalvm.js:js-language and org.graalvm.truffle:truffle-runtime
    24.2.2.  The two concrete jars are used instead of the
    org.graalvm.polyglot:js-community aggregator because that aggregator
    is pom-typed and maven-shade-plugin drops pom dependencies from the
    dependency-reduced-pom, leaving consumers with no JavaScript language
    on the classpath.
  * maven.compiler.release 8 -> 21, as required by GraalJS 24.2.  That in
    turn needs maven-plugin-plugin 3.6.4 -> 3.15.1, whose ASM can read
    class file major version 65.
  * maven-shade-plugin 2.1 -> 3.6.0, and the stale commented-out Rhino
    relocation blocks are removed.
  * jackson-core/jackson-databind dropped; they existed only for the
    source map serialisation.

Two pre-existing breakages had to be fixed to get the suite green; neither
is caused by the engine swap:

  * FileUtilities imports org.codehaus.plexus.util.FileUtils but
    plexus-utils was never declared - it was riding on Maven core's
    exported packages, which current Maven no longer provides.
  * orderedfilesettest's test asks for the "JavaScript" ScriptEngine,
    which has not shipped in the JDK since Nashorn was removed in 15.  The
    IT project now pulls in GraalJS's js-scriptengine.

Verified with 28 unit tests across all 14 bundled coffee-script versions
(compile and source-map paths), all 6 invoker integration tests, and spot
checks of generated JS for 1.2.0/1.6.1/1.10.0 over the bare, header,
literate and syntax-error cases.

Change-Id: I9c2e77492fff37a9ee98d4f673d508c238bacabf
Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
- Use njord/nisse for deployment

Change-Id: I014a255b284ebf3f41cad2c348e7cb476a6a6964
- Fix Javadoc errors and warnings

The Apache license boilerplate sat inside javadoc comments using the
invalid <p/> tag, which javadoc rejects; move it into plain block
comments and leave a real class javadoc behind it.

Also correct JoinSet.getFiles's @throws, which named an exception it
never throws, and fill in the missing @param/@return/@throws tags and
member comments so mvn javadoc:javadoc runs without errors or warnings.

Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Change-Id: I5486ce11112661a5a6377d5ecac793e16a6a6964
## [coffee-maven-plugin-1.4.18] - 2023-04-14

### 💼 Other

- Merge branch 'release/1.4.17'

Change-Id: I1ecd433cac76759209d61d51669acf98c1c562db
- Merge branch 'master' into develop

Change-Id: Id036d5300ed329b55301e0f68b4f9fbf0ddfc140
- Bump json from 20220924 to 20230227

Bumps [json](https://github.com/douglascrockford/JSON-java) from 20220924 to 20230227.
- [Release notes](https://github.com/douglascrockford/JSON-java/releases)
- [Changelog](https://github.com/stleary/JSON-java/blob/master/docs/RELEASES.md)
- [Commits](https://github.com/douglascrockford/JSON-java/commits)

---
updated-dependencies:
- dependency-name: org.json:json
  dependency-type: direct:production
...

Signed-off-by: dependabot[bot] <support@github.com>
- Merge pull request #39 from talios/dependabot/maven/org.json-json-20230227

Bump json from 20220924 to 20230227
- Use HTTPS instead of HTTP to resolve dependencies

This fixes a security vulnerability in this project where the `pom.xml`
files were configuring Maven to resolve dependencies over HTTP instead of
HTTPS.

Signed-off-by: Jonathan Leitschuh <Jonathan.Leitschuh@gmail.com>
- Merge pull request #36 from JLLeitschuh/fix/JLL/use_https_to_resolve_dependencies

[SECURITY] Use HTTPS to resolve dependencies in Maven Build
## [coffee-maven-plugin-1.4.17] - 2022-11-15

### 💼 Other

- Updated major dependencies, minor code cleanup

Change-Id: Id09db607e1aaead5d6ba0368fa5d3e79ba24c0dd
- Updated dependencies

Change-Id: Ie1856c277a12b952f9cef31e1f7c5bdfd83956cf
## [coffee-maven-plugin-1.4.16] - 2015-09-09

### 💼 Other

- Updated to include/default to CoffeeScript 1.10.0

Fixes #35

Change-Id: I1a73619e7bb6eac3b77b5bd5170b5ce028e69696
- Add logging for creation of output path.

Fixes #33 - it already created the path, we just log this now.

Change-Id: I5ed420807da384c1814727b3ff674e514af4623b
## [coffee-maven-plugin-1.4.15] - 2015-06-03

### 💼 Other

- Added CoffeeScript 1.9.3 Support

Change-Id: I5604ca0b208e6060b2374ec28f147b8a7750995d
## [coffee-maven-plugin-1.4.14] - 2015-04-30

### 💼 Other

- Merge branch 'master' into develop

* master:
- Added coffescript 1.9.2 support.

Change-Id: Icc6ac83a17d044f4e776eaa7cf214471b19dcefa
## [coffee-maven-plugin-1.4.13] - 2015-03-10

### 💼 Other

- Updated to CoffeeScript 1.9.1

From the changelog:

1.9.1 — FEBRUARY 18, 2015
* Interpolation now works in object literal keys (again). You can use
	this to dynamically name properties.
* Internal compiler variable names no longer start with underscores.
  This makes the generated JavaScript a bit prettier, and also fixes an
  issue with the completely broken and ungodly way that AngularJS
  "parses" function arguments.
* Fixed a few yield-related edge cases with yield return and yield throw.
* Minor bug fixes and various improvements to compiler error messages.

Change-Id: I787a5042e2937e33dc1183871ad6c4045f74f50e
## [coffee-maven-plugin-1.4.12] - 2015-01-30

### 💼 Other

- Added CoffeeScript 1.9.0 support

Fixes #31

Change-Id: I2ed3592d2f1eabb38f3d9c25211ef6d17f99ba1e
## [coffee-maven-plugin-1.4.11] - 2014-08-28

### 💼 Other

- Added coffeescript 1.8.0 as default

Change-Id: I6aedab5f6bc09a98c113ba9713bde89864f0fb12
## [coffee-maven-plugin-1.4.10] - 2014-02-08

### 💼 Other

- Updated to Coffee-Script 1.7.1

Also updated
* maven-compiler-plugin
* closure compiler version
* switch to newer rhino build ( experimental? )
* updated guava and jackson deps

Change-Id: Ibc8bae67118d635ae0df9010217cf10f1eb910fb
- Added support for coffee <refills> for multiple src paths

Fixes #9.

* Also switched to JDK Annotations
* Updated maven release plugin config to work with newer git.

Change-Id: I1ef8477bd2fa824a2d0d2915770add009fe0fc64
- Merge branch 'feature/coffee-script-1.7.1' into develop

* feature/coffee-script-1.7.1:
  Added support for coffee <refills> for multiple src paths
  Updated to Coffee-Script 1.7.1
## [coffee-maven-plugin-1.4.9] - 2013-10-20

### 💼 Other

- Fixing #26 - Support for ordered joinsets

Even tho a "set" is inherantly an unordered collection of files,
it can be desired to join files together in a specific order to
allow proper Javascript evaluation when compiled into a single
function/closure.

This patch adds a new <orderedFiles/> list of <file/> entries to a
<joinset/> in additional to the existing fileset.

In order for this to work properly however, <compileIndividualFiles/>
must be false ( the default value ).

Change-Id: I63c36a611b9f38a48a6e298469a74ee67fedf97e
- Added compileIndividualFiles to each joinset

Also updated the README.

Change-Id: I564f9cc0dcabdd64a928919f7cbf6ff48e45c5bc
- Merge branch 'feature/ordered-joinsets' into develop

* feature/ordered-joinsets:
  Added compileIndividualFiles to each joinset
  Fixing #26 - Support for ordered joinsets
## [coffee-maven-plugin-1.4.8] - 2013-10-12

### 💼 Other

- Wtf
- Reserve sub directories
- Added integration test and updated README.

Updated README and added IT test for directory preservation.
- Merge branch 'feature/preserve-dirs' into develop

* feature/preserve-dirs:
  Added integration test and updated README.
  reserve sub directories
- Added SourceMaps refference
- Merge pull request #25 from treeno/develop

Added SourceMaps refference
- Added coffeescript 1.6.3 support

- 1.6.3 is also now the default version
## [coffee-maven-plugin-1.4.7] - 2013-03-05

### 💼 Other

- Added support for CS 1.6.1 and sourcemaps

Enable sourcemap support by setting <map>true</map> in your configuration.
## [coffee-maven-plugin-1.4.6] - 2013-03-01

### 💼 Other

- Minor fix for literate compilation with named joinsets
## [coffee-maven-plugin-1.4.5] - 2013-03-01

### 💼 Other

- Added support for litcoffee
## [coffee-maven-plugin-1.4.4] - 2013-02-27

### 💼 Other

- Update develop
- Added coffeescript 1.4.0 and 1.5.0 ( default )
## [coffee-maven-plugin-1.4.3] - 2012-06-13

### 💼 Other

- Enable full optimization.
## [coffee-maven-plugin-1.4.2] - 2012-06-12

### 💼 Other

- Added coffeescript 1.3.1
- Added coffeescript 1.3.1 to the mojo
- Updated test to cover all supported versions
- Use sandboxed param
- Updated rhino head from 1.8 branch.

This still clashes with a rhino from google closure somewhere...
- Add support for Coffeescript 1.3.3

Adding Coffeescript 1.3.3 files and updated
acceptable versions list.
- Merge pull request #15 from jonbca/develop

Add support for Coffeescript 1.3.3
- Using Rhino 1.7R4 SNAPSHOT.
- Set 1.3.3 as default.
## [coffee-maven-plugin-1.4.1] - 2012-03-16

### 💼 Other

- Added ability to configure optional coffeeOutputDirectory override for a specific JoinSet. This property overrides the parent coffeeOutputDirectory if that is also specified.
- Updated rhino head.
- Updated readme for coffeeOutputDirectory setting per joinset.
## [coffee-maven-plugin-1.4.0] - 2012-01-22

### 📚 Documentation

- Documentation update

### 💼 Other

- Edited README.md via GitHub
- CoffeeScript 1.1.3 support.
- Edited README.md via GitHub
- Remove tests which are no longer applicable
- Bump version of plugin, add requirement for java5, add library for maven model, update junit
- Add license found elsewhere in project, extract out compiler version to var, add comment
- Add license found elsewhere in project, add comment
- Use FileSets instead of Strings to add a file, rearranged things a bit as well
- A test for fileSets
- Ignore the maven target directory for commits
- Update the doc for the new functionality
- Playing with README styling
- Playing with README styling
- Revise the test project name
- Update the test a bit to have more interesting data
- Make the Exception messages a little more informative
- Add Closure Minification Support
- Bump revision number to reflect new functionality, rename minified file, update readme
- Make test more robust - add some actual classes for coffee to compile
- Use same var for coffescript compiler name, Make Error message slightly more descriptive
- Couldn't straight concat the CoffeeScript files, so I added a new line after each file
- Increase revision number and add deployment destination
- Bump version
- Update documentation
- Extract some FileUtilities into a static library
- Make JoinSets richer as a model - move some functionality out of other classes into here to make its interface less leaky
- Many changes, introduced JavaScriptMinifierMojo, but unable to figure out how to run it correctly with multiple maven goals in the same plugin project
Moved some functionality to various supporting classes like FileUtilities
Changed some parameter names
Updated documentation
JavaScriptMinification now takes a fileset or a directory
- Update test for new version of plugin
- Merge branch 'master' of https://github.com/danieldbower/coffee-maven-plugin into feature/danielspatches

* 'master' of https://github.com/danieldbower/coffee-maven-plugin: (27 commits)
  update test for new version of plugin
  many changes, introduced JavaScriptMinifierMojo, but unable to figure out how to run it correctly with multiple maven goals in the same plugin project Moved some functionality to various supporting classes like FileUtilities Changed some parameter names Updated documentation JavaScriptMinification now takes a fileset or a directory
  make JoinSets richer as a model - move some functionality out of other classes into here to make its interface less leaky
  Extract some FileUtilities into a static library
  update documentation
  bump version
  increase revision number and add deployment destination
  Couldn't straight concat the CoffeeScript files, so I added a new line after each file
  Use same var for coffescript compiler name, Make Error message slightly more descriptive
  make test more robust - add some actual classes for coffee to compile
  documentation update
  bump revision number to reflect new functionality, rename minified file, update readme
  Add Closure Minification Support
  Make the Exception messages a little more informative
  Update the test a bit to have more interesting data
  revise the test project name
  playing with README styling
  playing with README styling
  update the doc for the new functionality
  ignore the maven target directory for commits
  ...
- Removed minifier from compile mojo and added @phase annotations.

Updated Apache 2.0 license headers to claim copyright to myself
as I wrote the original code.
- Updated copyright notice replacig The Apache Foundation with myself,
listing Daniel Bower as contributing author.
- Fixed javadoc and removed minification fields from compile goal.
- Merge branch 'feature/danielspatches' into develop

* feature/danielspatches: (30 commits)
  Fixed javadoc and removed minification fields from compile goal.
  Updated copyright notice replacig The Apache Foundation with myself, listing Daniel Bower as contributing author.
  Removed minifier from compile mojo and added @phase annotations.
  update test for new version of plugin
  many changes, introduced JavaScriptMinifierMojo, but unable to figure out how to run it correctly with multiple maven goals in the same plugin project Moved some functionality to various supporting classes like FileUtilities Changed some parameter names Updated documentation JavaScriptMinification now takes a fileset or a directory
  make JoinSets richer as a model - move some functionality out of other classes into here to make its interface less leaky
  Extract some FileUtilities into a static library
  update documentation
  bump version
  increase revision number and add deployment destination
  Couldn't straight concat the CoffeeScript files, so I added a new line after each file
  Use same var for coffescript compiler name, Make Error message slightly more descriptive
  make test more robust - add some actual classes for coffee to compile
  documentation update
  bump revision number to reflect new functionality, rename minified file, update readme
  Add Closure Minification Support
  Make the Exception messages a little more informative
  Update the test a bit to have more interesting data
  revise the test project name
  playing with README styling
  ...
- Mention minify goal in the README
- Mention new <version/> element to configure coffee version.
- Updated ignore file
- Added initial requirejs version
- When compiling individually - actually copy individually.
- Only compile once

When compiling individually, collate the output and save it, rather
than recompile all at once again.
- Updated javadoc and added newline
- Returned support for auto join set discovery
- Restored sonatype repo, now using rhino head
- Added rhino head build as a repo
- Switched to requirejs and coffeescript 1.1.3
- Merge branch 'feature/requirejs' into develop

* feature/requirejs:
  Switched to requirejs and coffeescript 1.1.3
  Added rhino head build as a repo
  Restored sonatype repo, now using rhino head
  Returned support for auto join set discovery
  Updated javadoc and added newline
  Only compile once
  When compiling individually - actually copy individually.
  Added initial requirejs version
  Updated ignore file
- Updated to experimental head rhino, enabled compilation.

Sadly - fails to compile :(
)
- Updated to coffee-script 1.2.0, added shade for closure
- Added gpg plugin back to pom
## [coffee-maven-plugin-1.1.3] - 2011-08-08

### 💼 Other

- Updated pom version in readme
- Merge branch 'master' into develop

* master:
  Updated pom version in readme
- Integrated coffeescript 1.1.2 directly.

Fixes #6.
- Added basic IT test for default operation/usage.
- Added check for null joinSets
- Merge branch 'master' of https://github.com/wpraet/coffee-maven-plugin into feature/coffeescript112

* 'master' of https://github.com/wpraet/coffee-maven-plugin:
  added check for null joinSets
- Fixed up indents on pulled code.

Fixes #7
- Merge branch 'feature/coffeescript112' into develop

* feature/coffeescript112:
  Fixed up indents on pulled code.
  Added basic IT test for default operation/usage.
  Integrated coffeescript 1.1.2 directly.
  added check for null joinSets
- Removed deps on original upstream coffeescript wrapper.

Fixes #3
- Updated READEME version number.
- Added coffeescript 1.1.2.js file
- Actually commit the changes to the pom :)
## [coffee-maven-plugin-1.1.2] - 2011-07-30

### 💼 Other

- Fixes #2 - adds <joinSets/> as a configuration setting.
- Updated joinset compilation with better logging.
- Merge branch 'feature/2-join-files' into develop

* feature/2-join-files:
  Updated joinset compilation with better logging.
  Fixes #2 - adds <joinSets/> as a configuration setting.
- Updated README for joinsets.
## [coffee-maven-plugin-1.1.1] - 2011-05-18

### 💼 Other

- Bare is the default - fixed README
- Fixed version number in README.
- Compilation fails with .coffee files in subdirectories
## [coffee-maven-plugin-1.1.0] - 2011-05-04

### 💼 Other

- Initial commit of coffeescript maven plugin.
- Added readme
- Updated pom
- Fixed artifact ref.
- Fixed artifact name
- Added description and url to pom
