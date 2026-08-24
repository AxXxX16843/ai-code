package com.szb.aicode.aop;


import com.szb.aicode.annotation.AuthCheck;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.UserRoleEnum;
import com.szb.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.szb.aicode.constant.UserConstant.USER_LOGIN_STATE;

@Component
@Aspect
public class AuthInterceptor {


    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint pjp, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User CUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getByValue(mustRole);
        if(CUser==null || CUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        CUser = userService.getById(CUser.getId());
        String userRole = CUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getByValue(userRole);
        if(mustRole.isEmpty()){
            return pjp.proceed();
        }
        if(userRole==null || userRole.isEmpty()){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }
        return pjp.proceed();
    }


}
