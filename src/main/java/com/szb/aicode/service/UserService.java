package com.szb.aicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.model.dto.user.UserLoginRequest;
import com.szb.aicode.model.dto.user.UserQueryRequest;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.dto.user.RegisterRequest;
import com.szb.aicode.model.vo.UserLoginVo;
import com.szb.aicode.model.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author 86186
 * @since 2026-08-23
 */
public interface UserService extends IService<User> {

    BaseResponse<Long> register(RegisterRequest userRequest);

    String getPassword(String simplePassword);

    BaseResponse<UserLoginVo> login(UserLoginRequest userRequest, HttpServletRequest request);

    UserLoginVo getUserLoginVo(User user);

    BaseResponse<UserLoginVo> getLogin(HttpServletRequest request);

    BaseResponse<Boolean> logout(HttpServletRequest request);

    UserVo getUserVo(User user);

    List<UserVo> getUserVoList(List<User> userList);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

}
