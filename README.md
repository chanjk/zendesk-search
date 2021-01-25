# Zendesk Search

**Note: If this is your first time running this, be aware that the initial download of sbt and dependencies can take a while. Please set aside at least 10 minutes for everything to be downloaded.**

## Requirements

### Docker (if you don't want to install JDK 11 on your machine)

#### macOS

**With [Homebrew](https://brew.sh)**

```sh
$ brew install --cask docker
```

#### Linux

https://docs.docker.com/engine/install/

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

#### Linux

https://adoptopenjdk.net/installation.html

## Running the app

Note that the application takes a fair bit longer to start if using Docker.

### With Docker

```sh
$ auto/run-docker
```

### Without Docker

```sh
$ auto/run
```

## Usage

When you the [run](#running-the-app) the application, you will be greeted with this screen:

```
Welcome to Zendesk Search
Type 'quit' to exit at any time.

Select search options:
* Press 1 to search Zendesk
* Press 2 to view a list of searchable fields
* Type 'quit' to exit
```

From here, you can either:
- Enter `1` to start searching,
- Enter `2` to view the list of searchable fields, or
- Type `quit` to exit.

Note that you can type `quit` anytime to exit the application.

### If you pressed `1` to start searching

```
Select 1) Users or 2) Tickets or 3) Organizations
```

Here, you can choose the type of entity that you want to search for by entering either `1`, `2` or `3`. Choosing any one of the search types will continue the search.

```
Enter search field
```

Once you have selected the search type, you will be prompted for the field that you want to search on – you must provide a non-blank string. Example: `_id`

```
Enter search value
```

After you have provided a search field, you can provide the value that you want to search for. Example: `1`

Once you have provided a search value, the application will search for all records that match your selected search type, search field, and search value. Example: search for all users that have an `_id` of `1`. The results will be printed to the screen.

Note that the search is _case-sensitive_.

You can also provide an empty search value (by just pressing <kbd>Enter</kbd> without any other input when prompted for a search value), and the application will return the records that don't have any value for the chosen search field. This can be used to search for users that don't belong to any organization, for example.

### If you pressed `2` to view the list of searchable fields

The following list will be displayed:

```
Search Users with:

_id
active
alias
created_at
email
external_id
last_login_at
locale
name
organization_id
phone
role
shared
signature
suspended
tags
timezone
url
verified
------------------------------------------
Search Tickets with:

_id
assignee_id
created_at
description
due_at
external_id
has_incidents
organization_id
priority
status
subject
submitter_id
tags
type
url
via
------------------------------------------
Search Organizations with:

_id
created_at
details
domain_names
external_id
name
shared_tickets
tags
url
```

## Running the tests

### With Docker

```sh
$ auto/test-docker
```

### Without Docker

```sh
$ auto/test
```

## Assumptions

WIP
