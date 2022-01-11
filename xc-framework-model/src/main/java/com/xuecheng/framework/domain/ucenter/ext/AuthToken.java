package com.xuecheng.framework.domain.ucenter.ext;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by xiaojuzi on 2010/12/5.
 */
@Data
@ToString
@NoArgsConstructor
public class AuthToken {
    String access_token;//访问token 身份令牌
    String refresh_token;//刷新token
    String jwt_token;//jwt令牌
}
