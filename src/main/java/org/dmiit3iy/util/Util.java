package org.dmiit3iy.util;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.List;

public class Util {
    public static ZipFile addZip(List<File> fileList) {

        try (ZipFile zipFile = new ZipFile(new File("client.zip"))) {
            for (File x : fileList) {
                zipFile.addFile(x);
            }
            return zipFile;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
