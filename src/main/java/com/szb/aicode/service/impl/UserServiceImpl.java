package com.szb.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.common.ResultUtils;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.dto.user.UserLoginRequest;
import com.szb.aicode.model.dto.user.UserQueryRequest;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.mapper.UserMapper;
import com.szb.aicode.model.enums.UserRoleEnum;
import com.szb.aicode.model.dto.user.RegisterRequest;
import com.szb.aicode.model.vo.UserLoginVo;
import com.szb.aicode.model.vo.UserVo;
import com.szb.aicode.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.szb.aicode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author 86186
 * @since 2026-08-23
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Override
    public BaseResponse<Long> register(RegisterRequest userRequest) {

        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        String check = userRequest.getCheck();
//        校验传入值
        if(StrUtil.hasBlank(username,password,check)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请全部填写后再注册");
        }
        if(username.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名不能小于四位");
        }
        if(password.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不能小于六位");
        }
//        校验两次密码是否相同
        if(!password.equals(check)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不同");
        }
//        判断用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userName",username);
        long l = this.mapper.selectCountByQuery(queryWrapper);
        if(l>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在");

        }
//        插入数据库
        User user = new User();
        user.setUserName("康神");
        user.setUserAccount(username);
        user.setUserPassword(getPassword(password));
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据库异常");
        }
        return ResultUtils.success(user.getId());
    }

    @Override
    public String getPassword(String simplePassword) {
        final String YZ = "AX";
        return DigestUtils.md5DigestAsHex((simplePassword + YZ).getBytes());
    }

    @Override
    public BaseResponse<UserLoginVo> login(UserLoginRequest userRequest, HttpServletRequest request) {


//        校验
        String userAccount = userRequest.getUserAccount();
        String password = userRequest.getPassword();

        if(StrUtil.hasBlank(userAccount,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号密码不能为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名不能小于四位");
        }
        if(password.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不能小于六位");
        }
//      查询

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",getPassword(password));
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码错误，请重新输入");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        UserLoginVo userLoginVo = getUserLoginVo(user);
        return ResultUtils.success(userLoginVo);
    }

    @Override
    public UserLoginVo getUserLoginVo(User user) {
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtil.copyProperties(user,userLoginVo);
        return userLoginVo;
    }

    @Override
    public BaseResponse<UserLoginVo> getLogin(HttpServletRequest request) {

        User CUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        if(CUser==null || CUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        CUser = this.getById(CUser.getId());
        if(CUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        return ResultUtils.success(getUserLoginVo(CUser));
    }


    @Override
    public BaseResponse<Boolean> logout(HttpServletRequest request) {

        User CUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        if(CUser==null || CUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return ResultUtils.success(true);
    }

    @Override
    public UserVo getUserVo(User user) {

        UserVo userVo = new UserVo();

        BeanUtil.copyProperties(user,userVo);

        return userVo;
    }

    @Override
    public List<UserVo> getUserVoList(List<User> userList) {

        List<UserVo> userVoList = new ArrayList<>();

        for (User user : userList) {
            userVoList.add(getUserVo(user));
        }

        return userVoList;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if(userQueryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));

    }

}
