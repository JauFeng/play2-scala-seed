# Getting started with Play (Scala)

This project demonstrate how to create a simple CRUD application with Play.

## Framework Version

- play version:   2.4.2
- scala version:  2.11.7
- sbt version:    0.13.9
- specs2 version: 3.6.3

- Slick version: 3.0.1
- bootstrap version: 3.3.5
- play-mailer version: 3.0.1

- - -

## Configuration

- Most Play JARs include a `reference.conf` with default settings
- `application.conf` will override `reference.conf`.

- - -

### Application Secret

Use for:
- Signing session cookies and CSRF tokens.
- Built in encryption utilities.

#### Configuring application secret

1. Start Script:    ```/path/to/yourapp/bin/yourapp -Dplay.crypto.secret="QCY?tAnfk?aZ?iwrNwnxIlR6CTf:G3gf:90Latabg@5241AB`R5W:1uDFN];Ik@n"```
2. Environment variables:   ```play.crypto.secret=${?APPLICATION_SECRET}```
3. Production configuration file:   ```include "application"    play.crypto.secret="QCY?tAnfk?aZ?iwrNwnxIlR6CTf:G3gf:90Latabg@5241AB`R5W:1uDFN];Ik@n"```

#### Generating an application secret

Run `play-generate-secret` in the Play console.

#### Updating the application secret in application

Run `play-update-secret` in the Play console

### Database Access library

