# Preparation
### Install jq (JSON processor)
```sudo apt-get install jq```
### Make file executable

    chmod u+x csye6225-aws-networking-setup.sh
    chmod u+x csye6225-aws-networking-setup.sh

# Usage

### Set up new vpc
```./csye6225-aws-networking-setup.sh```
Input values (Profile name, vpc IP(in CIDR block),three subnet IPs(in CIDR block), vpc Name, region name)

### Destroy old vpc
```./csye6225-aws-networking-setup.sh```
Input values (Profile name, vpc IP(in CIDR block), vpc Name, region Name)