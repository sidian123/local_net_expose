package live.sidian.local_net_expose_server.domain;

/**
 * 程序运行状态
 *
 * @author sidian
 * @date 2020/7/26 11:28
 */
public class AppStatus {
    public static String status = AppStatusConstant.INITIALIZING;

    public static void ready() {
        status = AppStatusConstant.READY;
    }

    public interface AppStatusConstant {
        String INITIALIZING = "initializing";
        String READY = "ready";
    }
}
