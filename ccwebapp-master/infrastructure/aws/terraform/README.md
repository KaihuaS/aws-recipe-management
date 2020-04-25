# Preparation
### Install terraform
1. Download from ```https://www.terraform.io/downloads.html```
2. Add to environment vairable path
```export='$PATH:{terraform_path}'```

# Usage
### Configuration
please modify variables.tf file for networking configuration

### Init the terraform
```terraform init```
### Create new vpc
```terraform plan```
```terraform apply``` Enter yes to create new vpc
### Destroy old vpc
```terraform destroy``` Enter yes to delete old vpc

### Create mutiple vpcs
1. Create different workspace for different vpc
    ```
    # workspace w1 for vpc1
    terraform workspace new w1
    # workspace w2 for vpc2
    terraform workspace new w2
    ``` 

2. Run above create/destroy script in different workspace