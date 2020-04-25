provider "aws" {
    region = "us-east-1"
    profile = var.aws_profile
}

variable "aws_profile" {
    description = "aws profile name"
    default = "dev"
}

variable "domain_name" {
    description = "domain name"
}