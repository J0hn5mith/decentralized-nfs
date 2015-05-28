#/bin/bash

# Test script for the P2P Challenge Task 2015

echo "create 1kb file"

tr '\0' 'X' < /dev/zero | dd of=xfile-1kb.txt count=1 bs=1024 &> /dev/null
cp xfile-1kb.txt xfile-1kb-1.txt
cp xfile-1kb.txt xfile-1kb-2.txt
cp xfile-1kb.txt xfile-1kb-3.txt
sleep 2

echo "create directory, and create a 1mb file"
mkdir csg
cd csg
tr '\0' 'Y' < /dev/zero | dd of=yfile-1mb.txt count=1024 bs=1024 &> /dev/null
sleep 2

echo "copy and move files, and create a 5mb file"
cp yfile-1mb.txt yfile-1mb-copy.txt
mkdir group
mv yfile-1mb-copy.txt group/yfile-1mb-copy.txt
cd group
tr '\0' 'Z' < /dev/zero | dd of=zfile-5mb.txt count=4096 bs=1024 &> /dev/null
sleep 2

echo "copy file, append data, read files"
cp zfile-5mb.txt ../zfile-5mb-copy.txt
cd ..
cat ../xfile-1kb.txt >> group/yfile-1mb-copy.txt
echo "expected: 0b82c7d1b6a136dd14cf2018a1a2af1b57563853 group/yfile-1mb-copy.txt"
echo "actual  :" `sha1sum group/yfile-1mb-copy.txt`
echo "expected: 9045932103582a1fec559a42dbb5d823973cadfa zfile-5mb-copy.txt"
echo "actual  :" `sha1sum zfile-5mb-copy.txt`
cd ..
sleep 2

echo "create empty file, delete files"
touch delete.me
rm csg/group/yfile-1mb-copy.txt  csg/group/zfile-5mb.txt
rmdir csg/group
rm csg/zfile-5mb-copy.txt csg/yfile-1mb.txt
rm delete.me

BEFORE=`ls -1 | wc -l`
rmdir csg
AFTER1=`ls -1 | wc -l`
rm xfile-1kb.txt
AFTER2=`ls -1 | wc -l`

echo "expected: 5, 4, 3"
echo "actual  : $BEFORE, $AFTER1, $AFTER2"

#this one could be a bit tricky
rm *.txt
echo "0 ==" `ls -1 | wc -l`
