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
 * Time: 6:11 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SimpleAuthResponse {
    private String password;
}
