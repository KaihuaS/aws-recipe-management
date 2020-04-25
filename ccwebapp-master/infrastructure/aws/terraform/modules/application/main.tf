module "network" {
  source = "../networking"
  aws_region = var.aws_region
  vpc_name = var.vpc_name
  vpc_cidr = var.vpc_cidr
  aws_account_id = var.aws_account_id
  public_cidrs = var.public_cidrs
  aws_profile = var.aws_profile
}

data "aws_sns_topic" "email_request" {
  name = "email_request"
}

data "aws_availability_zones" "available" {
    state = "available"
}

# app_security_group
resource "aws_security_group" "application" {
  name        = "application"
  description = "security group for web application"
  vpc_id = module.network.vpc_id

  ingress {
    # TLS (change to whatever ports you need)
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    # Please restrict your ingress to only necessary IPs and ports.
    # Opening to 0.0.0.0/0 can lead to security vulnerabilities.
    cidr_blocks = ["0.0.0.0/0"] # add your IP address here
  }

  ingress {
    # TLS (change to whatever ports you need)
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    # Please restrict your ingress to only necessary IPs and ports.
    # Opening to 0.0.0.0/0 can lead to security vulnerabilities.
    cidr_blocks = ["0.0.0.0/0"] # add your IP address here
  }

  ingress {
    # TLS (change to whatever ports you need)
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    # Please restrict your ingress to only necessary IPs and ports.
    # Opening to 0.0.0.0/0 can lead to security vulnerabilities.
    cidr_blocks = ["0.0.0.0/0"] # add your IP address here
  }

  ingress {
    # TLS (change to whatever ports you need)
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    # Please restrict your ingress to only necessary IPs and ports.
    # Opening to 0.0.0.0/0 can lead to security vulnerabilities.
    cidr_blocks = ["0.0.0.0/0"] # add your IP address here
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "application"
  }
}

# db_security_group
resource "aws_security_group" "web_application" {
  name        = "web_application"
  description = "security group for web application"
  vpc_id = module.network.vpc_id

  ingress {
    # TLS (change to whatever ports you need)
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    # cidr_blocks = ["0.0.0.0/0"]
    security_groups = [aws_security_group.application.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "web application"
  }

  depends_on = [aws_security_group.application]
}

# db_security_group
resource "aws_security_group" "database" {
  name        = "database"
  description = "security group for database"
  vpc_id = module.network.vpc_id

  ingress {
    # TLS (change to whatever ports you need)
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    # cidr_blocks = ["0.0.0.0/0"]
    security_groups = [aws_security_group.web_application.id]
  }

  tags = {
    Name = "database"
  }

  depends_on = [aws_security_group.web_application]
}

# s3_bucket
resource "aws_s3_bucket" "my_s3_bucket_resource" {


  bucket = "webapp.${var.domain_name}"
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
    transition {
      days = 30
      storage_class = "STANDARD_IA"
    }
  }

  #     lifecycle_rule {
  #
  #         # Any Terraform plan that includes a destroy of this resource will
  #         # result in an error message.
  #         #
  #         prevent_destroy = true
  #     }
  depends_on = [module.network]
}

# s3_bucket for circleCI
resource "aws_s3_bucket" "my_s3_bucket_circleCI" {

  bucket = "codedeploy.${var.domain_name}"
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
  depends_on = [module.network]
}

# subnet group
resource "aws_subnet" "rds_subnet1" {
  vpc_id = module.network.vpc_id
  cidr_block = var.rds_subnet_cidrs[0]
  map_public_ip_on_launch = true
  availability_zone = data.aws_availability_zones.available.names[0]
}
resource "aws_subnet" "rds_subnet2" {
  vpc_id = module.network.vpc_id
  cidr_block = var.rds_subnet_cidrs[1]
  map_public_ip_on_launch = true
  availability_zone = data.aws_availability_zones.available.names[1]
}
resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "rds_subnet_group"
  subnet_ids = [aws_subnet.rds_subnet1.id,aws_subnet.rds_subnet2.id]
  tags = {
    Name = "rds_subnet_group"
  }
}
# subnet association
resource "aws_route_table_association" "rds1" {
  subnet_id      = aws_subnet.rds_subnet1.id
  route_table_id = module.network.default_route_table
}
resource "aws_route_table_association" "rds2" {
  subnet_id      = aws_subnet.rds_subnet2.id
  route_table_id = module.network.default_route_table
}

