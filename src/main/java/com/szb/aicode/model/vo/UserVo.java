package com.szb.aicode.model.vo;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class UserVo implements Serializable {



    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Column("userRole")
    private String userRole;


    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;



}
