# Gradle Bundler 

Gradle Bundler is a G2 infrastructure utility that is part of Liaison's Build->Bundle->Deploy tool chain. The Gradle Bundler presents some known project task interfaces for tool automation. 

## Implementation
Major categories of implementation:

### Gradle
 * Conventional Gradle War Task
 * G2 Bundler Tasks 
 * _Project Specific Bundler Tasks_

Under gradle/bundler directory, we provide gradle tasks to manage construction of an install bundle consisting of tomcat, jre, application war, and target environment configuration files. The behavior may be extended with project specific tasks, which should be placed in gradle/bundler_project.gradle and made to "dependOn" G2 bundler tasks (such as distTar).

### Convenience Scripting 
 * Git and Gradle scripting 
 * Install Script
	* _Pre-Configuration Script_
	* _Post-Configuration Script_

Of all these components, customization per project should limited to those underlined above (_Project Specific Bundler Tasks_, _Pre-Configuration Script_, & _Post-Configuration Script_).


## Build->Bundle->Deploy Usage
Normally, bundler tasks will be used by scripts outside of this project. However, they are also available for manual usage. Tasks are self documented under "Production Bundler tasks" in detail. 

####Create a tarball (tgz) bundle:
*$ gradle distTar*

Tarball bundle will be found under build/distributions/

Contents of tarball are the JRE, Tomcat, tomcat environmental configs, and war(s) for your project. These versions of each of these dependencies are stripped from folder name to facilitate simple deploy scripting. Versioning information for the bundle in total remains on the root application bundle folder name and should correspond with git tag and maven artifact versioning.

        versioned_application_folder/
        ├── jre
        ├── tomcat-environments
            ├── common
			├── continuous-integration
			├── dev
			├── production
			├── production-stage
			└── qa-stage
        └── tomcat
            └── webapps
               └── application_name.war
    

####Cleanup Dependencies
*$ gradle cleanBundleStaging*

We manage install dependencies (JRE and Tomcat) outside of the traditional build directory because we do not want to burden gradle's normal clean task with sizable container and jre downloads. If that's not appropriate behavior for your application, feel free to add a relationship in your build.gradle between the default clean task and cleanBundleStaging.

Dependencies live in install dir:
        install/
        ├── jre
        ├── tomcat-environments
            ├── common
			├── continuous-integration
			├── dev
			├── production
			├── production-stage
			└── qa-stage
        └── tomcat
            └── webapps
               └── application_name.war


