package com.szb.aicode.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.szb.aicode.annotation.AuthCheck;
import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.common.ResultUtils;
import com.szb.aicode.constant.UserConstant;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.exception.ThrowUtils;
import com.szb.aicode.model.dto.*;
import com.szb.aicode.model.vo.UserLoginVo;
import com.szb.aicode.model.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.service.UserService;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author 86186
 * @since 2026-08-23
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody RegisterRequest userRequest) {
        ThrowUtils.throwIf(userRequest==null, ErrorCode.PARAMS_ERROR);

        return userService.register(userRequest);
    }
    @PostMapping("/login")
    public BaseResponse<UserLoginVo> login(@RequestBody UserLoginRequest userRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userRequest==null, ErrorCode.PARAMS_ERROR);
        return userService.login(userRequest,request);
    }
    @PostMapping("/get/login")
    public BaseResponse<UserLoginVo> getLogin( HttpServletRequest request) {

        return userService.getLogin(request);

    }
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {

        return userService.logout(request);

    }


    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest==null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String defaultKey="123456";
        String password = userService.getPassword(defaultKey);
        user.setUserPassword(password);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(user.getId());
    }
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUser(@RequestParam Long id) {

        ThrowUtils.throwIf(id==null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }
    @GetMapping("/get/vo")
    public BaseResponse<UserVo> getUserVo(@RequestParam Long id) {

        ThrowUtils.throwIf(id==null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        UserVo userVo = userService.getUserVo(user);
        return ResultUtils.success(userVo);
    }
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@RequestBody UserDeleteRequest userDeleteRequest) {
        if(userDeleteRequest==null || userDeleteRequest.getId()<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(userDeleteRequest.getId());
        return ResultUtils.success(b);
    }
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody UserUpdateRequest userUpdateRequest) {
        if(userUpdateRequest==null || userUpdateRequest.getId()==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean b = userService.updateById(user);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(b);
    }

    @PostMapping("/get/vo/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVo>> getUserVoList(@RequestBody UserQueryRequest userQueryRequest) {

        ThrowUtils.throwIf(userQueryRequest==null, ErrorCode.PARAMS_ERROR);
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();

        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVo> userVoPage = new Page<>(pageNum, pageSize,userPage.getTotalRow());
        List<UserVo> userVoList = userService.getUserVoList(userPage.getRecords());
        userVoPage.setRecords(userVoList);
        return ResultUtils.success(userVoPage);
    }

}
