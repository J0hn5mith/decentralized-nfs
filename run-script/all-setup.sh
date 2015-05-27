#!/bin/bash

# List of ODroids"

MASTER="192.168.1.112"

IPS="192.168.1.112
192.168.1.108
192.168.1.105
192.168.1.101"

#IPS="192.168.1.112
#192.168.1.108
#192.168.1.113
#192.168.1.100
#192.168.1.107
#192.168.1.119
#192.168.1.110
#192.168.1.115
#192.168.1.105
#192.168.1.114
#192.168.1.101"

PRIVATE_KEY="odroid.priv"


for i in $IPS;
do
  echo "------------------------------"
  echo "IP: $i"
  echo "... Uploading cleaning script"
  scp -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "group1-clean-all.sh" "root@$i:/root/group1-clean-all.sh"
  echo "... Running cleaning script"
  ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "chmod a+x /root/group1-clean-all.sh"
  ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "/root/group1-clean-all.sh"
  ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "rm /root/group1-clean-all.sh"
  echo "... Creating directories"
  ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "mkdir /root/group1 && mkdir /root/group1/fs"
  echo "... Uploading DWARFS.jar"
  scp -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "DWARFS.jar" "root@$i:/root/group1/DWARFS.jar"
  echo "... Uploading settings.xml"
  scp -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "settings.xml" "root@$i:/root/group1/settings.xml"
  if [ "$i" = "$MASTER" ]
    then
      echo "... Uploading run_master.sh"
      scp -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "run_master.sh" "root@$i:/root/group1/run_master.sh"
      if [ -z "$1" ]
        then
          echo "... Starting DWARFS master peer"
          ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "chmod a+x /root/group1/run_master.sh"
          ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "cd /root/group1/;sh run_master.sh"
      fi
    else
      echo "... Uploading run.sh"
      scp -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "run.sh" "root@$i:/root/group1/run.sh"
      if [ -z "$1" ]
        then
          echo "... Starting DWARFS peer"
          ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "chmod a+x /root/group1/run.sh"
          ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "cd /root/group1/; sh run.sh $MASTER"
      fi
  fi
  sleep 1

done

echo "--------------------------------"
echo "Running DWARFS!"

