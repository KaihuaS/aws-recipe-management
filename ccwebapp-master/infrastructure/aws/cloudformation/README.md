# Preparation
### Make file executable

    chmod u+x csye6225-aws-cf-create-stack.sh
    chmod u+x csye6225-aws-cf-terminate-stack.sh

# Usage

### Parameters file
To configure vpc IP, subnets IP and vpc name, please modify csye6225-cf-networking.yaml

### Set up new vpc
```./csye6225-aws-cf-create-stack.sh```
Input values(Profile name, stack name, region name)

### Destroy old vpc
```./csye6225-aws-cf-terminate-stack.sh```
Input values(Profile name, stack name, region name)