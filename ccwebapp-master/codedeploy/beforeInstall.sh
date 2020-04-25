#!/bin/bash
sudo pkill -f 'java -jar'
sudo systemctl stop amazon-cloudwatch-agent.service
sudo rm -rf /webapp
