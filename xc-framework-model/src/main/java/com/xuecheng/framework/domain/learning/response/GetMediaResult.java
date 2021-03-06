package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-10 13:26
 **/

@Data
@ToString
@NoArgsConstructor
public class GetMediaResult  extends ResponseResult {
    //媒资视频播放地址
    private  String fileUrl;
    public GetMediaResult(ResultCode resultCode,String fileUrl){
        super(resultCode);
        this.fileUrl=fileUrl;
    }

}
