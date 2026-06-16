package com.filez.demo.service;

import com.filez.demo.entity.SysUserEntity;

import java.util.List;

/**
 * <p>
 * 系统用户信息表 服务类
 * </p>
 *
 * @author qiugq
 * @since 2025-05-21
 */
public interface SysUserService {

	/**
	 * 新增用户
	 * @param user 用户信息
	 */
	SysUserEntity addUser(SysUserEntity user);

	/**
	 * 根据用户名密码，查询用户信息
	 * @param username 用户名
	 * @param password 密码
	 */
	SysUserEntity getUserByNameAndPwd(String username, String password);

	/**
	 * 查询用户信息
	 */
	List<SysUserEntity> getAllUser();

	/**
	 * 根据id查询用户信息
	 * @param id 用户id
	 */
	SysUserEntity getUserById(String id);

	/**
	 * 根据ID更新用户信息
	 * @param user 用户信息
	 */
	SysUserEntity updateUserById(SysUserEntity user);
}
