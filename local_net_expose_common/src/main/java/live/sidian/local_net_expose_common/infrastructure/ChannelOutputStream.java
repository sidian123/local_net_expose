package live.sidian.local_net_expose_common.infrastructure;

import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import static live.sidian.local_net_expose_common.infrastructure.EncoderConstant.CONTROLLER;

/**
 * @author sidian
 * @date 2020/7/26 14:37
 */
@Slf4j
public class ChannelOutputStream extends OutputStream {

    OutputStream out;

    public ChannelOutputStream(OutputStream out) {
        this.out = out;
    }


    @Override
    public void write(byte[] b) throws IOException {
        out.write(escape(b));
    }
    /**
     * 写入命令
     *
     * @param op 命令
     */
    public void writeOp(byte op) throws IOException {
        out.write(new byte[]{CONTROLLER, op});
    }

    /**
     * 转义
     *
     * @param rawBytes 未修改的字节组
     * @return 结果字节组
     */
    private byte[] escape(byte[] rawBytes) {
        LinkedList<Byte> bytes = new LinkedList<>();
        // 转义
        for (byte rawByte : rawBytes) {
            if (CONTROLLER == rawByte) {
                bytes.add(CONTROLLER);
            }
            bytes.add(rawByte);
        }
        // unwrap
        byte[] res = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            res[i] = bytes.get(i);
        }
        return res;
    }


    @Override
    public void write(int b) throws IOException {
        if (CONTROLLER == b) {
            write(new byte[]{(byte) b});
        } else {
            out.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(ArrayUtil.sub(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
