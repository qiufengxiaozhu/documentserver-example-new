package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统用户信息表 Mapper 接口
 * </p>
 *
 * @author qiugq
 * @since 2025-05-21
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

	/**
	 * 根据用户名密码，查询用户信息
	 * @param username 用户名
	 * @param password 密码
	 */
	SysUserEntity selectOneByNameAndPassword(@Param("username") String username, @Param("password") String password);
}
