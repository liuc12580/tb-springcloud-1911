package com.jk.controller;

import com.jk.mapper.UserMapper;
import com.jk.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RefreshScope
@Controller
public class UserController {

    @Value("${my.name}")
    String name;

    @Value("${my.gender}")
    String gender;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private UserMapper userMapper;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello(){

        Object obj = redisUtil.get("aaa");
        System.out.println(obj);

        List<Map<String, Object>> userList = userMapper.selectUserList();

        return  "你好，" + name + ", 性别：" + gender + userList.toString();
    }
}
