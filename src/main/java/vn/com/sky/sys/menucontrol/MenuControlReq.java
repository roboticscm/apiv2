package vn.com.sky.sys.menucontrol;

import java.util.List;
import lombok.Data;
import vn.com.sky.sys.model.MenuControl;

@Data
public class MenuControlReq {
    private String menuPath;
    private List<MenuControl> menuControls;
}
