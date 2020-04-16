/**
 *
 */
package vn.com.sky.redis;

import lombok.Data;

/**
 * @author roboticscm2018@gmail.com (khai.lv)
 * Created date: Apr 3, 2019
 */
@Data
public class LoginInfo {
    private Long time;
    private String token;
    private Long userId;
    private String username;
    private String fullname;
    private Long comanyId;
    private Long branchId;
    private String appName;
    private String clientDevice;
    private String remoteAddr;
    private String clientOs;
    private String clientBrowser;
}
