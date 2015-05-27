#!/usr/bin/env bash

nohup java -jar -Xmx512M -XX:MaxDirectMemorySize=512M DWARFS.jar -p 1773 -n -m fs 1>log.txt 2>log.txt &
echo "... DWARFS master peer started"
