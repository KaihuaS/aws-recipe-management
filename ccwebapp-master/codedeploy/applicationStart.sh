#!/bin/bash
source /bin/setenv.sh
sudo chmod 777 -R /webapp
sudo mkdir -p /webapp/logs
sudo systemctl restart amazon-cloudwatch-agent.service
cd /webapp/logs
nohup java -jar /webapp/target/ccwebapp-1.0-SNAPSHOT.jar >webapp.out 2>webapp.err &