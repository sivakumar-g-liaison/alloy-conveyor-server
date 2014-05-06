#!/bin/bash

if [[ "$1" != "" ]]; then
    RELEASE=$1
else
    echo 'A G2 project bundle release-name is expected as the first argument.'
    echo '( e.g.g2mailboxservice-1.0.0-SNAPSHOT_master_ )'
    exit 1
fi

if [[ "$2" != "" ]]; then
    DEPLOY_PROFILE=$2
else
    echo 'A buildbundle environment flag is expected as the 2nd argument. (e.g. dev)'
    exit 1
fi

if [[ "$3" != "" ]]; then
    DB_PREFIX=$3
else
    echo 'A database username prefix is expected as the 3rd argument. (e.g. SVR01)'
    exit 1
fi

if [[ "$BUNDLE_SERVER" == "" ]]; then
    BUNDLE_SERVER=lsvljnkin01d.liaison.dev
fi

DEPLOY_ROOT=/opt/liaison/releases
DEPLOY_BUNDLE=deployable_${RELEASE}.tgz
BUNDLE_URL_ROOT=http://${BUNDLE_SERVER}/job/${BUNDLE_PROJECT}/ws/build/buildbundle
BUNDLE_URL=${BUNDLE_URL_ROOT}/${DEPLOY_BUNDLE}

echo '==s===================================================================='
echo Deploying the following bundle...
echo DEPLOY_ROOT=${DEPLOY_ROOT}
echo BUNDLE_SERVER=${BUNDLE_SERVER}
echo BUNDLE_URL=${BUNDLE_URL}
echo DEPLOY_PROFILE=${DEPLOY_PROFILE}
echo DB_PREFIX=${DB_PREFIX}
echo '======================================================================'

# clean out the deploy root
# TODO - leave a rollback set instead of wiping it all
rm -rf ${DEPLOY_ROOT}
mkdir -p ${DEPLOY_ROOT}
cd ${DEPLOY_ROOT}

# get the bundle
curl ${BUNDLE_URL} -o ${DEPLOY_BUNDLE}

# open and deploy the bundle
tar xvzf ${DEPLOY_BUNDLE}
./deployBundle.sh ${RELEASE}.tgz ${DEPLOY_PROFILE}

# do some post deploy steps
ln -s ${RELEASE} current
chmod -R ug+rw ${DEPLOY_ROOT}/current/tomcat
chmod -R ug+x ${DEPLOY_ROOT}/current/tomcat/bin/*.sh

sed -i.bak 's/SHARED/'"${DB_PREFIX}"'/g' ${DEPLOY_ROOT}/current/tomcat/lib/g2mailboxservice-${DEPLOY_PROFILE}.properties
