package com.filez.demo.service.impl;

import com.filez.demo.dao.SysUserMapper;
import com.filez.demo.entity.SysUserEntity;
import com.filez.demo.service.SysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * System user information table service implementation class
 * </p>
 *
 * @author qiugq
 * @since 2025-05-21
 */
@Service
public class SysUserServiceImpl implements SysUserService {

	@Resource
	private SysUserMapper sysUserMapper;

	/**
	 * Add new user
	 * @param user User information
	 */
	@Override
	public SysUserEntity addUser(SysUserEntity user) {
		return sysUserMapper.insert(user) > 0 ? user : null;
	}

	/**
	 * Query user information by username and password
	 * @param username Username
	 * @param password Password
	 */
	@Override
	public SysUserEntity getUserByNameAndPwd(String username, String password) {
		return sysUserMapper.selectOneByNameAndPassword(username, password);
	}

	/**
	 * Query user information
	 */
	@Override
	public List<SysUserEntity> getAllUser() {
		return sysUserMapper.selectList(null);
	}

	/**
	 * Query user information by id
	 * @param id User id
	 */
	@Override
	public SysUserEntity getUserById(String id) {
		return sysUserMapper.selectById(id);
	}

	/**
	 * Update user information by ID
	 * @param user User information
	 */
	@Override
	public SysUserEntity updateUserById(SysUserEntity user) {
		return sysUserMapper.updateById(user) > 0 ? user : null;
	}
}
