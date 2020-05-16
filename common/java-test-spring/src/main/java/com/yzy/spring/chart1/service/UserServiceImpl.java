package com.yzy.spring.chart1.service;

import com.yzy.spring.chart1.dao.UserDao;
import com.yzy.spring.chart1.pojo.User;

import java.util.List;
import java.util.Map;

public class UserServiceImpl implements UserService {
    // 依赖注入UserDao
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<User> queryUsers(Map<String, Object> param) {
        return userDao.queryUserList(param);
    }
}
