import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * 米勒-拉宾素性检验算法实现
 */
public class MillerRabin {
    
    // 全局定义小素数列表
    private static final List<BigInteger> PRIME_LIST = Arrays.asList(
        BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(5),
        BigInteger.valueOf(7), BigInteger.valueOf(11), BigInteger.valueOf(13)
    );
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * 1. 整除性测试函数
     * @param dividend 被除数
     * @param divisor 除数
     * @return 如果能整除返回true，否则返回false
     */
    public static boolean divisibilityTest(BigInteger dividend, BigInteger divisor) {
        if (divisor.equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return dividend.mod(divisor).equals(BigInteger.ZERO);
    }
    
    /**
     * 2. 快速判断函数：检查输入值是否为PRIME_LIST中某素数的倍数
     * @param n 要检查的数
     * @return 0-可能是素数, 1-确定是合数, 2-不确定需进一步测试
     */
    public static int checkSmallPrimes(BigInteger n) {
        // 检查n是否本身就是小素数
        for (BigInteger p : PRIME_LIST) {
            if (n.equals(p)) {
                return 0;  // n是素数
            }
        }
        
        // 检查n是否是小素数的倍数（合数）
        for (BigInteger p : PRIME_LIST) {
            if (divisibilityTest(n, p)) {
                if (!n.equals(p)) {
                    return 1;  // n是合数
                }
            }
        }
        return 2;  // 不确定，需进一步测试
    }
    
    /**
     * 3. 取模幂运算函数：计算 a^b mod n
     * @param a 底数
     * @param b 指数
     * @param n 模数
     * @return a^b mod n 的结果
     */
    public static BigInteger modExp(BigInteger a, BigInteger b, BigInteger n) {
        return a.modPow(b, n);
    }
    
    /**
     * 4. 分解函数：将n-1拆解为 2^s * d（d为奇数）
     * @param n 要分解的数
     * @return 包含s和d的数组，arr[0]=s, arr[1]=d
     */
    public static BigInteger[] decompose(BigInteger n) {
        BigInteger nMinusOne = n.subtract(BigInteger.ONE);
        BigInteger s = BigInteger.ZERO;
        BigInteger d = nMinusOne;
        
        // 循环除2，直到d为奇数
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            s = s.add(BigInteger.ONE);
            d = d.divide(BigInteger.TWO);
        }
        
        return new BigInteger[]{s, d};
    }
    
    /**
     * 5. 随机数生成函数
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return [min, max]范围内的随机数
     */
    public static BigInteger urandomNumber(BigInteger min, BigInteger max) {
        BigInteger range = max.subtract(min).add(BigInteger.ONE);
        
        // 生成随机数
        BigInteger randomValue;
        do {
            randomValue = new BigInteger(range.bitLength(), random);
        } while (randomValue.compareTo(range) >= 0);
        
        return randomValue.add(min);
    }
    
    /**
     * 主函数：米勒-拉宾素性检验
     * @param n 要检验的数
     * @param k 测试次数，默认3次
     * @return 0-可能为素数, 1-确定为合数
     */
    public static int millerRabinTest(BigInteger n, int k) {
        // 基本情况处理
        if (n.compareTo(BigInteger.TWO) < 0) {
            return 1;  // 合数
        }
        if (n.equals(BigInteger.TWO)) {
            return 0;  // 素数
        }
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            return 1;  // 偶数（除2外是合数）
        }
        
        // 先使用小素数快速检查
        int smallPrimeResult = checkSmallPrimes(n);
        if (smallPrimeResult == 0) {
            return 0;  // 确定是素数
        } else if (smallPrimeResult == 1) {
            return 1;  // 确定是合数
        }
        
        // 分解n-1为2^s * d
        BigInteger[] decomp = decompose(n);
        BigInteger s = decomp[0];
        BigInteger d = decomp[1];
        
        // 进行k次测试
        for (int i = 0; i < k; i++) {
            // 生成随机数a ∈ [2, n-2]
            BigInteger a = urandomNumber(BigInteger.TWO, n.subtract(BigInteger.TWO));
            
            // 计算x = a^d mod n
            BigInteger x = modExp(a, d, n);
            
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) {
                continue;  // 通过本轮测试
            }
            
            boolean composite = true;
            // 循环r从1到s-1
            BigInteger r = BigInteger.ONE;
            while (r.compareTo(s) < 0) {
                x = modExp(x, BigInteger.TWO, n);  // x = x^2 mod n
                
                if (x.equals(BigInteger.ONE)) {
                    return 1;  // 发现合数证据
                }
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    composite = false;
                    break;
                }
                r = r.add(BigInteger.ONE);
            }
            
            if (composite) {
                return 1;  // 未通过测试
            }
        }
        
        return 0;  // 通过所有测试，可能为素数
    }
    
    /**
     * 主程序入口
     * @param args 命令行参数：args[0]=要检验的数, args[1]=测试次数(可选)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.exit(2);  // 参数错误
        }
        
        try {
            BigInteger n = new BigInteger(args[0]);
            int k = (args.length > 1) ? Integer.parseInt(args[1]) : 3;
            
            int result = millerRabinTest(n, k);
            System.exit(result);
            
        } catch (NumberFormatException e) {
            System.exit(2);  // 参数错误
        } catch (Exception e) {
            System.exit(2);  // 参数错误
        }
    }
}