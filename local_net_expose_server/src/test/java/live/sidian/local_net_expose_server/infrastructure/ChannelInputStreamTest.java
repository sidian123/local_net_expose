package live.sidian.local_net_expose_server.infrastructure;

import cn.hutool.core.util.ArrayUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 默认-1为控制字符
 *
 * @author sidian
 * @date 2020/7/26 15:36
 */
class ChannelInputStreamTest {

    @Test
    void read() throws IOException {
        ChannelInputStream inputStream = new ChannelInputStream(new ByteArrayInputStream(
                new byte[]{-1, -1, 1, 2, 3, -1, -1, -1, -1, 4, 5}
        ));
        byte[] bytes = new byte[1024];
        int len = inputStream.read(bytes);
        assertEquals(8, len);
        assertEquals(join(bytes, len), "-1123-1-145");
    }

    @Test
    void read2() throws IOException {
        ChannelInputStream inputStream = new ChannelInputStream(new ByteArrayInputStream(
                new byte[]{2, 23, -1, 2, 3, -1, -1, -1, -1, 4, 5}
        ));

        byte[] bytes = new byte[1024];
        int len = inputStream.read(bytes);
        assertEquals(len, 2);
        assertEquals(join(bytes, len), "223");

        len = inputStream.read(bytes);
        assertEquals(len, -2);

        len = inputStream.read(bytes);
        assertEquals(len, 6);
        assertEquals(join(bytes, len), "23-1-145");
    }

    String join(byte[] bytes, int len) {
        return ArrayUtil.join(ArrayUtil.sub(bytes, 0, len), "");
    }

}