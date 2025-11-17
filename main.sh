#!/bin/bash

./miller_rabin.sh $1 $2

result=$?

case $result in
    0) echo "可能为素数" ;;
    1) echo "确定为合数" ;;
    2) echo "参数错误" ;;
esac