# rds instance
resource "aws_db_instance" "rds" {
  allocated_storage = 5
  engine = "mysql"
  engine_version = "8.0"
  instance_class = "db.t2.medium"
  multi_az = false
  username = "dbuser"
  password = "Passw0rd!"
  publicly_accessible = true
  vpc_security_group_ids = [aws_security_group.database.id]
  db_subnet_group_name = "rds_subnet_group"
  name = "csye6225"
  skip_final_snapshot = true
  depends_on = [module.network, aws_db_subnet_group.rds_subnet_group]
}

# ec2 instance
# resource "aws_instance" "web" {
#     ami = var.ami_id
#     instance_type = "t2.micro"
#     iam_instance_profile = module.network.codeEC2DeployServiceRoleProfile
#     disable_api_termination = false
#     root_block_device {
#         delete_on_termination = true
#         volume_size = 20
#         volume_type = "gp2"
#     }
#     subnet_id = module.network.default_subnet_id
#     key_name = "csye6225_rsa"
#     vpc_security_group_ids = [aws_security_group.application.id]
#     depends_on = [aws_db_instance.rds, module.network]

#     tags = {
#       Name = "csye6225_codeDeploy",
#       CodeDeploy = "YES"
#     }

#     user_data = <<EOF
# #!/bin/bash
# sudo touch /bin/setenv.sh
# sudo chmod 777 /bin/setenv.sh
# sudo echo 'export SPRING_DATASOURCE_RDS="${aws_db_instance.rds.address}"' >> /bin/setenv.sh
# sudo echo 'export SPRING_DATASOURCE_USERNAME="${aws_db_instance.rds.username}"' >> /bin/setenv.sh
# sudo echo 'export SPRING_DATASOURCE_PASSWORD="${aws_db_instance.rds.password}"' >> /bin/setenv.sh
# sudo echo 'export CLOUD_CREDENTIALS_PROFILE="${var.aws_profile}"' >> /bin/setenv.sh
# sudo echo 'export CLOUD_REGION="${var.aws_region}"' >> /bin/setenv.sh
# sudo echo 'export CLOUD_S3_DOMAIN="${aws_s3_bucket.my_s3_bucket_resource.bucket}"' >> /bin/setenv.sh
# EOF
# }

# dynamodb table
resource "aws_dynamodb_table" "dynamodb_table" {
    name = "csye6225"
    hash_key = "id"
    read_capacity  = 5
    write_capacity = 5
    attribute {
        name = "id"
        type = "S"
    }
}

resource "aws_lb" "front" {
  name               = "front"
  internal           = false
  load_balancer_type = "application"
  security_groups    = ["${aws_security_group.application.id}"]
  subnets            = ["${module.network.subnet1.id}","${module.network.subnet2.id}"]

  enable_deletion_protection = false

  tags = {
    Environment = "production"
  }
}

resource "aws_lb_target_group" "front" {
  name     = "targetgroup"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = "${module.network.vpc_id}"

  health_check {
    path = "/v1/recipes"
    port = 8080
  }
}

resource "aws_lb_listener" "front" {
  load_balancer_arn = "${aws_lb.front.arn}"
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = "${var.certificate_arn}"

  default_action {
    type             = "forward"
    target_group_arn = "${aws_lb_target_group.front.arn}"
  }

}

resource "aws_launch_configuration" "asg_launch_config" {
  name          = "asg_launch_config"
  image_id      = "${var.ami_id}"
  instance_type = "t2.micro"
  key_name = "csye6225_rsa"
  associate_public_ip_address = true
  iam_instance_profile = module.network.codeEC2DeployServiceRoleProfile
  security_groups = [aws_security_group.web_application.id]
  user_data = <<EOF
#!/bin/bash
sudo touch /bin/setenv.sh
sudo chmod 777 /bin/setenv.sh
sudo echo 'export SPRING_DATASOURCE_RDS="${aws_db_instance.rds.address}"' >> /bin/setenv.sh
sudo echo 'export SPRING_DATASOURCE_USERNAME="${aws_db_instance.rds.username}"' >> /bin/setenv.sh
sudo echo 'export SPRING_DATASOURCE_PASSWORD="${aws_db_instance.rds.password}"' >> /bin/setenv.sh
sudo echo 'export CLOUD_CREDENTIALS_PROFILE="${var.aws_profile}"' >> /bin/setenv.sh
sudo echo 'export CLOUD_REGION="${var.aws_region}"' >> /bin/setenv.sh
sudo echo 'export CLOUD_S3_DOMAIN="${aws_s3_bucket.my_s3_bucket_resource.bucket}"' >> /bin/setenv.sh
sudo echo 'export CLOUD_TOPIC_ARN="${data.aws_sns_topic.email_request.arn}"' >> /bin/setenv.sh
sudo echo 'export DOMAIN_NAME="https://${var.aws_profile}.${var.domain_name}"' >> /bin/setenv.sh
EOF
}

