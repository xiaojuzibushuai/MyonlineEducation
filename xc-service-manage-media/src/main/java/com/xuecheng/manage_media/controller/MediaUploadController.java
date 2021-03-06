package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-06 18:38
 **/
@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {

    @Autowired
    MediaUploadService mediaUploadService;

    @Override
    @PostMapping("/register")
    public ResponseResult register(  String fileMd5,
                                    String fileName,
                                    Long fileSize,
                                    String mimeType,
                                    String fileExt) {
        return mediaUploadService.register(fileMd5,fileName,fileSize,mimeType,fileExt);
    }

    @Override
    @PostMapping("/checkchunk")
    public CheckChunkResult checkChunk(String fileMd5,
                                       Integer chunk,
                                       Integer chunkSize) {
        return mediaUploadService.checkchunk(fileMd5,chunk,chunkSize);
    }

    @Override
    @PostMapping("/uploadchunk")
    public ResponseResult uploadChunk( MultipartFile file,
                                       String fileMd5,
                                       Integer chunk) {
        return mediaUploadService.uploadchunk(file,fileMd5,chunk);
    }

    @Override
    @PostMapping("/mergechunks")
    public ResponseResult mergeChunks(String fileMd5,
                                      String fileName,
                                      Long fileSize,
                                      String mimeType,
                                      String fileExt) {
        return mediaUploadService.mergechunks(fileMd5,fileName,fileSize,mimeType,fileExt);
    }
}
