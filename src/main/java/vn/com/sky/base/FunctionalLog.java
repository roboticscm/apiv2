/**
 *
 */
package vn.com.sky.base;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author roboticscm2018@gmail.com (khai.lv)
 * Created date: Apr 12, 2019
 */
@Data
@Table
public class FunctionalLog {
    @Id
    private Long Id;

    private Long companyId;
    private Long branchId;
    private String app;
    private String screen;
    private String function;
    private String host;
    private String serverIp;
    private String clientIp;
    private String device;
    private String os;
    private Long createdDate;
    private String updaterId;
    private String updaterName;
    private String details;
    private Boolean show;
    private String browser;
    private String mainObjectId;
    private String mainObjectName;
    private String action;
    private String reason;
}