You can not use ==~~JDBC together with Play-Slick~~==([throw exception](https://www.playframework.com/documentation/2.4.x/PlaySlickFAQ)), but Play-Slick can be working with another Play module for database access.


- - -

#### JDBC

- Add JDBC in `bulid.sbt` :
```
	libraryDependencies += jdbc
```

- Database Configuration in `appliction`:

	1. H2:
```language
	db.default.driver=org.h2.Driver
	db.default.url="jdbc:h2:mem:play"
```

#### Slick

- Add a library dependency in `build.sbt`:
```
  	"com.typesafe.play" %% "play-slick" % "1.0.0",
  	"com.typesafe.play" %% "play-slick-evolutions" % "1.0.0"
```

- Database Configuration in `appliction`:
	1. H2:
```shell
	# Default database configuration
	slick.dbs.default.driver="slick.driver.H2Driver$"
	slick.dbs.default.db.driver="org.h2.Driver"
	slick.dbs.default.db.url="jdbc:h2:mem:play"

	# Orders database
	slick.dbs.orders.driver="slick.driver.H2Driver$"
	slick.dbs.orders.db.driver="org.h2.Driver"
	slick.dbs.orders.db.url="jdbc:h2:mem:play"

	# Customers database
	slick.dbs.customers.driver="slick.driver.H2Driver$"
	slick.dbs.customers.db.driver="org.h2.Driver"
	slick.dbs.customers.db.url="jdbc:h2:mem:play"
```

See more: [Play Slick](https://www.playframework.com/documentation/2.4.x/PlaySlick)



### Connection Pool

- JDBC
	Setting in `applicationi.conf`: `play.db.pool=bonecp`==(Only for JDBC)==
	- Default:  [HikariCP](https://brettwooldridge.github.io/HikariCP/)
	- Optional: [BoneCP](http://jolbox.com/)

  See details: `play-jdbc_2.11-2.4.1.jar/reference.conf` or Play's [`reference.conf`](https://www.playframework.com/documentation/2.4.x/resources/confs/play-jdbc/reference.conf)

- Slick
  Play Slick currently only allow using [HikariCP](http://brettwooldridge.github.io/HikariCP/).(See more: [PlaySlickFAQ](https://www.playframework.com/documentation/2.4.x/PlaySlickFAQ))

- - -

### Play Database evolution

TODO

- - -

### Play Default Thread Pool

- Default:  i.e.`akka-actor_2.11-2.3.11.jar!\reference.conf`
- Custom:   Configuring `application.conf` override `reference.conf`.
```shell
        akka {
          fork-join-executor {
            # The parallelism factor is used to determine thread pool size using the
            # following formula: ceil(available processors * factor). Resulting size
            # is then bounded by the parallelism-min and parallelism-max values.
            parallelism-factor = 3.0

            # Min number of threads to cap factor-based parallelism number to
            parallelism-min = 8

            # Max number of threads to cap factor-based parallelism number to
            parallelism-max = 64
          }
        }
```
- Using other thread pools(i.e. Java executors, Scala fork/join): See details: [here](https://www.playframework.com/documentation/2.4.x/ThreadPools##Using%20other%20thread%20pools)

- - -

### Logging([Logback](http://logback.qos.ch/))

- Default Configuration File: `conf\logback.xml`

- Using an external configuration file:
    1. Load config file from classpath: `$ start -Dlogger.resource = prod-logger.xml`
    2. Load config file from file system:   `$start -Dlogger.file = /opt/prod/logger.xml`

- Akka logging:
	- Default: Ignore Play's logging configuration and print log messages to STDOUT.
		Configure it in `application.conf`:	```akka {	loglevel="INFO"	}```
	- Direct Akka to use Play's logging engine:

		1. Config in `application.conf`:
```shell
	akka {
  		loggers = ["akka.event.slf4j.Slf4jLogger"]
  		loglevel="DEBUG"
	}
```
		2. Refine your Akka logging setting in Logback config:
```shell
    <!-- Set logging for all Akka library classes to INFO -->
    <logger name="akka" level="INFO" />
    <!-- Set a specific actor to DEBUG -->
    <logger name="actors.MyActor" level="DEBUG" />
```

	See more info about Akka logging: [Akka documentation](http://doc.akka.io/docs/akka/current/scala/logging.html)

- - -

### ~~GlobalSetting~~

#### `HttpRequestHandler`
- Configuring filters in `application.conf`:
```shell
	play.http.requestHandler = "com.example.RequestHandler"
    play.http.requestHandler = "play.api.http.DefaultHttpRequestHandler"
```
- Performance note:
  1. If you didn't configure Play's HttpRequestHandler, that means use the `GlobalSetting` method.
  2. `GlobalSetting` have a performance impact - your app has to do many lookups out of Guice to handle a single request.
  3. Instead using `DefaultHttpRequestHandler` avoid the issue.

- - -

### Play filters

- Add dependency in `build.sbt`:
```shell
	libraryDependencies += filters
```
- Configuring filters in `application.conf`:
```shell
	play.http.filters = "filters.MyFilters"
```
see details:  `filters-helpers_2.11-2.4.1.jar!\reference.conf`

- Overview the global filter:
```scala
    package filters

    import javax.inject.Inject

    import play.api.http.HttpFilters
    import play.api.mvc.EssentialFilter
    import play.filters.cors.CORSFilter
    import play.filters.csrf.CSRFFilter
    import play.filters.gzip.GzipFilter
    import play.filters.headers.SecurityHeadersFilter

    class MyFilters @Inject()(gzipFilter: GzipFilter,
                              securityHeadersFilter: SecurityHeadersFilter,
                              cORSFilter: CORSFilter,
                              cSRFFilter: CSRFFilter) extends HttpFilters {
      override def filters: Seq[EssentialFilter] = Seq(gzipFilter, securityHeadersFilter, cORSFilter, cSRFFilter)
    }
```

#### Gzip Filter

- Creating gzip `Filters` class:
```scala
    import javax.inject.Inject

    import play.api.http.HttpFilters
    import play.filters.gzip.GzipFilter

    class Filters @Inject() (gzipFilter: GzipFilter) extends HttpFilters {
      def filters = Seq(gzipFilter)
    }
```

- Controlling which responses are gzipped:
```scala
    new GzipFilter(shouldGzip = (request, response) =>
      response.headers.get("Content-Type").exists(_.startsWith("text/html")))
```

#### Security headers

- Creating Security Headers `Filters` class:
```scala
    import javax.inject.Inject

    import play.api.http.HttpFilters
    import play.filters.headers.SecurityHeadersFilter

    class Filters @Inject() (securityHeadersFilter: SecurityHeadersFilter) extends HttpFilters {
      def filters = Seq(securityHeadersFilter)
    }
```

- Configuring the security headers:
	- `play.filters.headers.frameOptions` - [X-Frame-Options](https://developer.mozilla.org/en-US/docs/HTTP/X-Frame-Options): "DENY" by default
	- `play.filters.headers.xssProtection` - [X-XSS-Protection](http://blogs.msdn.com/b/ie/archive/2008/07/02/ie8-security-part-iv-the-xss-filter.aspx): "1; mode=block" by default
	- `play.filters.headers.contentTypeOptions` - [X-Content-Type-Optiond](http://blogs.msdn.com/b/ie/archive/2008/09/02/ie8-security-part-vi-beta-2-update.aspx): "nosniff" by default
	- `play.filters.headers.permittedCrossDomainPolicies` - [X-Permitted-Cross-Domain-Policies](https://www.adobe.com/devnet/articles/crossdomain_policy_file_spec.html): "master-only" by default
	- `play.filters.headers.contentSecurityPolicy` - [Content-Security-Policy](http://www.html5rocks.com/en/tutorials/security/content-security-policy/): "default-src" by default

- Custome the security headers in `application.conf`:
  ```shell
    # The X-Frame-Options header. If null, the header is not set.
    play.filters.headers.frameOptions = "SAMEORIGIN"

    # The X-XSS-Protection header. If null, the header is not set.
    play.filters.headers.xssProtection = "1; mode=block"

    # The X-Content-Type-Options header. If null, the header is not set.
    play.filters.headers.contentTypeOptions = "nosniff"

    # The X-Permitted-Cross-Domain-Policies header. If null, the header is not set.
    play.filters.headers.permittedCrossDomainPolicies = "master-only"

    # The Content-Security-Policy header. If null, the header is not set.
    play.filters.headers.contentSecurityPolicy = "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'"
  ```

#### Cross-Origin Resource Sharing (See more about CORS: [here](http://www.w3.org/TR/cors/))

- Creating CORS `Filter` class:
```scala
    import javax.inject.Inject

    import play.api.http.HttpFilters
    import play.filters.cors.CORSFilter

    class Filters @Inject() (corsFilter: CORSFilter) extends HttpFilters {
      def filters = Seq(corsFilter)
    }
```

- Configuring the CORS filter:

	- `play.filters.cors.pathPrefixes` - filter paths by a whitelist of path prefixes
    - `play.filters.cors.allowedOrigins` - allow only requests with origins from a whitelist (by default all origins are allowed)
    - `play.filters.cors.allowedHttpMethods` - allow only HTTP methods from a whitelist for preflight requests (by default all methods are allowed)
    - `play.filters.cors.allowedHttpHeaders` - allow only HTTP headers from a whitelist for preflight requests (by default all headers are allowed)
    - `play.filters.cors.exposedHeaders` - set custom HTTP headers to be exposed in the response (by default no headers are exposed)
    - `play.filters.cors.supportsCredentials` - disable/enable support for credentials (by default credentials support is enabled)
    - `play.filters.cors.preflightMaxAge` - set how long the results of a preflight request can be cached in a preflight result cache (by default 1 hour)

#### Cross-Site Request Forgery(*==[Sometimes is not appropriate for global filter](https://www.playframework.com/documentation/2.4.x/ScalaCsrf)==*)
See more about CSRF is: [here](https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29)

- ~~Creating CSRF `Filter` class(Global filter)~~:
```scala
    import play.api.http.HttpFilters
    import play.filters.csrf.CSRFFilter
    import javax.inject.Inject

    class Filters @Inject() (csrfFilter: CSRFFilter) extends HttpFilters {
      def filters = Seq(csrfFilter)
    }
```

- Applying CSRF on a per action(==Recommendation==):
```scala
    // It generates a CSRF token if not already present on the incoming request.
    def save = CSRFAddToken {
      Action { req =>
        // handle body
        Ok
      }
    }

    def form = CSRFCheck {
      Action { implicit req =>
        Ok(views.html.itemsForm())
      }
    }
```

- Using in template:
```scala
	@(person: Form[CreatePersonForm])(implicit messages: Messages, requestHeader: RequestHeader)

	@import helper._

    @form(CSRF(routes.PersonController.addPerson())) { ...

    // or CSRF.formField
```

- CSRF configuration options:

    - `play.filters.csrf.token.name` - The name of the token to use both in the session and in the request body/query string. Defaults to csrfToken.
    - `play.filters.csrf.cookie.name` - If configured, Play will store the CSRF token in a cookie with the given name, instead of in the session.
    - `play.filters.csrf.cookie.secure` - If play.filters.csrf.cookie.name is set, whether the CSRF cookie should have the secure flag set. Defaults to the same value as play.http.session.secure.
    - `play.filters.csrf.body.bufferSize` - In order to read tokens out of the body, Play must first buffer the body and potentially parse it. This sets the maximum buffer size that will be used to buffer the body. Defaults to 100k.
    - `play.filters.csrf.token.sign` - Whether Play should use signed CSRF tokens. Signed CSRF tokens ensure that the token value is randomised per request, thus defeating BREACH style attacks.

- - -

### Play cache([EHCache](http://ehcache.org/))

- Add `cache` in `build.sbt`:
  ```shell
    libraryDependencies ++= Seq(
      cache,
      ...
    )
  ```
- Configuration in `application.conf`:
  ```shell
	play.cache.bindCaches = ["db-cache", "user-cache", "session-cache"]
```

- Example:
  ```scala
  	class Application @Inject()(@NamedCache("session-cache") sessionCache: CacheApi) extends Controller {

		cache.set("item.key", connectedUser, 5.minutes)
    	cache.remove("item.key")

    	val maybeUser: Option[User] = cache.get[User]("item.key", 1.minutes)

    	val user: User = cache.getOrElse[User]("item.key")(User.findById(connectedUser))
        
        // Cache the index.html page.
        def index = Action { implicit request =>
    		Ok(htmlCache.getOrElse("index-html")(views.html.index(title = "Index", loginForm = loginForm)))
  	    }
  ```

- - -

### Play WS
- Add `ws` in `build.sbt`:
  ```shell
    libraryDependencies ++= Seq(
      ws,
      ...
    )
```

- Configuration in `application.conf`:
  ```shell
    play.ws.followRedirects = true
    play.ws.useProxyProperties = true
    play.ws.useragent = null
    play.ws.compressionEnabled = false

    play.ws.timeout.connection = 2 minutes
    play.ws.timeout.idle = 2 minutes
    play.ws.timeout.request = 2 minutes
```

- - -

### Enable HTTPS(SSL)([JSSE](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html))

- Define system property named `https.port`:
```shell
	 -Dhttps.port = 9000 -Dhttps.keyStore=/path/to/jks -Dhttps.keyStorePassword=password
```
- TODO

- - -

### I18n

- Configuration in `application.conf`:
  ```shell
	play.i18n.langs = ["en", "zh"]
```

- Create `messages` file for each `langs`, i.e. `messages.en-US` & `messages.zh-CN`, and configuration:
  ```shell
	auth.unknown = Unknown author: {0}
	password.unknown = Unknown password: {0}
```

- Using `MessageApi`:
  ```scala
	val msgString = Messages("items.found", items.size)
```
- `Accept-Language` in the `HTTP headers`:
  `Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3`
  so you can  using following choice lang:
  ```scala
  request.headers.replace(("Accept-Language", "en-US"))
```

- - -

## 3rd party modules

### Play mailer

- Add a library dependency on play-mailer:
  ```shell
	"com.typesafe.play" %% "play-mailer" % "3.0.1"
```

- Configuration in `application.conf`:
  ```shell
  	play.mailer {
          host (mandatory)
          port (defaults to 25)
          ssl (defaults to no)
          tls (defaults to no)
          user (optional)
          password (optional)
          debug (defaults to no, to take effect you also need to set the log level to "DEBUG" for the application logger)
          timeout (defaults to 60s)
          connectiontimeout (defaults to 60s)
          mock (defaults to no, will only log all the email properties instead of sending an email)
	}
  ```