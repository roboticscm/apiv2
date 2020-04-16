package vn.com.sky.sys.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com)
 * Date: 3/19/2019
 * Time: 6:10 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuthRequest {
    private String resetToken;
    private String username;
    private String password;
    private Long companyId;
    private Long branchId;
}
