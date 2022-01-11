package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-11-15 15:05
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    RestClient restClient;

    @Test
    //创建索引库
    public void testCreateIndex() throws IOException{
        //创建索引请求对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
        //设置参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards","1").put("number_of_replicas","0"));
        //指定映射
        createIndexRequest.mapping("doc","{ \"properties\": {\n" +
                "     \"description\": { \n" +
                "         \"type\": \"text\", \n" +
                "         \"analyzer\": \"ik_max_word\",\n" +
                "          \"search_analyzer\": \"ik_smart\"\n" +
                "           },\n" +
                "           \"name\": { \n" +
                "               \"type\": \"text\", \n" +
                "               \"analyzer\": \"ik_max_word\", \n" +
                "               \"search_analyzer\": \"ik_smart\" \n" +
                "               },\n" +
                "            \"pic\":{ \n" +
                "                \"type\":\"text\",\n" +
                "                \"index\":false \n" +
                "                    }, \n" +
                "            \"price\":{\n" +
                "                \"type\": \"float\" \n" +
                "                    },\n" +
                "            \"studymodel\": {\n" +
                "                \"type\": \"keyword\" },\n" +
                "            \"timestamp\": {\n" +
                "                 \"type\": \"date\",\n" +
                "                  \"format\": \"yyyy‐MM‐dd HH:mm:ss||yyyy‐MM‐dd||epoch_millis\"\n" +
                "                   } } }", XContentType.JSON);
        //操作索引库客户端
        IndicesClient indices = restHighLevelClient.indices();
        //执行创建索引
        CreateIndexResponse response = indices.create(createIndexRequest);
        //得到响应
        boolean acknowledged = response.isAcknowledged();
        System.out.println(acknowledged);

    }

    @Test
    //删除索引库
    public void testDeleteIndex() throws IOException{
        //删除索引请求对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
        //操作索引的客户端
        IndicesClient indices = restHighLevelClient.indices();
        //删除索引库
        DeleteIndexResponse response = indices.delete(deleteIndexRequest);
        //得到响应
        boolean acknowledged = response.isAcknowledged();
        System.out.println(acknowledged);
    }

    @Test
    //添加文档
    public void testAddDoc() throws IOException{
        //准备json字串
         Map<String, Object> jsonMap = new HashMap<>();
         jsonMap.put("name", "spring cloud实战");
         jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
         jsonMap.put("studymodel", "201001");
         SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
         jsonMap.put("timestamp", dateFormat.format(new Date()));
         jsonMap.put("price", 5.6f);
         //索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
        //文档内容
        indexRequest.source(jsonMap);
        //响应对象
        IndexResponse index = restHighLevelClient.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = index.getResult();
        System.out.println(result);

    }

    //查询文档
    @Test
    public void testGetDoc() throws IOException{
        GetRequest getRequest = new GetRequest("xc_course","doc","o0buynUBp0sNYw34zwPd");
        GetResponse documentFields = restHighLevelClient.get(getRequest);
        //得到文档内容
        Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    //更新文档
    @Test
    public void testUpdateDoc() throws  IOException{
        //删除文档id
        String id = "o0buynUBp0sNYw34zwPd";
        UpdateRequest updateRequest = new UpdateRequest("xc_course","doc",id);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("name","spring cloud alibab");
        updateRequest.doc(map);
        //响应对象
        UpdateResponse updateResponsep = restHighLevelClient.update(updateRequest);
        RestStatus status = updateResponsep.status();
        System.out.println(status);


    }

}
