#!/bin/bash

# List of ODroids"
IPS="192.168.1.112
192.168.1.108
192.168.1.113
192.168.1.100
192.168.1.104
192.168.1.107
192.168.1.118
192.168.1.119
192.168.1.110
192.168.1.115
192.168.1.105
192.168.1.114
192.168.1.101"

PRIVATE_KEY="odroid.priv"

#first argument is the command
CMD=$1
GROUP_FOLDER="~/group_1/"

for i in $IPS;
do
  echo "Execute $CMD on $i..."
  ssh -o "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "root@$i" "cd $GROUP_FOLDER;$CMD"
done

