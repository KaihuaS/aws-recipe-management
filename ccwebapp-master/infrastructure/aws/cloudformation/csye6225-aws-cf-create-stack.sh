#!/bin/bash
set -e

echo "input profile name:"
read profile
echo "input stack name"
read stackName
echo "input region"
read region

if [ ! -n "$profile" ] || [ ! -n "$stackName" ] || [ ! -n "$region" ]
then
echo "Invalid Parameter"
exit
fi

aws cloudformation create-stack --stack-name $stackName --template-body file://$PWD/csye6225-cf-networking.yaml --profile $profile --region $region --parameters file://$PWD/csye6225-cf-parameters.json
