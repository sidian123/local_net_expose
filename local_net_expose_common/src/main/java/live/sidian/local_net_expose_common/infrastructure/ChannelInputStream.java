package live.sidian.local_net_expose_common.infrastructure;

import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import static live.sidian.local_net_expose_common.infrastructure.EncoderConstant.CONTROLLER;

/**
 * @author sidian
 * @date 2020/7/26 14:36
 */
@Slf4j
public class ChannelInputStream extends InputStream {
    PushbackInputStream in;

    public ChannelInputStream(InputStream in) {
        this.in = new PushbackInputStream(in, 1024);
    }

    /**
     * 读取流
     *
     * @param b 用于存储数据
     * @return 读取的字节长度; -1表示读到了文件尾; -2 遇到命令, 接下来的第一个字节为命令操作符
     */
    @Override
    public int read(byte[] b) throws IOException {
        // 读取
        int len = in.read(b);
        // 解析
        byte[] resBytes = new byte[b.length];
        int index = 0;
        int status = 0; // 状态, 0遇到普通字符; 1遇到一个控制字符; 2 转义的字节
        for (int i = 0; i < len; i++) {
            if (b[i] == CONTROLLER) {
                status++;
            }
            // 判断状态
            if (status == 0) { // 普通字符
                resBytes[index++] = b[i];
            } else if (status == 2) { // 转义的字符
                // 跳过, 因为这里只有控制字符才需要转义.
                status = 0;
            } else if (status == 1 && b[i] != CONTROLLER) { // 这是个命名操作数
                // 若控制符前有数据, 则控制符后及控制符本身重新压入流中
                if (index > 1) { // 有数据
                    // 压入流中
                    in.unread(b, i - 1, len - i + 1);
                    // 返回控制符前面的数据
                    index--;
                    for (int j = 0; j < index; j++) {
                        b[j] = resBytes[j];
                    }
                    return index;
                } else { // 无数据
                    // 操作数及之后字节入流
                    in.unread(b, i, len - i);
                    return -2;
                }
            } else if (i == len - 1) { // 碰到了控制字符, 但是最后一个
                // 判断接下来是转义还是命令, 因此该字符重入流中
                in.unread(b[i]);
            } else { // 碰到了控制字符, 不是最后一个
                resBytes[index++] = b[i];
            }
        }
        // 覆盖b
        for (int i = 0; i < index; i++) {
            b[i] = resBytes[i];
        }
        return index;
    }


    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(ArrayUtil.sub(b, off, len));
    }


    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }
}
