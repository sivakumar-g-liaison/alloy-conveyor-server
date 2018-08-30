#!/usr/bin/env bash
. service-implementation/src/main/resources/g2mailboxservice-tre-dev-int.sh
./gradlew -PcontainerJVMArgs="-Duser.timezone=UTC -XX:-UseSplitVerifier" cargoRunLocal licenseMain -PdeployEnvironment=tre-dev-int --stacktrace --debug