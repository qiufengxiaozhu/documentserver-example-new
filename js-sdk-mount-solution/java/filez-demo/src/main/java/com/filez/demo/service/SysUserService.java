package com.filez.demo.service;

import com.filez.demo.entity.SysUserEntity;

import java.util.List;

/**
 * <p>
 * System user information table service class
 * </p>
 *
 * @author qiugq
 * @since 2025-05-21
 */
public interface SysUserService {

	/**
	 * Add new user
	 * @param user User information
	 */
	SysUserEntity addUser(SysUserEntity user);

	/**
	 * Query user information by username and password
	 * @param username Username
	 * @param password Password
	 */
	SysUserEntity getUserByNameAndPwd(String username, String password);

	/**
	 * Query user information
	 */
	List<SysUserEntity> getAllUser();

	/**
	 * Query user information by id
	 * @param id User id
	 */
	SysUserEntity getUserById(String id);

	/**
	 * Update user information by ID
	 * @param user User information
	 */
	SysUserEntity updateUserById(SysUserEntity user);
}
