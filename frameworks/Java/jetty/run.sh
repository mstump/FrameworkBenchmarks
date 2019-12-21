#!/bin/sh
set -xe

JAVA_OPTS=${JAVA_OPTS:- -XX:+UseNUMA -XX:+UseParallelGC}
java ${JAVA_OPTS} -jar app.jar
