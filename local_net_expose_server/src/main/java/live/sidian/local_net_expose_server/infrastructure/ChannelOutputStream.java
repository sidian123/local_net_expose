package live.sidian.local_net_expose_server.infrastructure;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import static live.sidian.local_net_expose_server.infrastructure.EncoderConstant.CONTROLLER;

/**
 * @author sidian
 * @date 2020/7/26 14:37
 */
public class ChannelOutputStream {

    OutputStream out;

    public ChannelOutputStream(OutputStream out) {
        this.out = out;
    }


    public void write(byte[] b) throws IOException {
        out.write(escape(b));
    }

    /**
     * 转义
     *
     * @param rawBytes 未修改的字节组
     * @return 结果字节组
     */
    public byte[] escape(byte[] rawBytes) {
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
}
