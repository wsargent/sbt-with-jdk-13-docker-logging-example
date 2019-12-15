# Example SBT build with Java 13

This is how you can run projects on JDK 13 without messing up the rest of your system and the environment, using sbt-native-packager and Docker to deploy a custom JVM.

This is also a demonstration of how to get an application running in different logging environments, since that's a basic need.  This project uses [terse-logback](https://github.com/tersesystems/terse-logback) to do the heavy lifting, but most of it's under the hood.

## Setup

The first thing you should do is install SDKMAN and jenv, which will let you be specific about what JDK version you want.

### Install SDKMAN and jenv

[SDKMAN](https://sdkman.io/usage) is the best way to pick out different versions of Java and have them all co-exist.

[jenv](http://www.jenv.be/) is the best way to set different JDKs for different projects.

You can install multiple JDKs and have them available.  Here, we'll use the OpenJDK 13.0.1 image. 

```
$ sdk install java 13.0.1.hs-adpt
```

After that, you can see the JDK available in `$HOME/.sdkman/candidates/java/13.0.1-zulu`.

Now that you've got the JDK available, let's tell jenv about it:

```
$ jenv add $HOME/jenv add $HOME/.sdkman/candidates/java/13.0.1.hs-adpt
```

And then we'll set it as the default for this project.

```
$ jenv local 13.0
```

This will create a `.java-version` file in your directory that picks up and points you to the JDK.

```
$ java --version 
openjdk 13.0.1 2019-10-15
OpenJDK Runtime Environment AdoptOpenJDK (build 13.0.1+9)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 13.0.1+9, mixed mode, sharing)
```

Now you're running JDK 13 for your project.

## Running in different environments

There are four different environments, which affect how you want the application to log.

In a developer environment, you'll want line oriented logs written out to the console, and logs persisted local to the environment.

When running tests, you'll want to keep logging out of the console altogether.

In an OS environment, the server is handed a package which integrates into the OS.  You'll have either a `.deb` or a `.zip` package available, and the expectation is that logs will be written out to the file system.

In a cloud environment, you want JSON logging written out to the console and will not have a writable filesystem.

### Running in Development Environment

Run the program inside of SBT by typing `run` inside of SBT:

```
$ sbt
[master] root: run
[info] running example.Hello 
FeAZEWTIOiY6O0Qbm7EAAA 18:40:18.775 [INFO ] example.Hello$ -  hello
```

### Packaging for Zip File 

Packaging for a zip file is handled by the `JavaAppPackaging` plugin that comes with SBT native packager.

```
$ sbt universal:packageZipTarball
```

This will create a `target/universal/sbt-with-jdk13-example-0.1.0-SNAPSHOT.tgz` file, which we can unzip.

We can also try out the application in staging:

```
$ sbt stage
```

This will create a `target/universal/stage` directory.  From there, we can change to that directory and run directly:

```
$ cd target/universal/stage
$ jenv local 13.0
$ ./bin/sbt-with-jdk13-example  
FeAZEWUI15Q6O0Qbm7EAAA 19:56:19.781 [INFO ] example.Hello$ -  hello
```

Note that there will be a logs directory in the stage folder now, that contains the JSON file or text file, depending on how you set `LOGBACK_ENVIRONMENT`:

```
$ ls -l logs
total 4
-rw-r--r-- 1 wsargent wsargent 334 Dec 14 19:56 application.json
-rw-r--r-- 1 wsargent wsargent   0 Dec 14 19:56 application.log
```

### Packaging for Docker

Packaging for a Docker file is handled by the `DockerPlugin` plugin that comes with SBT native packager.

To publish a docker image to the local repository, run:

```
$ sbt clean stage docker:publishLocal
```

And then from there you can run the docker image locally on a machine, where it will run with production settings by default:

```
$ docker run --rm sbt-with-jdk13-example:latest
FeAZEWTy9QidHaINzdiAAA 3:33:59.234 [INFO ] example.Hello$ -  hello
```

However, you'll want to push this to the cloud and get JSON logging back. If you run the docker image and pass in `LOGBACK_ENVIRONMENT=container` then you'll get JSON back:

```
$ docker run --env "LOGBACK_ENVIRONMENT=container" --rm sbt-with-jdk13-example:latest
{"@timestamp":"2019-12-15T03:27:35.184Z","@version":"1","message":"hello","logger_name":"example.Hello$","thread_name":"main","level":"INFO","level_value":20000}
```

If you want to look at and modify the `Dockerfile` for your own purposes, you can use `sbt docker:stage`.  This produces `target/docker/stage` directory containing the `Dockerfile`.
