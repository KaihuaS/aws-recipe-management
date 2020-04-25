### Networking module
1. Create Virtual Private Cloud (VPC).
2. Create subnets in your VPC. You must create 3 subnets, each in different availability zone in the same region in the same VPC.
3. Create Internet Gateway resource. and attach the Internet Gateway to the VPC.
4. Create a public route table. Attach all subnets created above to the route table.
5. Create a public route in the public route table created above with destination CIDR block 0.0.0.0/0 and internet gateway created above as the target.