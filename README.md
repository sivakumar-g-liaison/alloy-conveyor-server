# Liaison G2 Build Skeleton
====

## About
----
This is a project intended to be the foundation of G2 project interaction with the rest of Liason's build and deploy environment.

This project is a fork of the Netflix gradle-template project. (See https://github.com/Netflix/gradle-template or git@github.com:Netflix/gradle-template.git)

This project also has the scripting and configuration required for building in continuous integration, and publishing artifacts to Liaison's internal maven repository.


### Features

The environment setup scripting will deploy the following.
----
* curl
    * Windows - this project includes windows curl executable
    * Linux - yum (or apt-get) install curl
* JDK 7 (Windows and Linux)
    * unzipped into a project local tools folder
    * platform specific deployment
* 7Zip (Windows only)

The following environment varibles will be set:
----
* ARTIFACT_REPO_URL     - the root URL for the repository where environment dependencies are archived
* JAVA_VER              - the Java release number (i.e. 7)
* JAVA_REL_VER          - the release patch version number (i.e. the "10" in 1.7.0_10)
* JAVA_MAJOR_VER        - the major.minor identifier (i.e. 1.7) (TODO - make this name in line with its function)
* JAVA_MAJOR_MINOR_VER  - the major.minor.revision identifier (i.e. 1.7.0) (TODO - same as above)
* JAVA_FULL_VERSION     - the full major.minor.revision_build identifier (i.e. 1.7.0_10)


To run
----
* setenv.sh (setenv.bat) - will bootstrap build dependencies via curl (HTTP GET) and install them.
    * very useful for Jenkins integration
    * creates a local environment gradle.properties from a template
    * needs some work to NOT automatically install dependencies without asking
    
* do.sh (do.bat)        - will execute setenv.sh (setenv.bat) and then the specified command line in a sub-process.


## Keeping Up With Change
====

This project is intended to be the distribution point for uniformly applying change to the build environments across projects. As updates are put into the gradle-template project, projects will need to merge in those changes.

In order to do so, a project must have a tracking branch back to this project. If there is not already a tracking branch defined, you can do the following to merge this project into your project and set up a tracking branch.

### Merging

This will pull in the template:

```
git remote add --track master build git@gitlab.hs.com:g2/liaison-gradle-template.git
git fetch build
git merge build/master
```

WARNING! Merging into an established project may result in merge conflict.


### Updating 

To merge updates, run this using the branch you choose when you first setup the project:

Once a tracking branch has been established, the following will pull in updates from this project:

````
git fetch build
git merge build/master
````





