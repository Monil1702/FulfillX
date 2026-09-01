#!/bin/sh
set -eu
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -classpath "$project_dir/.mvn/wrapper/maven-wrapper.jar" \
  -Dmaven.multiModuleProjectDirectory="$project_dir" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"

