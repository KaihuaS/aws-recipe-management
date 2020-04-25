output "vpc_id" {
  description = "The ID of the VPC"
  value       = aws_vpc.this.id
}

output "default_subnet_id"{
  description = "The first subnet of the VPC"
  value = aws_subnet.subnet1.id
}

output "default_route_table"{
  description = "The route table id"
  value = aws_route_table.public.id
}

output "codeEC2DeployServiceRoleProfile"{
  description = "code deploy profile"
  value = aws_iam_instance_profile.codeEC2DeployServiceRoleProfile.name
}

output "subnet1" {
  value = aws_subnet.subnet1
}

output "subnet2" {
  value = aws_subnet.subnet2
}