package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-06 18:41
 **/
@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;


    @Value("${xc‐service‐manage‐media.upload‐location}")
    String uploadPath;
    @Value("${xc‐service‐manage‐media.mq.routingkey-media-video}")
    String routingkey_media_video;



    //文件上传前的注册，检查文件是否存在
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {

        //1.检查文件在磁盘上是否存在
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //判断文件是否存在
        boolean exists = file.exists();
        //2.检查文件在mongodb是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()){
            //文件已经存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()){
            fileFolder.mkdirs();
        }
            return new ResponseResult(CommonCode.SUCCESS);
    };

    //得到文件所属目录的路径
    private String getFileFolderPath(String fileMd5){
        return uploadPath+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/" +fileMd5+"/";
    }

    /*** 根据文件md5得到文件路径
     * 规则：
     * * 一级目录：md5的第一个字符
     * * 二级目录：md5的第二个字符
     * * 三级目录：md5 * 文件名：md5+文件扩展名 *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     * */
    //得到文件的路径
    private String getFilePath(String fileMd5,String fileExt) {

        return uploadPath+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"
                +fileMd5+"/"+fileMd5+"."+fileExt;
    }
    //得到块文件所属目录的路径
    private String getChunkFileFolderPath(String fileMd5){
        return uploadPath+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/" +fileMd5+"/chunk/";
    }

    //得到文件所属相对目录的路径
    private String getRelativeFilePath(String fileMd5,String fileExt){
        return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/" +fileMd5+"/";
    }


    /**
     * 检查分块
     * @param fileMd5
     * @param chunk 块的下标
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
         //得到块文件所在目录
        String fileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //块的文件名称
        File file = new File(fileFolderPath + chunk);
        if (file.exists()){
            return  new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }else {
            return  new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,false);
        }
    }

    /**
     * 上传分块
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //检查分块目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //分块路径
        String chunkFilePath = chunkFileFolderPath+chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        //判断是否存在
        if (!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        //得到上传文件输入流
        InputStream inputStream=null;
        FileOutputStream fileOutputStream=null;
        try {
            inputStream=file.getInputStream();
            fileOutputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,fileOutputStream);
        }catch (IOException e){
         e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimeType
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {
       //获取分块文件路径
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File file = new File(chunkFileFolderPath);
        //分块文件列表
        File[] files = file.listFiles();
        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //执行合并
         mergeFile = this.mergeFile(Arrays.asList(files), mergeFile);
        if (mergeFile ==null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //检验文件md5值
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //将文件写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件相对路径
        mediaFile.setFilePath(this.getRelativeFilePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimeType);
        mediaFile.setFileType(fileExt);
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //向mq发送视频处理消息
        this.sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);

    };

    //发送视频处理消息给mq
     public ResponseResult sendProcessVideoMsg(String mediaId){
         //查询mediaFile
         Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
         if (!optional.isPresent()){
             ExceptionCast.cast(CommonCode.FAIL);
         }
         //构建消息内容
         HashMap<String, String> hashMap = new HashMap<>();
         hashMap.put("mediaId",mediaId);
         String jsonString = JSON.toJSONString(hashMap);
         //向mq发送视频处理消息
         try {
             rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
         }catch (AmqpException e){
             e.printStackTrace();
             return new ResponseResult(CommonCode.FAIL);
         }
         return new ResponseResult(CommonCode.SUCCESS);
     }


    //校验文件md5
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            //创建文件输入流
            FileInputStream fileInputStream = new FileInputStream(mergeFile);
            //得到文件md5
            String md5Hex= DigestUtils.md5Hex(fileInputStream);
            //和传入的md5比较
            if (md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
        return false;
    }


    //合并文件
    private File mergeFile(List<File> chunkFileList,File mergeFile){
       try {
           if (mergeFile.exists()) {
               mergeFile.delete();
           } else {
               mergeFile.createNewFile();
           }
           //对块文件排序
           Collections.sort(chunkFileList, new Comparator<File>() {
               @Override
               public int compare(File o1, File o2) {
                   if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                       return 1;
                   }
                   return -1;
               }
           });

           RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
           byte[] bytes = new byte[1024];
           for (File chunkFile : chunkFileList) {
               RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
               int len = -1;
               while ((len = raf_read.read(bytes)) != -1) {
                   raf_write.write(bytes, 0, len);
               }
               raf_read.close();
           }
           raf_write.close();
           return mergeFile;
       }catch (IOException e){
           e.printStackTrace();
           return null;
       }
    }
}
