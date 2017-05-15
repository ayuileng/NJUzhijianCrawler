package com.iip.nju.crawler.fujian;

import com.iip.nju.util.RarZipUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xu on 2017/5/7.
 */
public class Depress {
    private static final String path = "./attachment";
    private static List<String> fileList = new ArrayList<>();

    public static List<String> getFileList(String filePath, String extension) {

        File dir = new File(filePath);
        File[] files = dir.listFiles();
        if(files==null){
            return null;
        }
        for (File file : files) {
            if(file.isDirectory()){
                getFileList(file.getAbsolutePath(),extension);
            }else {
                if (file.getName().endsWith(extension)){
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }

    public static void depressRarAndZip() throws Exception {
        List<String> rar = getFileList("C:\\Users\\63117\\IdeaProjects\\zhijianCrawler_4.30\\attachment", "rar");
        List<String> zip = getFileList("C:\\Users\\63117\\IdeaProjects\\zhijianCrawler_4.30\\attachment", "zip");
        for (String s : rar) {
            RarZipUtils.deCompress(s,"./attachment/depress");
        }
        for (String s : zip) {
            RarZipUtils.deCompress(s,"./attachment/depress");
        }
    }


    @Test
    public void test2() throws Exception {
        depressRarAndZip();
    }

}
