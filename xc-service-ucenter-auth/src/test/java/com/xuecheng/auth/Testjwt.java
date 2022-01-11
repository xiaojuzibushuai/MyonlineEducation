package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-14 09:38
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class Testjwt {

    //创建jwt令牌
    @Test
    public void testCreateJwt(){
        //秘钥库文件
        String keystore= "xc.keystore";
        //秘钥库密码
        String keystore_password  = "xuechengkeystore";

        //秘钥别名
        String alias = "xckey";
        //秘钥的访问密码
        String key_password= "xuecheng";
        //秘钥库文件路径
        ClassPathResource classPathResource = new ClassPathResource(keystore);
        //秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource,keystore_password.toCharArray());
        //密钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        //获取私钥
        RSAPrivateKey aPrivate =(RSAPrivateKey) keyPair.getPrivate();
        //jwt令牌内容
        HashMap<String, String> body = new HashMap<>();
        body.put("name","xiaojuzi");
        String jsonString = JSON.toJSONString(body);
        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(jsonString, new RsaSigner(aPrivate));
        //生成jwt令牌编码
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    //校验jwt令牌
    @Test
    public void testVertify(){
           //公钥
         String publicKey="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl8iY4I/fduMvQEvn0eBuh0VGPBB/nwL1ODXoGyc/54tp5adyLuQ3qLdevwNfh8CSdb7Si96VgJ2JFQXW49i1NS+GmywcrDPCurxN1IdgEaNpUg1FcDXOpH/lhcNzkikYZgIMa5sq0j27A8yBwPKm/BYlMmBLeC4q35DtnMwYoquzvxe+II+wbDF+rfUh0Dbz7W1nLDqJh+FF/ZVPFenKmAIUBg8fASKu2Z8Z8XvMRZsKRa33GHZllNufTOUQwMoye5cTH9ie5v09SwxEVl9JzQUGtRUsBTSP9eSIDsDw84edTaKbGWjKe2hF3JQYsO3JiqyKmBx8dr/hr1Cy925rTwIDAQAB-----END PUBLIC KEY-----";
         //jwt令牌
        String jwt="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoieGlhb2p1emkifQ.H9GqCvqT4zZ2rrQjtaE6uV555U7YBNgS9v5Glk2BXsgOsbZO1Om9qJFJB5uAhW7wywe6yslmgysdyeMooQdOQwNnGLYAflEE5cBamDSDjizvMr2LJ1ZjHjEw3BwK_VtL8i8-FiusmZH01Vv1AtGkJ_M0FZfnQ3hL_7BHlblOxE1-R3yT197kNWV_IaoHjSY2WEYjf5ke4JKq1CcOa5WS0A19v33vylfAfc-TuqElACZJW-No9G9wgTKBgt4Q4hcK03eXJbNV6qXu2qyhnq3SCPlyVnC7CaVlEcbq1DTG8QMZgZfb6SQdziaHsQ8E_mUsrztZtydHXNj3nG7mQW-n9w";
        //校验jwt令牌
        Jwt jwt1 = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publicKey));
        String body = jwt1.getClaims();
        System.out.println(body);
    }



}
