# Copyright 2022 Harness Inc. All rights reserved.
# Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
# that can be found in the licenses directory at the root of this repository, also available at
# https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.

echo "Using cluster: \c"
if ! kubectl config current-context
then
  exit
fi

NAMESPACE=$1
SERVICE=$2
PORT=$3
TARGETPORT=$4
MODULE=$5
SERVICE_CAPSULE=$6


DEPLOY_FILE=/tmp/$SERVICE.yaml
SCRIPT_PATH="$( cd "$(dirname "$0")" ; pwd -P )"
#SCRIPT_PATH=../../$SERVICE/build/container

kubectl get deploy $SERVICE --namespace $NAMESPACE -o yaml > $DEPLOY_FILE
yq $DEPLOY_FILE > $DEPLOY_FILE.swap
yq -i '.spec.template.spec.containers[0].command |= ["/bin/sh", "-ec", "sleep infinity"]' $DEPLOY_FILE

## CHANGES TO DISABLE HEALTH CHECK
yq -i 'del(.spec.template.spec.containers[0].livenessProbe.httpGet)' $DEPLOY_FILE
yq -i '.spec.template.spec.containers[0].livenessProbe.exec.command |= ["ls"]' $DEPLOY_FILE
yq -i 'del(.spec.template.spec.containers[0].readinessProbe.httpGet)' $DEPLOY_FILE
yq -i '.spec.template.spec.containers[0].readinessProbe.exec.command |= ["ls"]' $DEPLOY_FILE

## ENV VARIABLE TO ENABLE REMOTE DEBUG INSIDE THE CONTAINER
yq -i '.spec.template.spec.containers[0].env |= [{"name": "ENABLE_REMOTE_DEBUG","value": "true"}]' $DEPLOY_FILE

# TODO: Add a dirty label to the deployment so it is easy to rollback to the last clean state

## ONLY APPLY CHANGES IF NEEDED
if ! diff $DEPLOY_FILE $DEPLOY_FILE.swap
then
  kubectl apply -f $DEPLOY_FILE
fi

kubectl get pod --namespace $NAMESPACE -l app=$SERVICE -o yaml > /tmp/$SERVICE-pod.yml
POD_NAME=$(yq ".items[0].metadata.name" /tmp/$SERVICE-pod.yml)

## BUILD
echo "Building"
cd "$SCRIPT_PATH"/../../ || exit
sh "$SCRIPT_PATH"/build_jar.sh

## DEPLOY
echo "Deploying (it really takes time)"
kubectl cp $MODULE $NAMESPACE/$POD_NAME:/opt/harness/$SERVICE-CAPSULE
lsof -ti tcp:$HOSTPORT | xargs kill | kubectl --namespace $NAMESPACE port-forward $POD_NAME $HOSTPORT:$TARGETPORT &

## RUN
kubectl exec -it --namespace $NAMESPACE $POD_NAME -- /opt/harness/run.sh
