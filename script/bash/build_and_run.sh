#!/bin/bash
cd ../..
mvn clean install
java -jar target/jrsh-2.0.2-jar-with-dependencies.jar superuser%superuser@http://localhost:8080/jasperserver-pro
