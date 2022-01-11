package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @program: xcEduService01
 * @description: 测试文件的上传分块
 * @author: xiaojuzi
 * @create: 2020-12-06 10:47
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {

     //测试文件分块
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\xcEdu\\xc-video\\hls\\lucene.mp4");
        //块文件目录
        String chunFileFolder= "D:\\xcEdu\\xc-video\\chunks";
        //定义块文件大小
        long chunkFileSize= 1 * 1024 *1024;
        //块数
        long chunkFileNum =(long) Math.ceil(sourceFile.length() * 1.0 / chunkFileSize);
        if (chunkFileNum<=0){
            chunkFileNum=1;
        }

        //创建读文件对象
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");

        //缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkFileNum; i++) {
            File chunkFile = new File(chunFileFolder);
            int len = -1;
            //创建向块文件的写对象
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
          while ((len=raf_read.read(bytes))!=-1){

              raf_write.write(bytes,0,len);
              //判断块文件大小
              if (chunkFile.length()>chunkFileSize){
                  break;
              }

          }
          raf_write.close();
        }
        raf_read.close();
    }

    //测试文件的合并
    @Test
    public void testMergeFile() throws IOException{
        //块文件目录
        String chunkFileFloderPath = "";
        //块文件目录对象
        File file = new File(chunkFileFloderPath);
        //块文件列表
        File[] files = file.listFiles();
        //将块文件排序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
               if (Integer.parseInt(o1.getName())> Integer.parseInt(o2.getName())){
                   return 1;
               }
                return -1;
            }
        });

        //合并文件
        File mergFile = new File("");
        if (mergFile.exists()){
            mergFile.delete();
        }
        //创建新文件
        boolean newFile = mergFile.createNewFile();
        //创建写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergFile, "rw");

        byte[] bytes = new byte[1024];

        for (File chunkFile:fileList){
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = raf_read.read(bytes))!=-1){
                raf_write.write(bytes,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }


}
