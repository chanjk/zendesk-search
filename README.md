# Zendesk Search

**Note: If this is your first time running this, be aware that the initial download of sbt and dependencies can take a while. Please set aside at least 10 minutes for everything to be downloaded.**

## Requirements

### Docker

#### macOS

**With [Homebrew](https://brew.sh)**

```sh
$ brew install --cask docker
```

### Java Development Kit (JDK) 11 (if you don't want to use Docker)

#### macOS

**Note: If you already have a different version of the JDK installed, sbt might detect that version instead of the intended one. In that case, you can either**:
- **Set the correct JDK version after installing JDK 11**:
    ```sh
    $ unset JAVA_HOME
    $ export JAVA_HOME=`/usr/libexec/java_home -v 11`
    ```

  **or**

- **Run the app in Docker instead.**

**With [Homebrew](https://brew.sh)**

```sh
$ brew tap AdoptOpenJDK/openjdk
$ brew install --cask adoptopenjdk11

// To check that the right version is installed
$ javac -version // should output `javac 11.x.y.z`
```

## Running the app

### With Docker

```sh
$ auto/run-docker
```

### Without Docker

```sh
$ auto/run
```

## Usage

WIP

## Running the tests

### With Docker

```sh
$ auto/test-docker
```

### Without Docker

```sh
$ auto/test
```
