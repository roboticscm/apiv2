package vn.com.sky.sys.localeresource;

import java.util.ArrayList;
import lombok.Data;
import vn.com.sky.sys.model.LocaleResource;

@Data
public class LocalResourceReq {
    private ArrayList<LocaleResource> addArray;
    private ArrayList<LocaleResource> editArray;
    private ArrayList<LocaleResource> removeArray;
}
