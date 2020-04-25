#!/bin/bash
set -e
#variables
echo "input profile name:"
read profile
echo "input vpc cidrblock"
read vpcCidrBlock
echo "input region"
read region
echo "input vpc name"
read vpcName

if [ ! -n "$profile" ] || [ ! -n "$vpcCidrBlock" ] || [ ! -n "$region" ] || [ ! -n "$vpcName" ]
then
echo "Invalid Parameter"
exit
fi

vpcId=$(aws ec2 describe-vpcs --profile $profile --region $region --filter "Name=cidr,Values='$vpcCidrBlock'" "Name=tag:Name,Values=$vpcName" | jq '.Vpcs[0].VpcId' | tr -d '"')
if [ -z "$vpcId" ] || [ "$vpcId" == "null" ]
then
    echo "No such Vpc!"
    exit
fi
echo $vpcId

subnetResponse=$(aws ec2 describe-subnets --profile $profile --region $region --filter "Name=vpc-id,Values='$vpcId'")
subnet1Id=$(echo -e "$subnetResponse" | jq '.Subnets[0].SubnetId' | tr -d '"')
subnet2Id=$(echo -e "$subnetResponse" | jq '.Subnets[1].SubnetId' | tr -d '"')
subnet3Id=$(echo -e "$subnetResponse" | jq '.Subnets[2].SubnetId' | tr -d '"')

#delete subnet
if [ ! -z "$subnet1Id" ] && [ "$subnet1Id" != "null" ]
then
aws ec2 delete-subnet --subnet-id $subnet1Id --profile $profile
fi
if [ ! -z "$subnet2Id" ] && [ "$subnet2Id" != "null" ]
then
aws ec2 delete-subnet --subnet-id $subnet2Id --profile $profile
fi
if [ ! -z "$subnet3Id" ] && [ "$subnet3Id" != "null" ]
then
aws ec2 delete-subnet --subnet-id $subnet3Id --profile $profile
fi

#delete gateway
gatewayId=$(aws ec2 describe-internet-gateways --filter "Name=attachment.vpc-id,Values='$vpcId'" --profile $profile | jq '.InternetGateways[0].InternetGatewayId' | tr -d '"')
aws ec2 detach-internet-gateway --internet-gateway-id $gatewayId --vpc-id $vpcId --profile $profile
aws ec2 delete-internet-gateway --internet-gateway-id $gatewayId --profile $profile

#delete routeTable
routeTableId1=$(aws ec2 describe-route-tables --filter "Name=vpc-id,Values='$vpcId'" --profile $profile | jq '.RouteTables[0].RouteTableId' | tr -d '"')
routeMain1=$(aws ec2 describe-route-tables --filter "Name=vpc-id,Values='$vpcId'" --profile $profile | jq '.RouteTables[0].Associations[0].Main' | tr -d '"')
routeTableId2=$(aws ec2 describe-route-tables --filter "Name=vpc-id,Values='$vpcId'" --profile $profile | jq '.RouteTables[1].RouteTableId' | tr -d '"')
routeMain2=$(aws ec2 describe-route-tables --filter "Name=vpc-id,Values='$vpcId'" --profile $profile | jq '.RouteTables[1].Associations[0].Main' | tr -d '"')
if [ ! -z "$routeTableId1" ] && [ "$routeTableId1" != "null" ] && [ "$routeMain1" != "true" ]
then
    aws ec2 delete-route-table --route-table-id $routeTableId1 --profile $profile
fi
if [ ! -z "$routeTableId2" ] && [ "$routeTableId2" != "null" ] && [ "$routeMain2" != "true" ]
then
    aws ec2 delete-route-table --route-table-id $routeTableId2 --profile $profile
fi

#delete vpc
aws ec2 delete-vpc --vpc-id $vpcId --profile $profile --region $region

echo "delete successfully"