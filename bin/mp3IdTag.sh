#!/bin/sh

rootDir="$(dirname "$0")"
cd "$rootDir"
/usr/bin/java -Xmx4096m -jar "mp3IdTag.jar"