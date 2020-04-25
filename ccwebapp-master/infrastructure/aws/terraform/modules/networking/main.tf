locals{
    vpc_id = aws_vpc.this.id
}

data "aws_availability_zones" "available" {
    state = "available"
}

# vpc
resource "aws_vpc" "this" {
    cidr_block = var.vpc_cidr
    enable_dns_hostnames = true

    tags = {
        Name = var.vpc_name
    }
}

# internet gateway
resource "aws_internet_gateway" "this" {
    vpc_id = local.vpc_id
}

# route table
resource "aws_route_table" "public" {
    vpc_id = local.vpc_id
}

# route
resource "aws_route" "public_internet_gateway" {
    route_table_id         = aws_route_table.public.id
    destination_cidr_block = "0.0.0.0/0"
    gateway_id             = aws_internet_gateway.this.id

    timeouts {
        create = "5m"
    }
}

# three subnets
resource "aws_subnet" "subnet1" {
    vpc_id = local.vpc_id
    cidr_block = var.public_cidrs[0]
    map_public_ip_on_launch = true
    availability_zone = data.aws_availability_zones.available.names[0]
}

resource "aws_subnet" "subnet2" {
    vpc_id = local.vpc_id
    cidr_block = var.public_cidrs[1]
    map_public_ip_on_launch = true
    availability_zone = data.aws_availability_zones.available.names[1]
}

resource "aws_subnet" "subnet3" {
    vpc_id = local.vpc_id
    cidr_block = var.public_cidrs[2]
    map_public_ip_on_launch = true
    availability_zone = data.aws_availability_zones.available.names[2]
}

# subnet association
resource "aws_route_table_association" "public1" {
  subnet_id      = aws_subnet.subnet1.id
  route_table_id = aws_route_table.public.id
}
resource "aws_route_table_association" "public2" {
  subnet_id      = aws_subnet.subnet2.id
  route_table_id = aws_route_table.public.id
}
resource "aws_route_table_association" "public3" {
  subnet_id      = aws_subnet.subnet3.id
  route_table_id = aws_route_table.public.id
}

# create circle iam user
resource "aws_iam_user" "circle" {
  name = "circleUser"
  path = "/system/"

  tags = {
    tag-key = "tag-value"
  }
}

resource "aws_iam_access_key" "circle" {
  user = aws_iam_user.circle.name
}

resource "aws_iam_user_policy" "circle_policy" {
  name = "circle_policy"
  user = aws_iam_user.circle.name

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [{
      "Effect": "Allow",
      "Action" : [
        "ec2:AttachVolume",
        "ec2:AuthorizeSecurityGroupIngress",
        "ec2:CopyImage",
        "ec2:CreateImage",
        "ec2:CreateKeypair",
        "ec2:CreateSecurityGroup",
        "ec2:CreateSnapshot",
        "ec2:CreateTags",
        "ec2:CreateVolume",
        "ec2:DeleteKeyPair",
        "ec2:DeleteSecurityGroup",
        "ec2:DeleteSnapshot",
        "ec2:DeleteVolume",
        "ec2:DeregisterImage",
        "ec2:DescribeImageAttribute",
        "ec2:DescribeImages",
        "ec2:DescribeInstances",
        "ec2:DescribeInstanceStatus",
        "ec2:DescribeRegions",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeSnapshots",
        "ec2:DescribeSubnets",
        "ec2:DescribeTags",
        "ec2:DescribeVolumes",
        "ec2:DetachVolume",
        "ec2:GetPasswordData",
        "ec2:ModifyImageAttribute",
        "ec2:ModifyInstanceAttribute",
        "ec2:ModifySnapshotAttribute",
        "ec2:RegisterImage",
        "ec2:RunInstances",
        "ec2:StopInstances",
        "ec2:TerminateInstances"
      ],
      "Resource" : "*"
  }]
}
EOF
}

resource "aws_iam_user_policy" "circleCI-upload-to-s3" {
    name = "circleCI-upload-to-s3"
    user = aws_iam_user.circle.name
    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
EOF
}

resource "aws_iam_user_policy" "circleCI-code-deploy" {
    name = "circleCI-code-deploy"
    user = aws_iam_user.circle.name
    policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:RegisterApplicationRevision",
        "codedeploy:GetApplicationRevision"
      ],
      "Resource": [
        "arn:aws:codedeploy:${var.aws_region}:${var.aws_account_id}:application:csye6225-webapp"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetDeployment"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:GetDeploymentConfig"
      ],
      "Resource": [
        "arn:aws:codedeploy:${var.aws_region}:${var.aws_account_id}:deploymentconfig:CodeDeployDefault.OneAtATime",
        "arn:aws:codedeploy:${var.aws_region}:${var.aws_account_id}:deploymentconfig:CodeDeployDefault.HalfAtATime",
        "arn:aws:codedeploy:${var.aws_region}:${var.aws_account_id}:deploymentconfig:CodeDeployDefault.AllAtOnce"
      ]
    }
  ]
}
EOF
}

# create iam role CodeDeployServiceRole
resource "aws_iam_role_policy" "codeDeploy-ec2-s3" {
    name = "codeDeploy-ec2-s3"
    role = aws_iam_role.codeEC2DeployServiceRole.id
    policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "s3:Get*",
                "s3:List*",
                "s3:PutObject",
                "s3:DeleteObject",
                "cloudwatch:PutMetricData",
                "ec2:DescribeVolumes",
                "ec2:DescribeTags",
                "logs:PutLogEvents",
                "logs:DescribeLogStreams",
                "logs:DescribeLogGroups",
                "logs:CreateLogStream",
                "logs:CreateLogGroup",
                "sns:Publish"
            ],
            "Effect": "Allow",
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role" "codeEC2DeployServiceRole"{
    name = "codeEC2DeployServiceRole"
    assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_instance_profile" "codeEC2DeployServiceRoleProfile" {
  name = "codeEC2DeployServiceRoleProfile"
  role = aws_iam_role.codeEC2DeployServiceRole.name
}



