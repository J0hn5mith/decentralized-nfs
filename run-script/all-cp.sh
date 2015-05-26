#!/bin/bash

#192.168.1.107
#192.168.1.118

# List of ODroids"
IPS="
192.168.1.110
192.168.1.112
192.168.1.108
192.168.1.113
192.168.1.100
192.168.1.104
192.168.1.119
192.168.1.115
192.168.1.105
192.168.1.114
192.168.1.101
"

PRIVATE_KEY="./odroid.priv"


#first argument is the source
SRC=$1
#second argument is the destination
DST=$2

GROUP_DIR="~/group_1/"


for i in $IPS;
do
  echo "Copy $SRC to $i..."
  echo "DST path is: #DST"
  scp -ro "StrictHostKeyChecking no" -i "$PRIVATE_KEY" "$SRC" "root@$i:$GROUP_DIR$DST"
done

