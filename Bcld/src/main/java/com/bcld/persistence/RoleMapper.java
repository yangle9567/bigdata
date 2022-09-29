package com.bcld.persistence;

import com.bcld.domain.Role;
import com.bcld.domain.example.RoleExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;

public interface RoleMapper {
    int countByExample(RoleExample example);

    int deleteByExample(RoleExample example);

    int insert(Role record);

    int insertSelective(Role record);

    List<Role> selectByExample(RoleExample example);

    int updateByExampleSelective(@Param("record") Role record, @Param("example") RoleExample example);

    int updateByExample(@Param("record") Role record, @Param("example") RoleExample example);

    Logger selectByPrimaryKey(String roleId);
}