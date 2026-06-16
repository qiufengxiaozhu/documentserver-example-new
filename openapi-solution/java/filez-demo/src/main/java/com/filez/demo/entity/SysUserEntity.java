package com.filez.demo.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 系统用户信息表
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUserEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID，自增主键
	 */
	@TableId(value = "id", type = IdType.INPUT)
	private String id;

	/**
	 * 显示名称
	 */
	@JSONField(name = "display_name")
	@TableField("display_name")
	private String displayName;

	/**
	 * 邮箱
	 */
	@TableField("email")
	private String email;

	/**
	 * 头像地址
	 */
	@JSONField(name = "photo_url")
	@TableField("photo_url")
	private String photoUrl;

	/**
	 * 用户名
	 */
	@TableField("name")
	private String name;

	/**
	 * 密码
	 */
	@TableField("password")
	private String password;

	/**
	 * 职位
	 */
	@JSONField(name = "job_title")
	@TableField("job_title")
	private String jobTitle;

	/**
	 * 组织名称
	 */
	@JSONField(name = "org_name")
	@TableField("org_name")
	private String orgName;

	/**
	 * 组织ID
	 */
	@JSONField(name = "org_id")
	@TableField("org_id")
	private String orgId;
}
