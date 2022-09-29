package com.bcld.service.impl;

import java.util.List;



import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcld.domain.Right;
import com.bcld.domain.Role;
import com.bcld.domain.User;
import com.bcld.service.UserService;

@Service
@Transactional
public class AuthorizingRealmImpl extends AuthorizingRealm {

    private static Logger log = LoggerFactory.getLogger(AuthorizingRealmImpl.class);

    @Autowired
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // TODO Auto-generated method stub
        return null;
    }

    /*@Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        User user = (User) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        List<Role> userRoles = userService.findUserRole(user);
        for (Role role : userRoles) {
            info.addRole(role.getCode());
        }
        List<Right> userRights = userService.findUserRight(user);
        for (Right right : userRights) {
            info.addStringPermission(right.getCode());
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        setCredentialsMatcher(new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME));
        User user = userService.validateUser((UsernamePasswordToken) token);
        if (user != null) {
            return new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes("unis_" + user.getId() + "_salt"), getName());
        } else {
            return null;
        }
    }
*/
}
