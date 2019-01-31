# ODK Aggregate CLI
![Platform](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/aggregate-cli.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/aggregate-cli)
[![codecov.io](https://codecov.io/github/opendatakit/aggregate-cli/branch/master/graph/badge.svg)](https://codecov.io/github/opendatakit/aggregate-cli)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)

ODK Aggregate CLI is a command line application to help with the installation and update of [ODK Aggregate](https://github.com/opendatakit/aggregate) server, starting with ODK Aggregate v2.0.   

ODK Aggregate CLI is bundled with all [ODK Aggregate Cloud-Config](https://github.com/opendatakit/aggregate/tree/master/cloud-config) stacks.

ODK Aggregate CLI is part of Open Data Kit (ODK), a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the Open Data Kit project and its history [here](https://opendatakit.org/about/) and read about example ODK deployments [here](https://opendatakit.org/about/deployments/).

* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK forum: [https://forum.opendatakit.org](https://forum.opendatakit.org)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 
* ODK developer Slack archive: [http://opendatakit.slackarchive.io](http://opendatakit.slackarchive.io) 
* ODK developer wiki: [https://github.com/opendatakit/opendatakit/wiki](https://github.com/opendatakit/opendatakit/wiki)

## Setting up your development environment

These instructions are for [IntelliJ IDEA Community edition](https://www.jetbrains.com/idea/), which is the (free) Java IDE we use for all the ODK toolsuite, but you don't really need any specific IDE to work with this codebase. Any Java IDE will support any of the steps we will be describing.

### Import 

- On the welcome screen, click `Import Project`, navigate to your aggregate folder, and select the `build.gradle` file. 

  Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog. 

- Make sure you set Java 8 as the project's selected SDK

## Contributing code

Any and all contributions to the project are welcome.

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

