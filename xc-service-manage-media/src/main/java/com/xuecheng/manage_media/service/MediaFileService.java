package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-08 10:20
 **/
@Service
public class MediaFileService {

    @Autowired
    MediaFileRepository mediaFileRepository;
    @Autowired
    MediaUploadService mediaUploadService;

    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if (queryMediaFileRequest == null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        //条件值对象
        MediaFile mediaFile = new MediaFile();
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains());
//  精准匹配withMatcher("processStatus",ExampleMatcher.GenericPropertyMatchers.exact());

        //定义example对象
        Example<MediaFile> example = Example.of(mediaFile,exampleMatcher);
        //分页查询对象
        if (page<=0){
            page = 1;
        }
        if (size<=10){
            size = 10;
        }
        page= page-1;

        Pageable pageable = new PageRequest(page,size);
        //分页查询
        Page<MediaFile> mediaFiles = mediaFileRepository.findAll(example,pageable);
        long total = mediaFiles.getTotalElements();
        List<MediaFile> content = mediaFiles.getContent();
        //返回的数据集
        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setTotal(total);
        queryResult.setList(content);
        //返回结果
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    /**
     * 手动通知视频处理生成mp4等文件
     * @param mediaId
     * @return
     */
    public ResponseResult process(String mediaId) {
        //通知mq处理
        mediaUploadService.sendProcessVideoMsg(mediaId);

      return new ResponseResult(CommonCode.SUCCESS);
    }
}
