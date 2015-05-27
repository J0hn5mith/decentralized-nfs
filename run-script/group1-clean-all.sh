#!/bin/bash

kill -9 `ps -ef | grep DWARFS.jar | grep -v grep | awk '{print $2}'`
umount /root/group1/fs
rm -rf /root/group1/fs
rm -rf /root/group1

