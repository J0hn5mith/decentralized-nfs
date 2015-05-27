#!/usr/bin/env bash

MASTADDR="$1:1773"

nohup java -jar -Xmx512M -XX:MaxDirectMemorySize=512M DWARFS.jar -p 1773 -a $MASTADDR -m fs 1>log.txt 2>log.txt &
echo "... DWARFS peer started (master peer at $MASTADDR)"
