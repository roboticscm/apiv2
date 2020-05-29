package vn.com.sky.util;

import java.io.File;

public class FileUtil {
	public static void createDir(String path) {
	    File directory = new File(path);
	    if (! directory.exists()){
	    	directory.mkdirs();
	    }
	}
	
}
