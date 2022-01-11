package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-08-15 16:27
 **/
@Api(value = "cms页面管理接口",description = "cms页面管理接口，提供页面的增删改查")
public interface CmsPageControllerApi {

     //页面查询
    @ApiOperation("分页查询列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value = "每页记录",required=true,paramType="path",dataType="int")
    })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

     //新增页面
    @ApiOperation("新增页面")
    public CmsPageResult add(CmsPage cmsPage);

    //根据页面id查询页面信息
    @ApiOperation("根据页面id查询页面信息")
    @ApiImplicitParam(name="id",value = "页面id",required=true,paramType="path",dataType="String")
    public CmsPage findById(String id);

    //修改页面
    @ApiOperation("修改页面")
    @ApiImplicitParam(name="id",value = "页面id",required=true,paramType="path",dataType="String")
   public CmsPageResult edit(String id,CmsPage cmsPage);

    //删除页面
    @ApiOperation("删除页面")
    @ApiImplicitParam(name="id",value = "页面id",required=true,paramType="path",dataType="String")
    public ResponseResult delete(String id);

    //页面发布
    @ApiOperation("页面发布")
    @ApiImplicitParam(name="pageId",value = "页面id",required=true,paramType="path",dataType="String")
    public ResponseResult release(String pageId);

    @ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
