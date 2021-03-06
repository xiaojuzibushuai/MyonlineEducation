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

    @Value("${course???publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course???publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course???publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course???publish.siteId}")
    private String publish_siteId;
    @Value("${course???publish.templateId}")
    private String publish_templateId;
    @Value("${course???publish.previewUrl}")
    private String previewUrl;

    //??????????????????
    public TeachplanNode findTeachplanList(String courseId){
        return  teachplanMapper.selectList(courseId);
    }

    //??????????????????
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //????????????
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            //???????????????????????????
             parentid = getTeachplanList(courseid);
        }
        //???????????????
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        String parentNodeGrade = parentNode.getGrade();

        Teachplan teachplanNew = new Teachplan();
        //??????????????????teachplan???????????????????????????
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        //?????????????????????????????????
        if (parentNodeGrade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
          teachplanRepository.save(teachplanNew);
         return  new ResponseResult(CommonCode.SUCCESS);
    }

    //??????????????????????????????????????????????????????
    private String getTeachplanList(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            return null;
        }
        //????????????
        CourseBase courseBase = optional.get();
        //?????????????????????
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList ==null || teachplanList.size()<=0){
            //???????????? ?????????????????????
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //???????????????id
        return  teachplanList.get(0).getId();
    }

    /**
     * ??????????????????
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId,int page,int size,CourseListRequest courseListRequest){
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //?????????id????????????dao
        courseListRequest.setCompanyId(companyId);

        if (page<=0){
            page=0;
        }
        if (size<=0){
            size=10;
        }

        //??????????????????
        PageHelper.startPage(page,size);
        //????????????
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //????????????
        List<CourseInfo> result = courseListPage.getResult();
        //????????????
        long total = courseListPage.getTotal();
        //???????????????
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setList(result);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS,courseInfoQueryResult);
    }

    /**
     * ??????????????????
     * @param courseBase
     * @return
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        //??????????????????????????????
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return  new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    /**
     * ????????????????????????
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
     * ????????????????????????
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
        //??????????????????
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
     *????????????????????????
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
     * ????????????????????????
     * @param id
     * @param courseMarket
     * @return
     */
    @Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket market = this.getCourseMarketById(id);
        if (market==null){
           //????????????????????????
            market = new CourseMarket();
            BeanUtils.copyProperties(courseMarket,market);
            //????????????id
            market.setId(id);
            courseMarketRepository.save(market);
        }else {
            market.setCharge(courseMarket.getCharge());
            market.setStartTime(courseMarket.getStartTime());
            //??????????????????????????????
            market.setEndTime(courseMarket.getEndTime());
            //??????????????????????????????
            market.setPrice(courseMarket.getPrice());
            market.setQq(courseMarket.getQq());
            market.setValid(courseMarket.getValid());
            courseMarketRepository.save(market);
        }
        return market;
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
        //??????????????????
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
     * ??????????????????
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
     * ??????????????????
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

    //?????????????????? ???????????????????????????????????????????????????
    public CourseView findCourseView(String courseId) {
        CourseView courseView = new CourseView();
        //????????????
        CourseBase courseBase = this.getCourseBaseById(courseId);
        courseView.setCourseBase(courseBase);
        //????????????
        CourseMarket courseMarketById = this.getCourseMarketById(courseId);
        courseView.setCourseMarket(courseMarketById);
        //????????????
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        //????????????
        CoursePic coursePic = this.findCoursePic(courseId);
        courseView.setCoursePic(coursePic);
        return courseView;
    }


    /**
     * ??????????????????
     * @param id
     * @return
     */
    public CoursePublishResult preview(String id) {
       //??????cms ????????????
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");

        CourseBase courseBase = this.getCourseBaseById(id);
        cmsPage.setPageAliase(courseBase.getName());

        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);//??????id
        //????????????cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            //????????????
               return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //?????????????????????url
        String pageId = cmsPageResult.getCmsPage().getPageId();
        String url=previewUrl+pageId;
        //??????CoursePublishResult??????
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    //????????????
    @Transactional
    public CoursePublishResult publish(String id) {
        //????????????
        CourseBase courseBase = this.getCourseBaseById(id);
        //??????????????????
        CmsPage cmsPage  = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre+id);
        cmsPage.setPageName(id+".html");
        cmsPage.setPageAliase(courseBase.getName());
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);//??????id
        //??????cms??????????????????
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //???????????????????????????
        CourseBase courseBase1 = this.saveCoursePubState(id);

        if (courseBase1 == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //????????????????????????
         //??????coursePub??????
        CoursePub coursePub = createCoursePub(id);
         //??????coursePub??????????????????
        saveCoursePub(id,coursePub);
        //??????????????????

        //????????????url
        String pageUrl = cmsPostPageResult.getPageUrl();

        //???teachplanMediaPub???????????????????????????
        this.saveTeachplanMediaPub(id);

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    //???teachplanMediaPub???????????????????????????
    private void saveTeachplanMediaPub(String courseId){
          //?????????teachplanMediaPub????????????
         teachplanMediaPubRepository.deleteByCourseId(courseId);
          //???teachplanMedia??????????????????
         List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
          //???teachplanMediaList?????????teachplanMediaPub???
         List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
         for (TeachplanMedia t:teachplanMediaList){
             TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
             BeanUtils.copyProperties(t,teachplanMediaPub);
             //???????????????
             teachplanMediaPub.setTimestamp(new Date());
             teachplanMediaPubs.add(teachplanMediaPub);
         }
         teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }



    //???coursePub????????????????????????
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
       //????????????id??????coursepub
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        CoursePub coursePubNew = null;
        if (coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }
        //???coursePub???????????????????????????coursePubNew???
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //???????????????
        coursePubNew.setTimestamp(new Date());
        //????????????
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);

        //????????????
        coursePubRepository.save(coursePubNew);
        return  coursePubNew;
    }

    //??????coursePub??????
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //????????????id??????course_base
        Optional<CourseBase> Baseoptional = courseBaseRepository.findById(id);
        if (Baseoptional.isPresent()){
            CourseBase courseBase = Baseoptional.get();
            //???coursebase???????????????CoursePub???
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //??????????????????
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        //??????????????????
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }
        //??????????????????????????????json?????????
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return  coursePub;
    }



    //?????????????????? ????????? 202002
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBaseById = this.getCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    /**
     * ????????????????????????????????????????????????
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia ==null ||StringUtils.isEmpty(teachplanMedia.getMediaId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //???????????????????????????3???
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optionalTeachplan = teachplanRepository.findById(teachplanId);
        if (!optionalTeachplan.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optionalTeachplan.get();
        //???????????????????????????????????????????????????
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
        //???????????????????????????
        one.setCourseId(teachplan.getCourseid());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());//????????????url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
