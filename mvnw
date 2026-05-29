#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper startup script for UNIX
# ----------------------------------------------------------------------------

set -e

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd -P)"
export MAVEN_PROJECTBASEDIR

MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-3.9.6-bin/3311e1d4/apache-maven-3.9.6"
if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Apache Maven 3.9.6..."
  mkdir -p "${HOME}/.m2/wrapper/dists/apache-maven-3.9.6-bin/3311e1d4"
  cd "${HOME}/.m2/wrapper/dists/apache-maven-3.9.6-bin/3311e1d4"
  if command -v curl > /dev/null; then
    curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" -o maven.zip
  elif command -v wget > /dev/null; then
    wget -q "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" -O maven.zip
  fi
  unzip -q maven.zip
  rm maven.zip
  cd "$MAVEN_PROJECTBASEDIR"
fi

export MAVEN_HOME
export PATH="${MAVEN_HOME}/bin:${PATH}"

exec mvn -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" "$@"
