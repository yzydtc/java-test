package com.yzy.spring.chart1.service;

import com.yzy.spring.chart1.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> queryUsers(Map<String, Object> param);
}
