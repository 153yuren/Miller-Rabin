import java.math.BigInteger;

/**
 * 米勒-拉宾素性检验测试程序
 * 对应原Bash脚本中的文档2功能
 */
public class MillerRabinTester {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("用法: java MillerRabinTester <要检验的数> [测试次数]");
            return;
        }
        
        try {
            // 调用MillerRabin主类
            ProcessBuilder pb = new ProcessBuilder("java", "MillerRabin", args[0], 
                args.length > 1 ? args[1] : "3");
            
            Process process = pb.start();
            int result = process.waitFor();
            
            // 根据返回码输出结果
            switch (result) {
                case 0:
                    System.out.println("可能为素数");
                    break;
                case 1:
                    System.out.println("确定为合数");
                    break;
                case 2:
                    System.out.println("参数错误");
                    break;
                default:
                    System.out.println("未知结果: " + result);
            }
            
        } catch (Exception e) {
            System.err.println("执行错误: " + e.getMessage());
        }
    }
    
    /**
     * 直接调用版本（不通过进程调用）
     * @param n 要检验的数
     * @param k 测试次数
     */
    public static void testDirectly(String n, int k) {
        try {
            BigInteger number = new BigInteger(n);
            int result = MillerRabin.millerRabinTest(number, k);
            
            switch (result) {
                case 0:
                    System.out.println("可能为素数");
                    break;
                case 1:
                    System.out.println("确定为合数");
                    break;
                default:
                    System.out.println("未知结果: " + result);
            }
            
        } catch (NumberFormatException e) {
            System.err.println("错误: 参数必须是有效的整数");
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
        }
    }
}