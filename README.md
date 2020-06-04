# ODK Aggregate CLI
![Platform](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/getodk/aggregate-cli.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/getodk/aggregate-cli)
[![codecov.io](https://codecov.io/github/getodk/aggregate-cli/branch/master/graph/badge.svg)](https://codecov.io/github/getodk/aggregate-cli)
[![Slack](https://img.shields.io/badge/chat-on%20slack-brightgreen)](https://slack.getodk.org)

ODK Aggregate CLI is a command line application to help with the installation and update of [ODK Aggregate](https://github.com/getodk/aggregate) server, starting with ODK Aggregate v2.0.   

ODK Aggregate CLI is bundled with all [ODK Aggregate Cloud-Config](https://github.com/getodk/aggregate/tree/master/cloud-config) stacks.

ODK Aggregate CLI is part of ODK, a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the ODK project and its history [here](https://getodk.org/about/) and read about example ODK deployments [here](https://getodk.org/about/deployments/).

* ODK website: [https://getodk.org](https://getodk.org)
* ODK forum: [https://forum.getodk.org](https://forum.getodk.org)
* ODK developer Slack chat: [https://slack.getodk.org](https://slack.getodk.org) 
* ODK developer wiki: [https://github.com/getodk/getodk/wiki](https://github.com/getodk/getodk/wiki)

## Usage

```
Launch an operation with: aggregate-cli <operation> <params>

Available operations:
  -h,--help                        Show help
  -i,--install                     Install ODK Aggregate
  -l,--list                        List available versions
  -u,--update                      Update ODK Aggregate
  -v,--version                     Show version

Params for -u operation:
  -c,--configuration <arg>         Path to the configuration file (JSON)
Optional params for -u operation:
  -f,--force                       Force update
  -ip,--include-pre-releases       Include pre-release versions
  -rv,--requested-version <arg>    Requested version (latest by default)
  -vv,--verbose                    Verbose mode. Shows all commands
  -y,--yes                         Always answer 'yes' to confirm prompts

Params for -l operation:
Optional params for -l operation:
  -ip,--include-pre-releases       Include pre-release versions

Params for -i operation:
  -c,--configuration <arg>         Path to the configuration file (JSON)
Optional params for -i operation:
  -cu,--custom-url <arg>           Custom URL to download the Aggregate WAR package
  -cv,--custom-version <arg>       Version that the custom Aggregate WAR URL corresponds to
  -f,--force                       Force update
  -ip,--include-pre-releases       Include pre-release versions
  -vv,--verbose                    Verbose mode. Shows all commands
  -y,--yes                         Always answer 'yes' to confirm prompts
```

The install and update operations require you to provide the path to a JSON configuration file that describes your environment. There's a template of this file at [src/main/resources/configuration.tpl.json](src/main/resources/configuration.tpl.json):

```json
{
  "home": "/root",
  "jdbc": {
    "host": "127.0.0.1",
    "port": 5432,
    "db": "aggregate",
    "schema": "aggregate",
    "user": "aggregate",
    "password": "aggregate"
  },
  "security": {
    "hostname": "aggregate.example.com",
    "forceHttpsLinks": true,
    "port": 80,
    "securePort": 443,
    "checkHostnames": true
  },
  "tomcat": {
    "uid": "tomcat8",
    "gid": "tomcat8",
    "webappsPath": "/var/lib/tomcat8/webapps"
  }
}
``` 

## Setting up your development environment

These instructions are for [IntelliJ IDEA Community edition](https://www.jetbrains.com/idea/), which is the (free) Java IDE we use for all the ODK toolsuite, but you don't really need any specific IDE to work with this codebase. Any Java IDE will support any of the steps we will be describing.

### Import 

- On the welcome screen, click `Import Project`, navigate to your aggregate folder, and select the `build.gradle` file. 

  Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog. 

- Make sure you set Java 8 as the project's selected SDK

### Logging

Copy src/main/resources/logback-release.xml to src/main/resources/logback.xml. This conf will be used when launching Aggregate CLI.

## Contributing code

Any and all contributions to the project are welcome.

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

