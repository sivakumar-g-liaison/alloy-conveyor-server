<div>

<a href="https://travis-ci.org/robertjchristian/service-nucleus">
<img src="https://travis-ci.org/robertjchristian/service-nucleus.png?branch=master" />
</a>

</div>

<h1>service-nucleus</h1>

Service-Nucleus is largely made up of two core projects, <a href="https://github.com/Netflix/karyon">NetFlix Karyon</a> and <a href="https://github.com/robertjchristian/angular-enterprise-seed">Angular-Enterprise-Seed</a>.

### About
THIS SPECIFIC PROJECT IS FOR OFS PROTOTYPES
Service-nucleus is a starting point for developing homogenous, SOA/Enterprise-friendly webservices.  It is a fork of the Netflix/karyon project found on github, with added functionality, examples, and a UI.  Out of the box, this project offers:

* Rich Client-MVC-Based UI built on Angular with:
 * Twitter Bootstrap and FontAwesome styling
 * Pre-built components (modals, pagination, grids, etc)
 * <i>If you are interested only in the UI, check out <a href="https://github.com/robertjchristian/angular-enterprise-seed">Angular-Enterprise-Seed</a>.</i>
* Java REST webservice development (Jersey stack and working examples)
* Dynamic webservice development (runtime-deployment of Javascript-defined webservices)
* Asynchronous logging (Log4J2)
* Properties management (via Netflix Archaeus)
* JMX monitoring made easy with annotations, and metrics reporting (via Netflix Servo)
* Framework-level monitoring (bytes in/out, CPU high water mark, number of requests handled per endpoint, etc)
* Auditing
 * PCI and Hippa requirements are modeled within the seed framework
 * Custom auditing appender
* Administrative console
 * Always exposes an administrative console on port 8077, regardless of deployment/container configuration.

### Pre-requisites
* Gradle >= 1.6
* JDK >= 6

### To run

* From the project root, "gradle jettyRun"
 * Checkout the admin console at http://localhost:8077
 * Checkout the example REST services at http://localhost:8989/hello-world/v1/
  * /hello - example of static service (no parameters)
  * /hello/to/world - example of dynamic query parameter
 * Checkout the dynamic services landing page
  * localhost:8989/dyn

### To deploy
* From the project root, "gradle war"
* Then copy (deploy) to your container of choice

### Developing a concrete service from the seed

There are currently four modules within the seed:

* karyon-admin-web
* karyon-admin
* service-framework
* service-implementation

The first two, for admin, are only coupled to the project as source as an artifact of the karyon project, and have not been moved out because they will likely be modified by this project within the near to mid term.

The service-framework module, like the admin modules, contain the homogenous functionality like auditing, servlet filter exception processing, system/generic metrics, and dynamic services.  Likely, for a given implementation, there will be no need to modify this module.

The service-implementation module is the module everyone will be concerned with.  Out of the box, it defines a hello-world project with two endpoints (one static and one that takes a template parameter), and a simple health check servlet.  This module is meant to be refactored into a useful service implementation.

### Example

Let's say you wanted to develop a service called "math" that multiplies two template parameters and returns the result.

