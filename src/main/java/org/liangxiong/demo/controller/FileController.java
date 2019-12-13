package org.liangxiong.demo.controller;

import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.DefaultFastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liangxiong
 * @version 1.0.0
 * @date 2019-12-13
 * @description 文件相关操作:上传/下载/删除
 **/
@Slf4j
@RequestMapping("/fdfs")
@RestController
public class FileController {

    private DefaultFastFileStorageClient client;

    @Autowired
    public FileController(DefaultFastFileStorageClient client) {
        this.client = client;
    }

    /**
     * 上传文件
     *
     * @param file
     */
    @PostMapping("/upload")
    public void uploadAndDownloadAndDeleteFile(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                // 文件相关元信息
                Set<MetaData> metaData = createMetaData();
                if (log.isInfoEnabled()) {
                    log.info("上传文件: {}", file.getOriginalFilename());
                }
                // 上传文件
                String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
                StorePath storePath = client.uploadFile(file.getInputStream(), file.getSize(), extension, metaData);
                String group = storePath.getGroup();
                String path = storePath.getPath();
                Set<MetaData> fetchMetaData = client.getMetadata(group, path);
                if (log.isInfoEnabled()) {
                    log.info("获取元信息: {}", fetchMetaData);
                    log.info("文件所在组: {}", group);
                    log.info("文件所在路径: {}", path);
                }
                DownloadCallback callback = new DownloadByteArray();
                byte[] bytes = (byte[]) client.downloadFile(group, path, callback);
                FileCopyUtils.copy(bytes, new File("C:/Users/Administrator/Desktop/test." + extension));
                client.deleteFile(group, path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Set<MetaData> createMetaData() {
        Set<MetaData> metaDataSet = new HashSet<MetaData>();
        metaDataSet.add(new MetaData("Author", "xiangqian"));
        metaDataSet.add(new MetaData("CreateDate", "2019-12-15"));
        return metaDataSet;
    }


}
