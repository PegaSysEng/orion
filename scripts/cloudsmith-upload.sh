#!/bin/bash
set -euo pipefail

ORION_VERSION=${1:?Must specify orion version}
TAR_DIST=${2:?Must specify path to tar distribution}
ZIP_DIST=${3:?Must specify path to zip distribution}

ENV_DIR=./build/tmp/cloudsmith-env
if [[ -d ${ENV_DIR} ]] ; then
    source ${ENV_DIR}/bin/activate
else
    python3 -m venv ${ENV_DIR}
    source ${ENV_DIR}/bin/activate
fi

python3 -m pip install --upgrade cloudsmith-cli

cloudsmith push raw consensys/orion $TAR_DIST --name 'orion.tar.gz' --version "${ORION_VERSION}" --summary "Orion ${ORION_VERSION} binary distribution" --description "Binary distribution of Orion ${ORION_VERSION}." --content-type 'application/tar+gzip'
cloudsmith push raw consensys/orion $ZIP_DIST --name 'orion.zip' --version "${ORION_VERSION}" --summary "Orion ${ORION_VERSION} binary distribution" --description "Binary distribution of Orion ${ORION_VERSION}." --content-type 'application/zip'