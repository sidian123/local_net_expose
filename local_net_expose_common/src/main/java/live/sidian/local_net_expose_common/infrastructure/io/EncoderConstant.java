package live.sidian.local_net_expose_common.infrastructure.io;

/**
 * @author sidian
 * @date 2020/7/26 14:12
 */
public interface EncoderConstant {
    /**
     * 控制字符
     */
    byte CONTROLLER = -1;

    /**
     * 操作具体解析权, 在于使用的类
     */
    interface OpConstant {
        byte OPEN = 0; // 打开
        byte CLOSE = 1; // 关闭
    }
}
