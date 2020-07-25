package live.sidian.local_net_expose_server.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 端口暴露记录
 *
 * @author sidian
 * @date 2020/7/24 22:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {@Index(columnList = "clientId")})
public class ExposeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * server上暴露端口
     */
    Long serverPort;
    /**
     * client上暴露端口
     */
    Long clientPort;

    /**
     * 状态. 0启用, 1禁用
     */
    Integer status;

    /**
     * 应用id
     */
    Long clientId;


    public interface ExposeRecordStatus {
        int enable = 0;
        int disable = 1;
    }
}
