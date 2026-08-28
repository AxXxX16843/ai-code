package com.szb.aicode.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.szb.aicode.ai.AiGenRoutingService;
import com.szb.aicode.annotation.AuthCheck;
import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.common.DeleteRequest;
import com.szb.aicode.common.ResultUtils;
import com.szb.aicode.constant.AppConstant;
import com.szb.aicode.constant.UserConstant;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.exception.ThrowUtils;
import com.szb.aicode.model.dto.app.AppAddRequest;
import com.szb.aicode.model.dto.app.AppAdminUpdateRequest;
import com.szb.aicode.model.dto.app.AppQueryRequest;
import com.szb.aicode.model.dto.app.AppUpdateRequest;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import com.szb.aicode.model.vo.AppVo;
import com.szb.aicode.service.ChatHistoryService;
import com.szb.aicode.service.DownloadProjectService;
import com.szb.aicode.service.UserService;
import dev.langchain4j.internal.Json;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.szb.aicode.model.entity.App;
import com.szb.aicode.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.szb.aicode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 应用 控制层。
 *
 * @author 86186
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private DownloadProjectService downloadProjectService;

    @Resource
    private AiGenRoutingService aiGenRoutingService;


    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response){

        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");

        User loginUser = userService.getLoginUser(request);

        App app = appService.getById(appId);
        if(app == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File file = new File(sourceDirPath);
        ThrowUtils.throwIf(!file.exists()|| !file.isDirectory(),ErrorCode.NOT_FOUND_ERROR,"代码文件不存在");

        String downloadFileName = String.valueOf(appId);

        downloadProjectService.downloadProjectAsZip(sourceDirPath,downloadFileName,response);


    }

    @PostMapping("/deploy")
    public BaseResponse<String> deploy(@RequestParam Long appId,HttpServletRequest request) {

        ThrowUtils.throwIf(appId==null||appId<0,ErrorCode.NOT_FOUND_ERROR,"应用id错误");

        User loginUser = userService.getLoginUser(request);

        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"用户未登录");

        String deployDir = appService.deployCode(appId, loginUser);

        return ResultUtils.success(deployDir);

    }

    @GetMapping(value = "/gene",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> gene(@RequestParam String message,
                                           @RequestParam Long appId,
                                           HttpServletRequest request) {
        ThrowUtils.throwIf(message==null,ErrorCode.PARAMS_ERROR,"提示词不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id错误");
        User loginUser = userService.getLoginUser(request);
        Flux<String> stringFlux = appService.chatToGeneCode(message, appId, loginUser);
        Flux<ServerSentEvent<String>> responseFlux = stringFlux.map(chunk->{
            Map<String,String> map =Map.of("d",chunk);
            String jsonStr = JSONUtil.toJsonStr(map);
            return ServerSentEvent.<String>builder().data(jsonStr).build();
        }).onErrorResume(error -> {
            log.error("应用代码生成失败, appId={}", appId, error);
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("generation-error")
                    .data("项目生成失败，请重试")
                    .build());
        });
        return responseFlux
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));

    }

    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 参数校验
        App app = appService.getApp(appAddRequest, request);
        return ResultUtils.success(app.getId());
    }



    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        User userLogin = userService.getById(user.getId());
        App app = appService.getById(appUpdateRequest.getId());
        Long userId = app.getUserId();
        if(!Objects.equals(userLogin.getId(), userId)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"只能修改自己的作品");
        }
        app.setAppName(appUpdateRequest.getAppName());
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) || !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {

            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = appService.removeById(id);

        return ResultUtils.success(result);
    }

    @GetMapping("/get/vo")
    public BaseResponse<AppVo> getAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类（包含用户信息）
        return ResultUtils.success(appService.getAppVO(app));
    }



    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVo>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);

        Page<AppVo> appVoPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVo> appVOList = appService.getAppVOList(appPage.getRecords());
        appVoPage.setRecords(appVOList);
        return ResultUtils.success(appVoPage);
    }
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVo>> listGoodAppVoByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVo> AppVoPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVo> AppVoList = appService.getAppVOList(appPage.getRecords());
        AppVoPage.setRecords(AppVoList);
        return ResultUtils.success(AppVoPage);
    }
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVo>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVo> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVo> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVo> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appService.getAppVO(app));
    }

}













