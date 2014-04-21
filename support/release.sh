#!/usr/bin/env bash

set -e

function usage {
  echo "Usage: $0 NEW_VERSION NEXT_VERSION"
  echo ""
  echo "NEW_VERSION should not end in -SNAPSHOT"
  echo "NEXT_VERSION should end in -SNAPSHOT"
  echo ""
  echo "Example: $0 0.1.0 0.1.1-SNAPSHOT"
}

function set_versions {
  local version=$1
  mvn versions:set -DnewVersion=$version
  pushd examples/echo
  mvn versions:set -DnewVersion=$version
  popd
  find . -name "pom.xml.versionsBackup" -print0 | xargs -0 rm
}

new_version=$1;
next_version=$2;

if [[ -z $new_version || -z $next_version ]]; then
  usage
  exit 1
fi

if [[ ! $next_version = *-SNAPSHOT ]]; then
  echo "Error: NEXT_VERSION must end in -SNAPSHOT"
  usage
  exit 1
fi

echo "Checking for local git changes..."
if git status --porcelain | grep -E "^\s?M"; then
  echo "Local git changes detected - aborting release."
  exit 1
fi

echo "Checking for previous failed releases..."
if git log -n 1 | grep -B 3 "prepare release"; then
  echo "Previous failed release found - clean up and try again."
  exit 1
fi
if git log -n 1 | grep -B 3 "prepare for next development iteration"; then
  echo "Previous failed release found - clean up and try again."
  exit 1
fi

echo "Checking for preexisting git tag..."
if git tag -l | grep "$new_version"; then
  echo "Release already tagged in git - clean up and try again."
  exit 1
fi

mvn clean
mvn install -Pintegration-tests

set_versions $new_version
git commit -am "prepare release $new_version"
git tag "v$new_version"

mvn clean deploy -Psonatype-oss-release

set_versions $next_version
git commit -am "prepare for next development iteration"
