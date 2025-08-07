# https://just.systems

build:
    ./mvnw verify

assemble:
    JRELEASER_PROJECT_VERSION=`mvn help:evaluate -Dexpression=project.version -q -DforceStdout` ./jbang jreleaser@jreleaser assemble

dry-run-release:
    JRELEASER_PROJECT_VERSION=`mvn help:evaluate -Dexpression=project.version -q -DforceStdout` ./jbang jreleaser@jreleaser full-release --dry-run -scp

