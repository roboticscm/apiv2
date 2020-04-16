/**
 *
 */
package vn.com.sky.sys.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

/**
 * @author roboticscm2018@gmail.com (khai.lv)
 * Created date: Apr 17, 2019
 */

@Data
@NoArgsConstructor
@Table
public class LoginDetails extends GenericEntity {
    String username;
    Long companyId;
    Long branchId;
    Long departmentId;
    String token;
    String ip;
    String device;
    String os;
    String city;
    String country;
    String browser;
    String location;
    String exactLocation;
    String serviceProvider;
}
