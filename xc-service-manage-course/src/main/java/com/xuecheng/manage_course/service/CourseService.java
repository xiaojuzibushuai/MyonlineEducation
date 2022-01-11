package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-21 21:05
 **/
@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId){
        return  teachplanMapper.selectList(courseId);
    }

    //课程计划添加
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            //取出该课题的根节点
             parentid = getTeachplanList(courseid);
        }
        //找出父节点
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        String parentNodeGrade = parentNode.getGrade();

        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝到新对象中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        //级别根据父节点级别设置
        if (parentNodeGrade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
          teachplanRepository.save(teachplanNew);
         return  new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根节点，查不到就添加根节点
    private String getTeachplanList(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //查询课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList ==null || teachplanList.size()<=0){
            //查询不到 自动添加根结点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根节点id
        return  teachplanList.get(0).getId();
    }

    /**
     * 查询课程列表
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId,int page,int size,CourseListRequest courseListRequest){
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //将公司id参数传入dao
        courseListRequest.setCompanyId(companyId);

        if (page<=0){
            page=0;
        }
        if (size<=0){
            size=10;
        }

        //设置分页参数
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //查询列表
        List<CourseInfo> result = courseListPage.getResult();
        //总记录数
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setList(result);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS,courseInfoQueryResult);
    }

    /**
     * 添加课程提交
     * @param courseBase
     * @return
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return  new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    /**
     * 获取课程基础信息
     * @param courseId
     * @return
     * @throws RuntimeException
     */
    public CourseBase getCourseBaseById(String courseId) throws RuntimeException{
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
            return null;
    }

    /**
     * 更新课程基础信息
     * @param id
     * @param courseBase
     * @return
     */
    @Transactional
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        CourseBase base = this.getCourseBaseById(id);
        if (base ==null){
            ExceptionCast.cast(CommonCode.SERVER_ERROR);
        }
        //修改课程信息
        base.setName(courseBase.getName());
        base.setMt(courseBase.getMt());
        base.setSt(courseBase.getSt());
        base.setGrade(courseBase.getGrade());
        base.setStudymodel(courseBase.getStudymodel());
        base.setUsers(courseBase.getUsers());
        base.setDescription(courseBase.getDescription());
        courseBaseRepository.save(base);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *获取课程营销信息
     * @param courseId
     * @return
     */
    public CourseMarket getCourseMarketById(String courseId){
         Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
         if (optional.isPresent()){
               return optional.get();
           }
           return  null;
       }

    /**
     * 更新课程营销信息
     * @param id
     * @param courseMarket
     * @return
     */
    @Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket market = this.getCourseMarketById(id);
        if (market==null){
           //添加课程营销信息
            market = new CourseMarket();
            BeanUtils.copyProperties(courseMarket,market);
            //设置课程id
            market.setId(id);
            courseMarketRepository.save(market);
        }else {
            market.setCharge(courseMarket.getCharge());
            market.setStartTime(courseMarket.getStartTime());
            //课程有效期，开始时间
            market.setEndTime(courseMarket.getEndTime());
            //课程有效期，结束时间
            market.setPrice(courseMarket.getPrice());
            market.setQq(courseMarket.getQq());
            market.setValid(courseMarket.getValid());
            courseMarketRepository.save(market);
        }
        return market;
    }

    /**
     * 向课程管理数据库添加课程与图片关联信息
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
              coursePic=optional.get();
        }
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result>0){
             return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //查询课程视图 包括基本信息、图片、营销、课程计划
    public CourseView findCourseView(String courseId) {
        CourseView courseView = new CourseView();
        //课程信息
        CourseBase courseBase = this.getCourseBaseById(courseId);
        courseView.setCourseBase(courseBase);
        //课程营销
        CourseMarket courseMarketById = this.getCourseMarketById(courseId);
        courseView.setCourseMarket(courseMarketById);
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        //课程图片
        CoursePic coursePic = this.findCoursePic(courseId);
        courseView.setCoursePic(coursePic);
        return courseView;
    }


    /**
     * 课程预览方法
     * @param id
     * @return
     */
    public CoursePublishResult preview(String id) {
       //请求cms 添加页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");

        CourseBase courseBase = this.getCourseBaseById(id);
        cmsPage.setPageAliase(courseBase.getName());

        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);//模板id
        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            //抛出异常
               return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //拼装页面预览的url
        String pageId = cmsPageResult.getCmsPage().getPageId();
        String url=previewUrl+pageId;
        //返回CoursePublishResult对象
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBase = this.getCourseBaseById(id);
        //准备页面信息
        CmsPage cmsPage  = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);//模板id
        //调用cms一键发布接口
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程的发布状态
        CourseBase courseBase1 = this.saveCoursePubState(id);

        if (courseBase1 == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程索引信息
         //创建coursePub对象
        CoursePub coursePub = createCoursePub(id);
         //保存coursePub对象到数据库
        saveCoursePub(id,coursePub);
        //缓存课程信息

        //得到页面url
        String pageUrl = cmsPostPageResult.getPageUrl();

        //向teachplanMediaPub中保存课程媒资信息
        this.saveTeachplanMediaPub(id);

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //向teachplanMediaPub中保存课程媒资信息
    private void saveTeachplanMediaPub(String courseId){
          //先删除teachplanMediaPub表中数据
         teachplanMediaPubRepository.deleteByCourseId(courseId);
          //从teachplanMedia表中查询数据
         List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
          //将teachplanMediaList插入到teachplanMediaPub中
         List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
         for (TeachplanMedia t:teachplanMediaList){
             TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
             BeanUtils.copyProperties(t,teachplanMediaPub);
             //添加时间戳
             teachplanMediaPub.setTimestamp(new Date());
             teachplanMediaPubs.add(teachplanMediaPub);
         }
         teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }



    //将coursePub对象保存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
       //根据课程id查询coursepub
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        CoursePub coursePubNew = null;
        if (coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }
        //将coursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //设置时间戳
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);

        //保存对象
        coursePubRepository.save(coursePubNew);
        return  coursePubNew;
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //根据课程id查询course_base
        Optional<CourseBase> Baseoptional = courseBaseRepository.findById(id);
        if (Baseoptional.isPresent()){
            CourseBase courseBase = Baseoptional.get();
            //将coursebase属性拷贝到CoursePub中
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        //查询营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }
        //将课程计划信息转换为json串存储
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return  coursePub;
    }



    //更新课程状态 已发布 202002
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBaseById = this.getCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    /**
     * 保存课程计划与媒资文件的关联信息
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia ==null ||StringUtils.isEmpty(teachplanMedia.getMediaId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //校验课程计划是否为3级
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optionalTeachplan = teachplanRepository.findById(teachplanId);
        if (!optionalTeachplan.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optionalTeachplan.get();
        //只允许为最里面子节点课程计划选视频
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        TeachplanMedia one = null;
        Optional<TeachplanMedia> optionalTeachplanMedia = teachplanMediaRepository.findById(teachplanId);
        if (optionalTeachplanMedia.isPresent()){
            one= optionalTeachplanMedia.get();
        }else {
            one = new TeachplanMedia();
        }
        //将对象保存到数据库
        one.setCourseId(teachplan.getCourseid());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒体文件url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
