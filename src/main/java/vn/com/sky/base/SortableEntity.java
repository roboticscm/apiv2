/**
 *
 */
package vn.com.sky.base;

import lombok.Data;

/**
 * @author roboticscm2018@gmail.com (khai.lv)
 * Created date: Apr 21, 2019
 */
@Data
public class SortableEntity extends GenericEntity {
    private Long sort = 0L;
}
