#!/usr/bin/env bash
set -euo pipefail

version="$1"

perl -pi -e "s/^VERSION_NAME=.*/VERSION_NAME=${version}/" gradle.properties

perl -pi -e "s/(testcontainers-moto:)[0-9][^\"]*/\${1}${version}/g" README.md
perl -0777 -pi -e \
  "s/(<artifactId>(?:spring-boot-)?testcontainers-moto<\/artifactId>\s*<version>)[0-9][^<]*(<\/version>)/\${1}${version}\${2}/g" \
  README.md
