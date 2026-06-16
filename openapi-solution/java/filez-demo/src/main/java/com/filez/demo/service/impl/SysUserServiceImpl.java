package com.filez.demo.service.impl;

import com.filez.demo.dao.SysUserMapper;
import com.filez.demo.entity.SysUserEntity;
import com.filez.demo.service.SysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 系统用户信息表 服务实现类
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
	 * 新增用户
	 * @param user 用户信息
	 */
	@Override
	public SysUserEntity addUser(SysUserEntity user) {
		return sysUserMapper.insert(user) > 0 ? user : null;
	}

	/**
	 * 根据用户名密码，查询用户信息
	 * @param username 用户名
	 * @param password 密码
	 */
	@Override
	public SysUserEntity getUserByNameAndPwd(String username, String password) {
		return sysUserMapper.selectOneByNameAndPassword(username, password);
	}

	/**
	 * 查询用户信息
	 */
	@Override
	public List<SysUserEntity> getAllUser() {
		return sysUserMapper.selectList(null);
	}

	/**
	 * 根据id查询用户信息
	 * @param id 用户id
	 */
	@Override
	public SysUserEntity getUserById(String id) {
		return sysUserMapper.selectById(id);
	}

	/**
	 * 根据ID更新用户信息
	 * @param user 用户信息
	 */
	@Override
	public SysUserEntity updateUserById(SysUserEntity user) {
		return sysUserMapper.updateById(user) > 0 ? user : null;
	}
}
