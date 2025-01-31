#! /bin/sh -
n=${1?}
t=${2?}
while [ "$n" -gt 0 ]; do
  java q1 1920 1080 $t 24
  n=$(( n - 1 ))
done | awk '
  {sum += $1}
  END {if (NR) print sum / NR}'