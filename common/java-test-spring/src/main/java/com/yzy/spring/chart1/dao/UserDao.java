package com.yzy.spring.chart1.dao;

import com.yzy.spring.chart1.pojo.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserDao {
    List<User> queryUserList(Map<String, Object> param) ;
}
