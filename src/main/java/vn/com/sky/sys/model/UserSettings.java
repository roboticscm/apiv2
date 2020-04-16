package vn.com.sky.sys.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table
public class UserSettings {
    @Id
    private Long id;

    private Long userId;
    private String menuPath;
    private String controlId;
    private String key;
    private String value;
}