#### First step, barebones implementation
* Get/fork the project, (ie) git clone github:robertjchristian/service-nucleus
 * As a sanity check, perform the steps in "to run" outlined above`
* nano service-implementation/src/main/java/com/liaison/service/HelloworldResource
 * Copy/paste the helloTo service, including annotations
 * Change path from "to/{name}" to "multiply/{a}/{b}"
 * Change method name to multiply, and the single "name" path parameter interface to take a and b "int" parameters instead
 * Change the response.put call to return the value of a*b instead.

That's it!

#### Second step, productize

Realistically you will want to productize your service, which basically means fixing up the namespace from hello-world to something more specific to your particular service.  These steps outline that process.  Note that scaffolding is on the near-term roadmap, and will make most or all of these steps obsolete:

* Edit ./service-implementation/build.gradle
 * Change project.war.basename from "hello-world" to "math"
 * Change System.setProperty("archaius.deployment.applicationId", "hello-world") to System.setProperty("archaius.deployment.applicationId", "math")
* Rename ./service-implementation/src/main/resources/hello-world* to use prefix "math" instead.
* Refactor your package namespace as desired
 * Make sure to update math.properties to reflect any high-level namespace change, ie com.netflix.karyon.server.base.packages=com.liaison
to com.netflix.karyon.server.base.packages=com.acme

##### Building LESS
In addition to the prerequisites outlined above, you'll need npm, less, and uglify-js to build Twtter Bootstrap.

### Configurable Data Source

If your service will be utilizing an Oracle database for persistence and you'd like to use a container-managed connection pool, follow the directions below.

CommonsLib (included by Service-Nucleus) has a Custom DataSource Factory that can be used to configure your Data Source Pool at Tomcat Container or Tomcat WEB App Context Level. Through the container declaration, a JNDI name can be automatically bound and available for the Service to use.  Only minimal configuration is actually in the container configuration, most of the DataSource configuration comes from the Liaison Service configuration (accessed via the LiaisonConfigurationFactory and DecryptableConfiguration classes).

These directions assume you are going to deploy inside Tomcat 7.

#### Directions

1. Put ojdbc*.jar and ucp*.jar in {tomcat-home}/lib folder
2. In your FOO.WAR app, add a context.xml file (see below).
   This configuration file will set the JNDI name that your connection pool will have.
3. If you are using JPA, you will need a persistence.xml file (see below).
   Important to configure a JNDI name matching what is in context.xml.
4. Configure your application as appropriate for using the LiaisonConfigurationFactory/DecryptableConfiguration classes available in the Commons lib.
   The following setting values must be provided:
   a. com.liaison.DB_URL (String)
   b. com.liaison.DB_USER (String)
   c. com.liaison.DB_PASSWORD (String; Note: this property should be encrypted)
   d. com.liaison.DB_CONNECTIONFACTORYCLASSNAME (String)
   e. com.liaison.DB_MINPOOLSIZE (Integer)
   f. com.liaison.DB_MAXPOOLSIZE (Integer)
   There are optional settings to control more of the Connections behavior, see [this document](DataSourceOptionalProperties).

#### context.xml
<pre>
<Context ... >
    ...
    <Resource name="jdbc/UCPPool-YOUR_SERVICE_SPECIFIC_NAME_HERE"
              type="oracle.ucp.jdbc.PoolDataSource"
              auth="Container"
              factory="com.liaison.commons.util.datasource.LiaisonConfigurableDataSourceFactory"
              />
</Context>
</pre>

#### persistence.xml
<pre>
<persistence ... >
    <persistence-unit ... >
       ...
       <properties>
           <property name="javax.persistence.transactionType" value="JTA"/>
           <property name="javax.persistence.jtaDataSource" value="java:comp/env/jdbc/UCPPool-YOUR_SERVICE_SPECIFIC_NAME_HERE"/>
       </properties>
    </persistence-unit>
</persistence>
</pre>


### Keeping up with changes

<h3>Keeping up with changes to Karyon</h3>

<pre>
git remote add --track master karyon git@github.com:Netflix/karyon.git
git fetch karyon
git merge karyon/master
</pre>

<h3>Keeping up with changes</h3>

This project is going to be in flux for the foreseeable future (see roadmap).

Adding the service-nucleus as a remote tracking branch is a low cost and easy way to stay current. It's recommended to do something like:

<pre>
git remote add --track master service-nucleus git@gitlab.com:g2/liaison-service-nucleus.git
git fetch liaison-service-nucleus
git merge liaison-service-nucleus/master
</pre>

<i>Note that similarly, when developing on this project, a remote tracking branch should be setup against NetFlix/karyon.</i>


<h3>12/13/2013 - Recent Changes to Service Nucleus</h3>
1. Merge with HEAD of karyon master:  https://github.com/Netflix/karyon 
2. AsyncServlet enabled and wired into web.xml 
3. Upgrade all gradle 1.5 references to 1.6 
4. Remove dependencies on junit in favor of testing
5. Delete fs2 source (grab snapshot 0.0.2 jar from nexus and refer to it locally instead i.e. in service-implementation/lib
6. Deprecate initialization servlet  (should be using HelloWorldComponent.initialize() instead ) 
7. Add Servlet 3.0
8. Added swqgger annotations and swagger ui


### Swagger REST Documentation 
Located at http://localhost:8989/hello-world/swagger-ui/index.html

#### Swagger-UI Config:
For release or war deployment edit src/main/webapps/ui/swagger-ui/index.html to point to public api-docs url 

For more info read swagger-ui docs at service-implementation/src/main/webapp/ui/swagger-ui/README.md

<h3>Roadmap</h3>
* Add thread sandboxing (mainly for dynamic services)




