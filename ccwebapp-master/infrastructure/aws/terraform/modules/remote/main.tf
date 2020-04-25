# s3_bucket for terraform remote state
resource "aws_s3_bucket" "my_s3_bucket_terraform" {

  bucket = "terraform-state.${var.domain_name}"
  force_destroy = true
  acl    = "private"

  server_side_encryption_configuration {
      rule {
        apply_server_side_encryption_by_default {
          sse_algorithm     = "AES256"
      }
    }
  }

  lifecycle_rule {
    enabled = true
    expiration {
      days = 60
    }
  }

  #     lifecycle_rule {
  #
  #         # Any Terraform plan that includes a destroy of this resource will
  #         # result in an error message.
  #         #
  #         prevent_destroy = true
  #     }
}