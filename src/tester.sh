#! /bin/sh -
java -version
n=${1?}
while [ "$n" -gt 0 ]; do
  java q1 4096 4096 4 16
  n=$(( n - 1 ))
done | awk '
  {sum += $1}
  END {if (NR) print sum / NR}'
