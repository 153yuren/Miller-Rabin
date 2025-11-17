#!/bin/bash

# 全局定义小素数列表
prime_list=("2" "3" "5" "7" "11" "13")

# 1. 整除性测试函数
divisibility_test() {
    if [ $2 -eq 0 ]; then
        return 2
    fi
    division_result=$(($1 / $2))
    if [ $(($division_result * $2)) -ne $1 ]; then
        return 1
    else
        return 0
    fi
}

# 2. 快速判断函数：检查输入值是否为prime_list中某素数的倍数
check_small_primes() {
    local n=$1
    # 检查n是否本身就是小素数
    for p in "${prime_list[@]}"; do
        if [ "$n" -eq "$p" ]; then
            return 0  # n是素数
        fi
    done
    # 检查n是否是小素数的倍数（合数）
    for p in "${prime_list[@]}"; do
        divisibility_test "$n" "$p"
        ret=$?
        if [ $ret -eq 0 ]; then  # n可被p整除
            if [ "$n" -ne "$p" ]; then
                return 1  # n是合数
            fi
        fi
    done
    return 2  # 不确定，需进一步测试
}

# 3. 取模幂运算函数：计算 a^b mod n（使用bc处理大数）
mod_exp() {
    local a=$1
    local b=$2
    local n=$3
    echo "scale=0; ($a ^ $b) % $n" | bc
}

# 4. 分解函数：将n-1拆解为 2^s * d（d为奇数）
decompose() {
    local n=$1
    local n_minus_one=$(echo "$n - 1" | bc)
    local s=0
    local d=$n_minus_one
    # 循环除2，直到d为奇数
    while [ $(echo "$d % 2" | bc) -eq 0 ]; do
        s=$((s + 1))
        d=$(echo "$d / 2" | bc)
    done
    echo "$s $d"
}

# 5. 随机数生成函数
urandom_number() {
    local max=$1
    local min=$2
    local range=$(($max - $min + 1))
    local random_number=$(( $(od -An -N4 -tu4 /dev/urandom) % range + $min ))
    echo $random_number
}

# 主函数：米勒-拉宾素性检验
miller_rabin_test() {
    local n=$1
    local k=${2:-3}  # 测试次数，默认3次

    # 基本情况处理
    if [ "$n" -lt 2 ]; then
        return 1  # 合数
    fi
    if [ "$n" -eq 2 ]; then
        return 0  # 素数
    fi
    if [ $(echo "$n % 2" | bc) -eq 0 ]; then
        return 1  # 偶数（除2外是合数）
    fi

    # 先使用小素数快速检查
    check_small_primes "$n"
    local ret=$?
    if [ $ret -eq 0 ]; then
        return 0  # 确定是素数
    elif [ $ret -eq 1 ]; then
        return 1  # 确定是合数
    fi

    # 分解n-1为2^s * d
    local decomp=$(decompose "$n")
    local s=$(echo "$decomp" | cut -d' ' -f1)
    local d=$(echo "$decomp" | cut -d' ' -f2)

    # 进行k次测试
    for i in $(seq 1 "$k"); do
        # 生成随机数a ∈ [2, n-2]
        local a=$(urandom_number $(($n-2)) 2)
        # 计算x = a^d mod n
        local x=$(mod_exp "$a" "$d" "$n")
        if [ "$x" -eq 1 ] || [ "$x" -eq "$(echo "$n - 1" | bc)" ]; then
            continue  # 通过本轮测试
        fi
        local composite=1
        # 循环r从1到s-1
        for r in $(seq 1 "$(echo "$s - 1" | bc)"); do
            x=$(mod_exp "$x" "2" "$n")  # x = x^2 mod n
            if [ "$x" -eq 1 ]; then
                return 1  # 发现合数证据
            fi
            if [ "$x" -eq "$(echo "$n - 1" | bc)" ]; then
                composite=0
                break
            fi
        done
        if [ "$composite" -eq 1 ]; then
            return 1  # 未通过测试
        fi
    done
    return 0  # 通过所有测试，可能为素数
}

# 主程序入口
if [ $# -lt 1 ]; then
    exit 2  # 参数错误
fi

n=$1
k=${2:-3}  # 默认测试3次

miller_rabin_test "$n" "$k"
exit $?  # 返回检验结果：0=可能素数，1=合数