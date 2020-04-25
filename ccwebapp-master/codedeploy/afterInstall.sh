#!/bin/bash
sudo systemctl daemon-reload
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/webapp/cloudwatch/config.json \
    -s
