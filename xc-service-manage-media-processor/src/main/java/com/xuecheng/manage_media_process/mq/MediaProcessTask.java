package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-07 13:43
 **/
@Component
public class MediaProcessTask {

    @Autowired
    MediaFileRepository mediaFileRepository;
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;
    @Value("${xc-service-manage-media.upload-location}")
    String serverPath;


    //接收视频处理消息
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        //1.解析消息内容得到media
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId =(String) map.get("mediaId");
        //2.拿mediaId从数据库查询文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()){
            return;
        }
        MediaFile mediaFile = optional.get();
        String fileType = mediaFile.getFileType();
        if (!fileType.equals("avi")){
            mediaFile.setProcessStatus("303004");//无需处理
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            mediaFile.setProcessStatus("303001");//处理中
            mediaFileRepository.save(mediaFile);
        }
        //3.使用工具类将avi文件生成mp4
        //要处理的视频文件路径
        String video_path=serverPath+mediaFile.getFilePath()+mediaFile.getFileName();
        //生成的mp4文件名称
        String mp4_name=mediaFile.getFileId()+".mp4";
        //生成的mp4文件路径
        String mp4_floderPath=serverPath+mediaFile.getFilePath();
        //工具类对象
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_floderPath);
        String result = mp4VideoUtil.generateMp4();
        if (result ==null || !result.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            //记录失败原因
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //4.将MP4文件生成m3u8和ts文件
        String mp4_video_path = serverPath + mediaFile.getFilePath()+mp4_name;
        //m3u8文件名称
        String m3u8_name = mediaFile.getFileId() +".m3u8";
        //m3u8文件目录
        String m3u8_folder_path = serverPath + mediaFile.getFilePath() +"hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4_video_path,m3u8_name,m3u8_folder_path);
         result = hlsVideoUtil.generateM3u8();
        if (result == null || !result.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            //记录失败原因
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
          //处理成功
         //获取ts列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();

        mediaFile.setProcessStatus("303002");

        //定义mediaFileProcess_m3u8
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //保存fileUrl 就是视频播放的相对路径
         String fileUrl = mediaFile.getFilePath()+"hls/"+m3u8_name;
         mediaFile.setFileUrl(fileUrl);
         mediaFileRepository.save(mediaFile);
    }
}
