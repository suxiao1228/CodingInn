package com.xiongsu.service.user.service.help;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class UserPwdEncoder {

    /**
     * 密码加盐，推荐做法是每个用户都使用独立的盐，提高安全性
     */
    @Value("${security.salt}")
    private String salt;//@Value("${security.salt}") 读取 配置文件 里的 security.salt 值，这个值是盐（Salt）。
    //这个盐是所有用户共用的，不是随机的。

    @Value("3")
    private Integer saltIndex;//表示盐将会被插入到密码的第 3 个字符位置（索引从 0 开始）。

    public boolean match(String plainPwd, String encPwd) {
        return Objects.equals(encPwd(plainPwd), encPwd);
    }

    /**
     * 明文密码处理
     */
    public String encPwd(String plainPwd) {
        if (plainPwd.length() > saltIndex) {
            plainPwd = plainPwd.substring(0, saltIndex) + salt + plainPwd.substring(saltIndex);
        } else {
            plainPwd = plainPwd + salt;
        }
        return DigestUtils.md5DigestAsHex(plainPwd.getBytes(StandardCharsets.UTF_8));
    }
}
