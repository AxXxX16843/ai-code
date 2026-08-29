package com.szb.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.ai.AiGenRoutingService;
import com.szb.aicode.ai.AiGenRoutingServiceFactory;
import com.szb.aicode.ai.AiGeneratorCodeService;
import com.szb.aicode.ai.CodeGeneratorServiceFactory;
import com.szb.aicode.constant.AppConstant;
import com.szb.aicode.core.AICodeGeneratorFaced;
import com.szb.aicode.core.builder.VueProjectBuilder;
import com.szb.aicode.core.handle.StreamHandleExecutor;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.exception.ThrowUtils;
import com.szb.aicode.model.dto.app.AppAddRequest;
import com.szb.aicode.model.dto.app.AppQueryRequest;
import com.szb.aicode.model.entity.App;
import com.szb.aicode.mapper.AppMapper;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import com.szb.aicode.model.vo.AppVo;
import com.szb.aicode.model.vo.UserVo;
import com.szb.aicode.service.AppService;
import com.szb.aicode.service.ChatHistoryService;
import com.szb.aicode.service.ScreenshotService;
import com.szb.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.szb.aicode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 应用 服务层实现。
 *
 * @author 86186
 * @since 2026-08-24
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private AICodeGeneratorFaced aiCodeGeneratorFaced;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandleExecutor streamHandleExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;


    @Resource
    private AiGenRoutingServiceFactory aiGenRoutingServiceFactory;




    @Transactional
    @Override
    public App getApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 获取当前登录用户
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        if(loginUser==null || loginUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        loginUser = userService.getById(loginUser.getId());
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位

        AiGenRoutingService aiGenRoutingService = aiGenRoutingServiceFactory.createAiGenRoutingService();

        GeneratorTypeEnum generatorTypeEnum = aiGenRoutingService.routingType(initPrompt);

        app.setCodeGenType(generatorTypeEnum.getValue());
        // 插入数据库
        boolean result = save(app);


        String name = aiGenRoutingService.getName(initPrompt);

        app.setAppName(name);

        updateById(app);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app;
    }



    @Override
    public boolean removeById(Serializable id) {

        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteChatHistory(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

    @Override
    public Flux<String> chatToGeneCode(String message, Long appId, User loginUser) {
        ThrowUtils.throwIf(message==null,ErrorCode.PARAMS_ERROR,"提示词不能为空");
        ThrowUtils.throwIf(appId==null && appId<0,ErrorCode.PARAMS_ERROR,"应用id错误");
        App app = getById(appId);
        if(app==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        String codeGenType = app.getCodeGenType();

        GeneratorTypeEnum byValue = GeneratorTypeEnum.getByValue(codeGenType);
        if(byValue==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持的生成类型");
        }
        chatHistoryService.addChatHistory(loginUser,message,
                ChatHistoryMessageTypeEnum.USER.getValue(),appId);

        Flux<String> stream = aiCodeGeneratorFaced.generatorAndSaveFluxStream(message, byValue, appId);
        return streamHandleExecutor.handle(loginUser,stream,appId,chatHistoryService,byValue);
    }

    @Override
    public String deployCode(Long appId, User loginUser) {

        ThrowUtils.throwIf(appId==null||appId<0,ErrorCode.NOT_FOUND_ERROR,"应用id错误");

        ThrowUtils.throwIf(loginUser==null,ErrorCode.NOT_LOGIN_ERROR,"用户未登录");

        App app = getById(appId);
        if(app==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在请先创建");
        }

        String deployKey = app.getDeployKey();
        if(deployKey==null){
            deployKey = RandomUtil.randomString(6);
        }
        app.setDeployKey(deployKey);
        String codeGenType = app.getCodeGenType();
        String dirName=codeGenType+"_"+appId;
        String dirPath= AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+dirName;
        File resource = new File(dirPath);
        if (!resource.exists() || !resource.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        GeneratorTypeEnum byValue = GeneratorTypeEnum.getByValue(codeGenType);
        if(byValue.getValue().equals("vue_project")){
            boolean buildSuccess = vueProjectBuilder.buildProject(dirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(dirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            resource = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());

        }
        String deployDir=AppConstant.CODE_DEPLOY_ROOT_DIR+File.separator+deployKey;
        File deploy = new File(deployDir);
        try {
            FileUtil.copyContent(resource,deploy,true);
        }catch (Exception e){
            System.out.println("源目录存在? " + resource.exists() + " -> " + resource.getAbsolutePath());
            System.out.println("目标父目录存在? " + deploy.getParentFile().exists() + " -> " + deploy.getParent());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败");
        }
        app.setDeployedTime(LocalDateTime.now());
        boolean updateResult = updateById(app);
        if(!updateResult){

            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署信息更新失败");
        }
        String url = AppConstant.CODE_DEPLOY_HOST + File.separator + deployKey;

        generateAppScreenshotAsync(appId,url);

        return url;
    }

    private void generateAppScreenshotAsync(Long appId,String url) {

        Thread.startVirtualThread(()->{

            String cosUrl = screenshotService.generateAndUploadScreenshot(url);
            ThrowUtils.throwIf(cosUrl==null,ErrorCode.SYSTEM_ERROR,"截图上传失败");
            App app = new App();
            app.setId(appId);
            app.setCover(cosUrl);
            boolean b = updateById(app);
            ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });

    }


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
