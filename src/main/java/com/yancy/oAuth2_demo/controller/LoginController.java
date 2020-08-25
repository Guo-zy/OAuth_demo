package com.yancy.oAuth2_demo.controller;


import com.alibaba.fastjson.JSONObject;
import com.yancy.oAuth2_demo.entity.Oauth2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
@Slf4j
     public class LoginController {

    @Autowired
    private Oauth2 oauth2;


    @GetMapping("/authorize")
    public String authorize() {
        String url = oauth2.getAuthorizeUrl() +
                "?client_id=" + oauth2.getClientId() +
                "&redirect_uri=" + oauth2.getRedirectUrl();
        log.info("url" , url);
        return "redirect:" + url;

    }



    @GetMapping("/oauth/redirect")
    public String callback(@RequestParam("code") String code) {
        // code换token
        String accessToken = getAccessToken(code);
        // token换userInfo
        String userInfo = getUserInfo(accessToken);
        return "redirect:/home";
    }

    @GetMapping("/home")
    @ResponseBody
    public String home() {
        return "hello world";
    }


    private String getAccessToken(String code) {
        String url = oauth2.getAccessTokenUrl() +
                "?client_id=" + oauth2.getClientId() +
                "&client_secret=" + oauth2.getClientSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";
        // 构建请求头
        HttpHeaders requestHeaders = new HttpHeaders();
        // 指定响应返回json格式
        requestHeaders.add("accept", "application/json");
        // 构建请求实体
        HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
        RestTemplate restTemplate = new RestTemplate();
        // post 请求方式
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        String responseStr = response.getBody();

        // 解析响应json字符串
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        String accessToken = jsonObject.getString("access_token");
        return accessToken;
    }

    private String getUserInfo(String accessToken) {
        String url = oauth2.getUserInfoUrl();
        // 构建请求头
        HttpHeaders requestHeaders = new HttpHeaders();
        // 指定响应返回json格式
        requestHeaders.add("accept", "application/json");
        // AccessToken放在请求头中
        requestHeaders.add("Authorization", "token " + accessToken);
        // 构建请求实体
        HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
        RestTemplate restTemplate = new RestTemplate();
        // get请求方式
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String userInfo = response.getBody();
        return userInfo;
    }
}
