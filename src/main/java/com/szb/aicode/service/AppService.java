package com.szb.aicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.szb.aicode.model.dto.app.AppQueryRequest;
import com.szb.aicode.model.entity.App;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.vo.AppVo;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 86186
 * @since 2026-08-24
 */
public interface AppService extends IService<App> {

    AppVo getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVo> getAppVOList(List<App> appList);

    Flux<String> chatToGeneCode(String message, Long appId, User loginUser);

    String deployCode(Long appId, User loginUser);
}
