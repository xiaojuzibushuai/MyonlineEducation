package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * @program: xcEduService01
 * @description: 媒资管理接口
 * @author: xiaojuzi
 * @create: 2020-12-06 15:28
 **/
@Api(value = "媒资管理接口",description = "媒资管理接口，提供文件的上传下载处理等接口")
public interface MediaUploadControllerApi {


    @ApiOperation("文件上传注册")
    public ResponseResult register(String fileMd5,String fileName,Long fileSize,String mimeType,String fileExt);

    @ApiOperation("校验分块")
    public CheckChunkResult checkChunk(String fileMd5,Integer chunk,Integer chunkSize);

    @ApiOperation("上传分块")
    public ResponseResult uploadChunk(MultipartFile file, String fileMd5, Integer chunk);

    @ApiOperation("合并文件")
    public ResponseResult mergeChunks(String fileMd5,String fileName,Long fileSize,String mimeType,String fileExt);

}
