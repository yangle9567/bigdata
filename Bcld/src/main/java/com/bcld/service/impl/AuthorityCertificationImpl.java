package com.bcld.service.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcld.persistence.UserRoleMapper;
import com.bcld.service.AuthorityCertificationService;


@Service
@Transactional
public class AuthorityCertificationImpl implements AuthorityCertificationService{

    private static Logger log = LoggerFactory.getLogger(AuthorityCertificationImpl.class);

    @Autowired
    private UserRoleMapper userRoleMapper;
    /*@Autowired
    private RoleRightMapper roleRightMapper;
    @Autowired
    private RightMapper rightMapper;*/
    
    @Override
    public boolean authority(String module, String userId) {
        boolean condition = false;
        
        return false;
    }

    /*public boolean authority(String module, String userId) {
        boolean condition = false;
        if (StringUtils.isNotBlank(userId)) {
            *//**
             * 根据当前用户id获取角色id
             *//*
            UserRoleExample userRoleExample = new UserRoleExample();
            UserRoleExample.Criteria userRoleCriteria = userRoleExample.createCriteria();
            userRoleCriteria.andUserIdEqualTo(userId);
            List<UserRole> userRoleList = userRoleMapper.selectByExample(userRoleExample);
            String userRoleId = userRoleList.get(0).getRoleId();
            if (StringUtils.isNotBlank(userRoleId)) {
                *//**
                 * 根据角色id获取权限id列表
                 *//*
                RoleRightExample roleRightExample = new RoleRightExample();
                RoleRightExample.Criteria roleRightCriteria = roleRightExample.createCriteria();
                roleRightCriteria.andRoleIdEqualTo(userRoleId);
                List<RoleRight> rightIdList = roleRightMapper.selectByExample(roleRightExample);
                if(rightIdList.size() > 0){
                    List<String> rightList = (List<String>) CollectionUtils.collect(rightIdList, new BeanToPropertyValueTransformer("rightId"));                   
                    //根据模块名称查找id
                    if(StringUtils.isNotBlank(module)){
                        RightExample rightExample = new RightExample();
                        RightExample.Criteria rightCriteria = rightExample.createCriteria();
                        rightCriteria.andNameEqualTo(module);
                        List<Right> rights = rightMapper.selectByExample(rightExample);
                        if(rights.size()>0){
                            String rightId = rights.get(0).getId();
                            if(rightList.contains(rightId)){
                                condition = true;
                            }else{
                                condition = false;
                            }
                        }
                    }      
                }
                           
            }
        }
        return condition;

    }*/

    @Override
    public List<String> reportType() throws ConfigurationException {
        Configuration config = new PropertiesConfiguration("configuration.properties");
        String reportKeyword = config.getString("report.keyword");
        String reportVulnerability = config.getString("report.vulnerability");
        String reportTrojan = config.getString("report.trojan");
        String reportIllegalLink = config.getString("report.illegalLink");
        String reportTamper = config.getString("report.tamper");
        String reportLogAnalysis = config.getString("report.logAnalysis");
        String reportUsability = config.getString("report.usability");
        ArrayList ListType = new ArrayList();
        Map map = new HashMap();
        if(reportVulnerability.equals("true")){
            map = new HashMap();
            map.put("name", "vulnerability");
            map.put("value", "漏洞报表"); 
            ListType.add(map);
        }
        if(reportKeyword.equals("true")){
            map = new HashMap();
            map.put("name", "keyword");
            map.put("value", "关键字报表");  
            ListType.add(map);
        }
        if(reportTrojan.equals("true")){
            map = new HashMap();
            map.put("name", "trojan");
            map.put("value", "挂马报表");
            ListType.add(map);
        }
        if(reportIllegalLink.equals("true")){
            map = new HashMap();
            map.put("name", "illegalLink");
            map.put("value", "暗链报表");
            ListType.add(map);
        }
        if(reportTamper.equals("true")){
            map = new HashMap();
            map.put("name", "tamper");
            map.put("value", "篡改报表");
            ListType.add(map);
        }
        if(reportLogAnalysis.equals("true")){
            map = new HashMap();
            map.put("name", "logAnalysis");
            map.put("value", "日志分析报表");
            ListType.add(map);
        }
        
        if(reportUsability.equals("true")){
            map = new HashMap();
            map.put("name", "usability");
            map.put("value", "可用性报表");
            ListType.add(map);
        }

        return ListType;
    }

   

}