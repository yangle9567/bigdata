package com.bcld.service.impl;

import java.util.Date;


import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcld.domain.Right;
import com.bcld.domain.Role;
import com.bcld.domain.User;
import com.bcld.domain.UserRole;
import com.bcld.domain.example.RoleExample;
import com.bcld.domain.example.UserExample;
import com.bcld.domain.example.UserRoleExample;
import com.bcld.persistence.RoleMapper;
import com.bcld.persistence.UserMapper;
import com.bcld.persistence.UserRoleMapper;
import com.bcld.service.UserService;
import com.bcld.utils.MyBatisUtils;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleMapper roleMapper;
   

    @Override
    public User validateUser(UsernamePasswordToken usernamePasswordToken) {

        String ip = usernamePasswordToken.getHost();

        UserExample userExample = new UserExample();
        userExample.createCriteria().andUserNameEqualTo(usernamePasswordToken.getUsername());
        List<User> userList = userMapper.selectByExample(userExample);
        if (userList.size() <= 0) {
            return null;
        }

        User user = userList.get(0);
        return user;

    }

    @Override
    public List<User> selectByExample(User user) {
        UserExample userExample=new UserExample();
        return userMapper.selectByExample(userExample);
    }

    @Override
    public List<Role> findUserRole(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Right> findUserRight(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String findRoleNameByUserId(String id) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void deleteByExample(UserExample example){
        UserExample userExample=new UserExample();
        userMapper.deleteByExample(userExample);
    }

}
