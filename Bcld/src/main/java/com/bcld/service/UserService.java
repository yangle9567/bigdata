package com.bcld.service;

import java.util.List;

import org.apache.shiro.authc.UsernamePasswordToken;

import com.bcld.domain.Right;
import com.bcld.domain.Role;
import com.bcld.domain.User;
import com.bcld.domain.example.UserExample;

public interface UserService {

    /**
     * 验证用户有效性
     * @param usernamePasswordToken
     * @return
     */
    public User validateUser(UsernamePasswordToken usernamePasswordToken);

    /**
     * 根据用户查询用户的所有角色码
     * 
     * @param user
     * @return
     */
    public List<Role> findUserRole(User user);

    /**
     * 根据用户查询用户的所有权限码
     * 
     * @param user
     * @return
     */
    public List<Right> findUserRight(User user);
    
   
    public List<User> selectByExample(UserExample userExample);
    
    //以下为自定义方法
    String findRoleNameByUserId(String id);

    void deleteByExample(UserExample example);
}
