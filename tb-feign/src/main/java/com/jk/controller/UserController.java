package com.jk.controller;

import com.jk.entity.UserEntity;
import com.jk.service.UserServiceFeign;
import com.jk.utils.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.jk.utils.SmsConstant.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserServiceFeign userService;

    @Resource
    private RedisUtil redisUtil;


    @RequestMapping("/saveOrder")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "saveOrderFail")
    public Object saveOrder(Integer userId, Integer productId, HttpServletRequest request) {
        return userService.saveOrder(userId, productId);
    }

    //注意，方法签名一定要要和api方法一致 自定义降级方法
    public Object saveOrderFail(Integer userId, Integer productId, HttpServletRequest request) {

        System.out.println("controller 保存订单降级方法");

        String sendValue  = (String) redisUtil.get(Constant.SAVE_ORDER_WARNING_KEY);

        String ipAddr = request.getRemoteAddr();

        //新启动一个线程进行业务逻辑处理
        // 开启一个独立线程，进行发送警报，给开发人员，处理问题
        new Thread( ()->{
            if(StringUtil.isEmpty(sendValue)) {
                System.out.println("紧急短信，用户下单失败，请离开查找原因,ip地址是="+ipAddr);

                //发送一个http请求，调用短信服务 TODO
                // 写发送短信代码，带有参数发送 userId  productId
                try {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(SmsConstant.SERVER_URL);
                    String curTime = String.valueOf((new Date()).getTime() / 1000L);
                    /*
                     * 参考计算CheckSum的java代码，在上述文档的参数列表中，有CheckSum的计算文档示例
                     */
                    String checkSum = CheckSumBuilder.getCheckSum(SmsConstant.APP_SECRET, SmsConstant.NONCE, curTime);

                    // 设置请求的header
                    httpPost.addHeader("AppKey", SmsConstant.APP_KEY);
                    httpPost.addHeader("Nonce", SmsConstant.NONCE);
                    httpPost.addHeader("CurTime", curTime);
                    httpPost.addHeader("CheckSum", checkSum);
                    httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

                    // 设置请求的的参数，requestBody参数
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    /*
                     * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档”
                     * 2.参数格式是jsonArray的格式，例如 "['13888888888','13666666666']"
                     * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
                     */
                    nvps.add(new BasicNameValuePair("templateid", SmsConstant.TEMPLATEID));
                    nvps.add(new BasicNameValuePair("mobile", SmsConstant.MOBILE));
                    nvps.add(new BasicNameValuePair("codeLen", SmsConstant.CODELEN));

                    httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

                    // 执行请求
                    HttpResponse response = httpClient.execute(httpPost);
                    /*
                     * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500
                     * 2.具体的code有问题的可以参考官网的Code状态表
                     */
                    System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));

                    redisUtil.set(Constant.SAVE_ORDER_WARNING_KEY, "用户保存订单失败", 60);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                System.out.println("已经发送过短信，1分钟内不重复发送");
            }
        }).start();

        // 反馈给用户看的
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", -1);
        map.put("message", "抢购排队人数过多，请您稍后重试。");

        return map;
    }


    @RequestMapping("/hello")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "helloFail")
    public String hello(String name) {
        return userService.hello(name);
    }

    private String helloFail(String name){
        return "请求失败";
    }

    @RequestMapping("/selectUserList")
    @ResponseBody
    public List<UserEntity> selectUserList() {

        List<UserEntity> userList = (List<UserEntity>) redisUtil.get(Constant.SELECT_USER_LIST);

        // 1. 有值   2. 没有值
        if(userList == null || userList.size() <= 0 || userList.isEmpty()) {
            // 从数据库查询，存redis
            userList = userService.findUserList();
            redisUtil.set(Constant.SELECT_USER_LIST, userList, 30);
        }

       return userList;

    }

}
