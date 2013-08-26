## General TODOs
TODO fix regex for war rename!!!!
TODO remove application plug and fix tasks
TODO check/fix test/integration/soapui
TODO clean and polish
TODO clean docs
TODO add preApplicationContainerConfig.py and postApplicationContainterConfig.py hooks to installer and bundler
TODO make service nucleus have example of project.gradle w/ stagePersisence hook
TODO test when tomcat-environments missing and error


TODO test when tomcat-environments missing

# Gradle Bundler 
## (a git submodule)

Gradle Bundler is a G2 infrastructure utility that is part of Liaison's Build->Bundle->Deploy tool chain. The Gradle Bundler presents some known project task interfaces for tool automation.

## Build->Bundle->Deploy Usage
See _git submodule_ and _gradle integration_ sections below for implementation details.

Normally, bundler tasks will be used by the gitTagBuildAndBundle.sh script (See BuildBundleDeploy git repo). However they are also available for manual use:

####Create a tarball (tgz) bundle:
*$ gradle distTar*

Tarball bundle will be found under build/distributions/

Contents of tarball are the JRE, Tomcat, tomcat environmental configs, and war(s) for your project. These versions of each of these dependencies are stripped from folder name to facilitate simple deploy scripting. Versioning information for the bundle in total remains on the root application bundle folder name and should correspond with git tag and maven artifact versioning.

TODO: add versions.txt to root folder of stripped versions.

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


## Gradle Integration

TODO detail project.gradle
TODO talk about properties
TODO talk war tasks


## Git Submodule Integration

TODO how to set up submodule











hi
