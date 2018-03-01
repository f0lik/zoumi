#!/bin/sh
nohup java -Ddbpasswd=<databasePassword> -Dspring.profiles.active=prod -DdbUrl=<sourceDbUrl> -jar *.jar --server.port=9090 > nohup2.out&
