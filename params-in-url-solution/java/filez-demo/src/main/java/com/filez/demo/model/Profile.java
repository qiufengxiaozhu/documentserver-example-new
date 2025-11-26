package com.filez.demo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.filez.demo.entity.SysUserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File creator information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    /** User ID */
    private String id;

    @JSONField(name = "display_name")
    private String displayName;

    private String email;

    @JSONField(name = "photo_url")
    private String photoUrl;

    private String name;

    @JSONField(name = "job_title")
    private String jobTitle;

    @JSONField(name = "org_name")
    private String orgName;

    @JSONField(name = "org_id")
    private String orgId;

    public static Profile convertUserToProfile(SysUserEntity user) {
        if (user == null) {
            return null;
        }
        return Profile.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .photoUrl(user.getPhotoUrl())
                .name(user.getName())
                .jobTitle(user.getJobTitle())
                .orgName(user.getOrgName())
                .orgId(user.getOrgId())
                .build();
    }
}
