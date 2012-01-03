#!/bin/bash

export JAVA_HOME=/usr/java/latest
export MVN=/opt/apache-maven-2.2.1/bin/mvn

run() {
	for I in $@
	do
		if test -f "$I"
		then
			if test "${I:0:1}" = "/"
			then
				${MVN} exec:exec -Dconfigfile=$I
			else
				${MVN} exec:exec -Dconfigfile=${CURRENT_DIR}/$I
			fi
		elif test -d "$I"
		then
			run $I/*
		fi
	done
}

export CURRENT_DIR=$PWD
cd $(dirname $0)
run $@
