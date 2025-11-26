package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * System user information table Mapper interface
 * </p>
 *
 * @author qiugq
 * @since 2025-05-21
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

	/**
	 * Query user information by username and password
	 * @param username Username
	 * @param password Password
	 */
	SysUserEntity selectOneByNameAndPassword(@Param("username") String username, @Param("password") String password);
}
