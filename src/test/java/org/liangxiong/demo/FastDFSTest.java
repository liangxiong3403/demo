package org.liangxiong.demo;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * @author liangxiong
 * @version 1.0.0
 * @date 2019-12-13
 * @description FastDFS测试
 **/
public class FastDFSTest {

    /**
     * 查看测试
     */
    @Test
    public void testLookFile() {
        TrackerGroup trackerGroup = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("192.168.199.70", 22122)});
        ClientGlobal.setG_tracker_group(trackerGroup);
        StorageClient1 client = new StorageClient1();
        try {
            FileInfo result = client.get_file_info1("group1/M00/00/00/wKjHRl3zgHWACaQAAK8f2ghN_2w873.pdf");
            Assertions.assertNotNull(result);
            Assertions.assertEquals(11476954, result.getFileSize());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查上传测试
     */
    @Test
    public void testUploadFile() {
        TrackerGroup trackerGroup = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("192.168.199.70", 22122)});
        ClientGlobal.setG_tracker_group(trackerGroup);
        StorageClient1 client = new StorageClient1();
        String filePath = "C:/Users/Administrator/Desktop/spring-boot-reference.pdf";
        String extension = StringUtils.getFilenameExtension(filePath);
        try {
            String[] result = client.upload_file(filePath, extension, null);
            Arrays.stream(result).forEach(System.out::println);
            Assertions.assertNotNull(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载测试
     */
    @Test
    public void testDownloadFile() {
        TrackerGroup trackerGroup = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("192.168.199.70", 22122)});
        ClientGlobal.setG_tracker_group(trackerGroup);
        StorageClient1 client = new StorageClient1();
        String fileId = "group1/M00/00/00/wKjHRl3zgHWACaQAAK8f2ghN_2w873.pdf";
        String extension = StringUtils.getFilenameExtension(fileId);
        try {
            byte[] result = client.download_file1(fileId);
            Assertions.assertNotNull(result);
            FileCopyUtils.copy(result, new File("C:/Users/Administrator/Desktop/test." + extension));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件测试
     */
    @Test
    public void testDeleteFile() {
        String fileId = "group1/M00/00/00/wKjHRl3zgHWACaQAAK8f2ghN_2w873.pdf";
        TrackerGroup trackerGroup = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("192.168.199.70", 22122)});
        ClientGlobal.setG_tracker_group(trackerGroup);
        StorageClient1 client = new StorageClient1();
        try {
            int result = client.delete_file1(fileId);
            Assertions.assertEquals(0, result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

}
