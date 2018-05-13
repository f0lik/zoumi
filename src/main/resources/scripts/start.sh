#!/bin/sh
nohup java -Ddbuser=<databaseUser> -Ddbpasswd=<databasePassword> -Dspring.profiles.active=prod -DdbUrl=<sourceDbUrl> -jar *.jar --server.port=9090 > nohup2.out&
