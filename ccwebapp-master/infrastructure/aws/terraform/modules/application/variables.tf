variable "ami_id" {
    description = "ami id"
}

variable "aws_profile" {
    description = "aws profile name"
}

variable "rds_subnet_cidrs" {
    description = "rds subnet cidrs list"
}

variable "aws_region" {
    description = "aws region"
    default = "us-east-1"
}

variable "vpc_name" {
    description = "Please enter the name for this VPC"
    default = "test3"
}

variable "vpc_cidr" {
    description = "Please enter the IP range (CIDR notation) for this VPC"
    default = "10.0.0.0/16"
}

variable "public_cidrs" {
    description = "Please enter the IP range (CIDR notation) for the public subnet in the first Availability Zone"
    default = ["10.0.48.0/24","10.0.56.0/24","10.0.64.0/24"]
}

variable "domain_name" {
    description = "domain name"
}

variable "aws_account_id" {
    description = "account id"
}

variable "certificate_arn" {
  description = "ssl certificate arn"
}


provider "aws" {
    region = "us-east-1"
    profile = var.aws_profile
}

