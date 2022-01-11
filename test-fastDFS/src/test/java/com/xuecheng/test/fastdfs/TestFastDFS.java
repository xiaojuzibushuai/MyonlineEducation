package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-24 14:19
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    //上传文件
    @Test
    public void testUpload(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义trackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient
            StorageClient1 storageClient = new StorageClient1(trackerServer,storeStorage);
            //向storage服务器上传文件
            //确定本地文件路径
            String filePath = "D:/like.jpg";
            //上传成功拿到文件id
            String fileId = storageClient.upload_file1(filePath, "jpg", null);
            System.out.println(fileId); //group1/M00/00/00/wKgVgF-UOQaAHtf0ABfDK6Hsg8E502.jpg
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    //查询文件
    @Test
    public void testQuery(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);
                String fileId="M00/00/00/wKgVgF-UFqqAJrvTABfDK6Hsg8E321.jpg";
            FileInfo fileInfo = storageClient.query_file_info("group1", fileId);
            System.out.println(fileInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

    }

    //下载文件
   @Test
    public void testDownload(){
       //加载配置文件
       try {
           ClientGlobal.initByProperties("config/fastdfs-client.properties");
       //定义trackerClient
       TrackerClient trackerClient = new TrackerClient();
       //连接tracker
       TrackerServer trackerServer = trackerClient.getConnection();
       //获取storage
       StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
       //创建storageClient
       StorageClient storageClient = new StorageClient(trackerServer,storeStorage);
       //下载文件
           String fileId="M00/00/00/wKgVgF-UFqqAJrvTABfDK6Hsg8E321.jpg";
           byte[] file = storageClient.download_file("group1", fileId);
           //使用输入流保存文件
           FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/imgLike.jpg"));
           fileOutputStream.write(file);
       } catch (IOException e) {
           e.printStackTrace();
       } catch (MyException e) {
           e.printStackTrace();
       }
       }
}
