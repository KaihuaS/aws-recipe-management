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
aws cloudformation delete-stack --stack-name $stackName --profile $profile --region $region

echo "delete successfully"