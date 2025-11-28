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
 * System user information table
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUserEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * User ID, auto-increment primary key
	 */
	@TableId(value = "id", type = IdType.INPUT)
	private String id;

	/**
	 * Display name
	 */
	@JSONField(name = "display_name")
	@TableField("display_name")
	private String displayName;

	/**
	 * Email
	 */
	@TableField("email")
	private String email;

	/**
	 * Avatar URL
	 */
	@JSONField(name = "photo_url")
	@TableField("photo_url")
	private String photoUrl;

	/**
	 * Username
	 */
	@TableField("name")
	private String name;

	/**
	 * Password
	 */
	@TableField("password")
	private String password;

	/**
	 * Position
	 */
	@JSONField(name = "job_title")
	@TableField("job_title")
	private String jobTitle;

	/**
	 * Organization name
	 */
	@JSONField(name = "org_name")
	@TableField("org_name")
	private String orgName;

	/**
	 * Organization ID
	 */
	@JSONField(name = "org_id")
	@TableField("org_id")
	private String orgId;
}
