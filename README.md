JasperReports Server Shell
==========================
[![Build Status](https://travis-ci.org/Krasnyanskiy/jrsh-project.svg?branch=master)](https://travis-ci.org/Krasnyanskiy/jrsh-project) [![Coverage Status](https://img.shields.io/coveralls/Krasnyanskiy/jrs-command-line-tool.svg)](https://coveralls.io/r/Krasnyanskiy/jrs-command-line-tool?branch=master)

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Krasnyanskiy/jrs-command-line-tool?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)

# Overview

JRSH is a simple and comprehensive CLI tool for JasperReports Server. It's designed for fast and easy interaction with JasperServer across various operating systems.

## Usage examples

The following code snippets show how to run

### Import command

```bash
jrsh --server "http://54.163.3.100/jasperserver-pro" --username "superuser" --password "superuser" \
import "/folder/import.zip"
```

### Export command

```bash
jrsh --server "http://54.163.3.100/jasperserver-pro" --username "superuser" --password "superuser" \
export "/public/Samples/Reports/06g.ProfitDetailReport" without-users-and-roles
```

### Help command

```bash
jrsh help
```

## How to build

To build [JRSH](https://github.com/Krasnyanskiy/jrs-command-line-tool), just pen a terminal window and invoke
```java
mvn clean install
```
After that you can simply find and run executable scripts from your target folder.
