package com.szb.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.dto.app.AppQueryRequest;
import com.szb.aicode.model.entity.App;
import com.szb.aicode.mapper.AppMapper;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.vo.AppVo;
import com.szb.aicode.model.vo.UserVo;
import com.szb.aicode.service.AppService;
import com.szb.aicode.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 86186
 * @since 2026-08-24
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;



    @Override
    public AppVo getAppVO(App app){
        if(app==null){
            return null;
        }
        AppVo appVo = new AppVo();
        BeanUtil.copyProperties(app,appVo);
        if (app.getUserId()!=null) {
            User user = userService.getById(app.getUserId());
            UserVo userVo = userService.getUserVo(user);
            appVo.setUserVo(userVo);
        }
        return appVo;
    }
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
    @Override
    public List<AppVo> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVo> userVoMap = userService.listByIds(userIds).
                stream().collect(Collectors.toMap(User::getId, userService::getUserVo));
        return appList.stream().map(app->{
            AppVo appVO = getAppVO(app);
            UserVo userVo = userVoMap.get(app.getUserId());
            appVO.setUserVo(userVo);
            return appVO;
        }).collect(Collectors.toList());
    }
}
