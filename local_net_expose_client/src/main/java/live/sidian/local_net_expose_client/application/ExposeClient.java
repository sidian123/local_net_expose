package live.sidian.local_net_expose_client.application;

import java.io.IOException;

/**
 * @author sidian
 * @date 2020/8/1 11:21
 */
public interface ExposeClient {
    /**
     * 登入server
     */
    void login() throws IOException;
}
