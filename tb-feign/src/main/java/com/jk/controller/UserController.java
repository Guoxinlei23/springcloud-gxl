package com.jk.controller;

import com.jk.entity.UserEntity;
import com.jk.service.UserServiceFeign;
import com.jk.utils.Constant;
import com.jk.utils.RedisUtil;
import com.jk.utils.StringUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String sendValue  = redisUtil.get(Constant.SAVE_ORDER_WARNING_KEY).toString();

        String ipAddr = request.getRemoteAddr();

        //新启动一个线程进行业务逻辑处理
        // 开启一个独立线程，进行发送警报，给开发人员，处理问题
        new Thread( ()->{
            if(StringUtil.isNotEmpty(sendValue)) {
                System.out.println("紧急短信，用户下单失败，请离开查找原因,ip地址是="+ipAddr);





                redisUtil.set(Constant.SAVE_ORDER_WARNING_KEY, "用户保存订单失败", 60);
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
    public String hello(String name) {
        return userService.hello(name);
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


    public static void sendPost(HttpEntity reqEntity) {
        //luosimao短信平台短信发送接口URL
        HttpPost post = new HttpPost("http://sms-api.luosimao.com/v1/send.json");

        //“d609b769db914a4d959bae3414ed1f7X” --APIkey，在luosimao.com注册登陆以后可以得到
        post.setHeader("Authorization",	"Basic " + Base64.encodeBase64String("api:key-d609b769db914a4d959bae3414ed1f7X".getBytes()));
        post.setEntity(reqEntity);
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(post);
            HttpEntity respEntity = response.getEntity();
            //status code,如200
            int statusCode= response.getStatusLine().getStatusCode();
            //result,如{"error":0,"msg":"ok"}
            String respString = EntityUtils.toString(respEntity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }
    }




}
