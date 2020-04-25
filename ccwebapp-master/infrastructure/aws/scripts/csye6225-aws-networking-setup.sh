#!/bin/bash
set -e

#variables
echo "input profile name:"
read profile
echo "input vpc cidrblock"
read vpcCidrBlock
echo "input subnet1 cidrblock"
read subnetCidrBlock1
echo "input subnet2 cidrblock"
read subnetCidrBlock2
echo "input subnet3 cidrblock"
read subnetCidrBlock3
echo "input region"
read region
echo "input vpc name"
read vpcName

if [ ! -n "$profile" ] || [ ! -n "$vpcCidrBlock" ] || [ ! -n "$subnetCidrBlock1" ] || [ ! -n "$subnetCidrBlock2" ] \
|| [ ! -n "$subnetCidrBlock3" ] || [ ! -n "$region" ] || [ ! -n "$vpcName" ]
then
echo "Invalid Parameter"
exit
fi

#create vpc with cidr block /16
aws_response=$(aws ec2 create-vpc --cidr-block $vpcCidrBlock --profile $profile --region $region)
vpcId=$(echo -e  "$aws_response" | jq '.Vpc.VpcId' | tr -d '"') 

#name the vpc
aws ec2 create-tags --resources "$vpcId" --tags Key=Name,Value="$vpcName" --profile $profile --region $region

#get availability zones
aws_zones=$(aws ec2 describe-availability-zones --profile $profile --region $region)
aws_zones1=$(echo -e "$aws_zones" | jq '.AvailabilityZones[0].ZoneName' | tr -d '"')
aws_zones2=$(echo -e "$aws_zones" | jq '.AvailabilityZones[1].ZoneName' | tr -d '"')
aws_zones3=$(echo -e "$aws_zones" | jq '.AvailabilityZones[2].ZoneName' | tr -d '"')

#Create 3 subnets
subnetId1=$(aws ec2 create-subnet --cidr-block $subnetCidrBlock1 --availability-zone "$aws_zones1" --vpc-id "$vpcId" --profile $profile --region $region| jq '.Subnet.SubnetId' | tr -d '"')
subnetId2=$(aws ec2 create-subnet --cidr-block $subnetCidrBlock2 --availability-zone "$aws_zones2" --vpc-id "$vpcId" --profile $profile --region $region| jq '.Subnet.SubnetId' | tr -d '"')
subnetId3=$(aws ec2 create-subnet --cidr-block $subnetCidrBlock3 --availability-zone "$aws_zones3" --vpc-id "$vpcId" --profile $profile --region $region| jq '.Subnet.SubnetId' | tr -d '"')

# create internet gateway
gatewayId=$(aws ec2 create-internet-gateway --profile $profile --region $region| jq '.InternetGateway.InternetGatewayId' | tr -d '"')
# attach gateway to vpc
attach_response=$(aws ec2 attach-internet-gateway --internet-gateway-id "$gatewayId" --vpc-id "$vpcId" --profile $profile --region $region)

#create route table for vpc
route_tableId=$(aws ec2 create-route-table --vpc-id "$vpcId" --profile $profile --region $region| jq '.RouteTable.RouteTableId' | tr -d '"')
#attach all subnets
aws ec2 associate-route-table --subnet-id "$subnetId1" --route-table-id "$route_tableId" --profile $profile --region $region
aws ec2 associate-route-table --subnet-id "$subnetId2" --route-table-id "$route_tableId" --profile $profile --region $region
aws ec2 associate-route-table --subnet-id "$subnetId3" --route-table-id "$route_tableId" --profile $profile --region $region
#create route 
aws ec2 create-route --route-table-id "$route_tableId" --destination-cidr-block 0.0.0.0/0 --gateway "$gatewayId" --profile $profile --region $region

echo "VPC:$vpcId created"
