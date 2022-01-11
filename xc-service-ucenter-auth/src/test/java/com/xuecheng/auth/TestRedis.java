package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-14 10:18
 **/

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //创建一个jwt令牌
    @Test
    public void testRedis(){
        //定义一个key
        String key = "user_token:1f37d4e9-72cc-44f0-9e04-10b1941174c6";
        //定义一个value
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("jwt","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6InhpYW9qdXppIiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNjA3OTU2MTI5LCJqdGkiOiIxZjM3ZDRlOS03MmNjLTQ0ZjAtOWUwNC0xMGIxOTQxMTc0YzYiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.I3jyvLgPI_0dslzk014XUkDz5NjAOGjkHlpD8E2D4T6qiZ5I8rmRp_VFQlAvy7vnv6JCwgzNGbPpOmkJ4oqiNu_I26zfEW2NQmEDtFrWi1e0S4d6D3q_aioPlmsyl8b7oPj3X5aab6LoaGd3ML413icjxtdrI_Ic8NzM9ZPAB-XxVTKQD0SNC1i21O1k79veY97gJ-xwhXPCUG3d_gx7RnF7trVsPLufFLywfn7ghQzJWQtGIzj3Z3lIEbLZY8PVBikDVkbHaeDb_U_4LiZlwacHcBPNzxwb_UJftSK41e-W5KmaC9u1v5cKKb007ZX-WV3qHWp51qjsLUl20mld8g");
        hashMap.put("refresh_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6InhpYW9qdXppIiwic2NvcGUiOlsiYXBwIl0sImF0aSI6IjFmMzdkNGU5LTcyY2MtNDRmMC05ZTA0LTEwYjE5NDExNzRjNiIsIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNjA3OTU2MTI5LCJqdGkiOiIxMzJiZGIzNS1kYjU5LTQ3Y2UtOGE3Zi1iYzdlNzJhNjJiNWQiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.hC67pTtg_nY0oDN1maNPGH-0ZBDjNI4tjS4iazCuAgiFIj8oU8lNlpaysUAjvrqfQUbUnWKxI00nYHI2EhEp7YKe6MdPizboy-1tQWZ7cDw5NkmzWB41e6h_LI1c2gjLHJ83hu7QO98eUBfwUhj7QnpOljksa7mpJSO3cg9qmMMTJisaPYaYQTfsp47slqAiS2nH5gi3cmJYfzgR9U3y31E-T0t1mKnhkEKb6bvDF_kJGEBDhcrt4pZZ9J_ppdoMMC7mNBMEO1zTUsdsB0Hckj3XjnCVvJow_66wSqluZbO6OhfA-2o1drU_F9E-URKkjkZtUQmhD52qd-MlytE5oA");
        String jsonString = JSON.toJSONString(hashMap);
        //校验key是否存在
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        System.out.println(expire);
        //存储数据
        stringRedisTemplate.boundValueOps(key).set(jsonString,30, TimeUnit.SECONDS);
        //获取数据
        String content = stringRedisTemplate.opsForValue().get(key);
        System.out.println(content);
    }
}
