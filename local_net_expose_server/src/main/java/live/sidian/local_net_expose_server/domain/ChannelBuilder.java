package live.sidian.local_net_expose_server.domain;

import cn.hutool.core.lang.Assert;
import live.sidian.local_net_expose_common.infrastructure.TransmitChannel;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;

/**
 * @author sidian
 * @date 2020/7/31 23:15
 */
public class ChannelBuilder {
    /**
     * 来自外网的连接
     */
    Socket outSocket;

    /**
     * 来自客户端的连接
     */
    @Setter
    Socket localSocket;

    TransmitChannel channel;

    boolean showContent = false;

    public ChannelBuilder(Socket outSocket) {
        this.outSocket = outSocket;
    }

    public ChannelBuilder setShowContent(boolean showContent) {
        this.showContent = showContent;
        return this;
    }


    /**
     * 建立隧道
     */
    public void build() throws IOException {
        Assert.isTrue(canBuild(),
                "socket为null或已关闭");
        channel = new TransmitChannel(localSocket, outSocket);
        channel.setShowContent(showContent);
    }

    private boolean canBuild() {
        return !(localSocket == null || localSocket.isClosed() || outSocket == null || outSocket.isClosed());
    }

    public boolean isOutSocketOk() {
        return outSocket != null && !outSocket.isClosed();
    }

    public void close() {
        channel.close();
    }
}
