# https://just.systems

build:
    ./mvnw verify

assemble:
    JRELEASER_PROJECT_VERSION=1.0.0-SNAPSHOT ./jbang jreleaser@jreleaser assemble

dry-run-release:
    JRELEASER_PROJECT_VERSION=1.0.0-SNAPSHOT ./jbang jreleaser@jreleaser full-release --dry-run -scp