resource "aws_autoscaling_policy" "scale_up_policy" {
  name                   = "scale_up_policy"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = "${aws_autoscaling_group.autoscaling_group.name}"
}

resource "aws_autoscaling_policy" "scale_down_policy" {
  name                   = "scale_down_policy"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = "${aws_autoscaling_group.autoscaling_group.name}"
}

resource "aws_cloudwatch_metric_alarm" "alarm_high" {
  alarm_name          = "alarm_high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Average"
  threshold           = "5"

  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.autoscaling_group.name}"
  }

  alarm_description = "Scale-up if CPU > 5% for 1 minutes"
  alarm_actions     = ["${aws_autoscaling_policy.scale_up_policy.arn}"]
}

resource "aws_cloudwatch_metric_alarm" "alarm_low" {
  alarm_name          = "alarm_low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Average"
  threshold           = "3"

  dimensions = {
    AutoScalingGroupName = "${aws_autoscaling_group.autoscaling_group.name}"
  }

  alarm_description = "Scale-up if CPU < 3% for 1 minutes"
  alarm_actions     = ["${aws_autoscaling_policy.scale_down_policy.arn}"]
}

resource "aws_autoscaling_group" "autoscaling_group" {
  name                      = "autoscaling_group"
  max_size                  = 10
  min_size                  = 3
  health_check_grace_period = 300
  health_check_type         = "ELB"
  desired_capacity          = 3
  force_delete              = true
  default_cooldown          = 60
  launch_configuration      = "${aws_launch_configuration.asg_launch_config.name}"
  vpc_zone_identifier       = ["${module.network.subnet1.id}","${module.network.subnet2.id}"]
  wait_for_elb_capacity     = 3

  tag {
    key                 = "Name"
    value               = "csye6225_codeDeploy"
    propagate_at_launch = true
  }

  tag {
    key                 = "CodeDeploy"
    value               = "YES"
    propagate_at_launch = true
  }

  timeouts {
    delete = "15m"
  }

  depends_on = [aws_launch_configuration.asg_launch_config]
}

data "aws_route53_zone" "primary" {
  name         = "dev.${var.domain_name}"
}

resource "aws_route53_record" "front" {
  zone_id = "${data.aws_route53_zone.primary.zone_id}"
  name    = "dev.${var.domain_name}"
  type    = "A"

  alias {
    name                   = "${aws_lb.front.dns_name}"
    zone_id                = "${aws_lb.front.zone_id}"
    evaluate_target_health = true
  }
}

resource "aws_autoscaling_attachment" "asg_attachment_target" {
  autoscaling_group_name = "${aws_autoscaling_group.autoscaling_group.name}"
  alb_target_group_arn = "${aws_lb_target_group.front.arn}"
}


resource "aws_cloudformation_stack" "owasp_template" {
  name = "owasp-template"

  template_body = "${file("${path.module}/owasp_10_base.yml")}" 
}

resource "aws_wafregional_web_acl_association" "attach_waf" {
  resource_arn = "${aws_lb.front.arn}"
  web_acl_id   = "${aws_cloudformation_stack.owasp_template.outputs.wafWebACL}"
}

resource "aws_iam_role" "codeDeployServiceRole" {
  name = "codeDeployServiceRole"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "codedeploy.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
  role       = "${aws_iam_role.codeDeployServiceRole.name}"
}


resource "aws_codedeploy_app" "code_app" {
  name             = "csye6225-webapp"
}

resource "aws_codedeploy_deployment_group" "code_app_group" {
  app_name              = aws_codedeploy_app.code_app.name
  deployment_group_name = "csye6225-webapp-deployment"
  deployment_config_name = "CodeDeployDefault.AllAtOnce"
  service_role_arn      = aws_iam_role.codeDeployServiceRole.arn
  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE"]
  }
  autoscaling_groups = [ aws_autoscaling_group.autoscaling_group.name ]

  deployment_style {
    deployment_option = "WITHOUT_TRAFFIC_CONTROL"
    deployment_type   = "IN_PLACE"
  }

  ec2_tag_set {
    ec2_tag_filter {
      key   = "Name"
      type  = "KEY_AND_VALUE"
      value = "csye6225_codeDeploy"
    }

    ec2_tag_filter {
      key   = "CodeDeploy"
      type  = "KEY_AND_VALUE"
      value = "YES"
    }
  }
}