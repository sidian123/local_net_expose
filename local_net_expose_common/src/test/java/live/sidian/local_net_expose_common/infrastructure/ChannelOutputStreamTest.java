package live.sidian.local_net_expose_common.infrastructure;

import cn.hutool.core.util.ArrayUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author sidian
 * @date 2020/7/26 16:12
 */
class ChannelOutputStreamTest {

    @Test
    void write() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ChannelOutputStream outputStream = new ChannelOutputStream(byteArrayOutputStream);
        outputStream.write(new byte[]{-1, 23, 34, -2, 22});
        outputStream.writeOp((byte) 1);
        outputStream.write(new byte[]{2});

        byte[] bytes = byteArrayOutputStream.toByteArray();
        assertEquals(join(bytes, bytes.length), "-1-12334-222-112");
    }

    String join(byte[] bytes, int len) {
        return ArrayUtil.join(ArrayUtil.sub(bytes, 0, len), "");
    }
}