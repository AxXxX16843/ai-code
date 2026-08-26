package com.szb.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.constant.AppConstant;
import com.szb.aicode.core.AICodeGeneratorFaced;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.exception.ThrowUtils;
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
import com.szb.aicode.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
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
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private AICodeGeneratorFaced aiCodeGeneratorFaced;

    @Resource
    private ChatHistoryService chatHistoryService;

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
        StringBuilder stringBuilder = new StringBuilder();
        return stream.
                map(chunk->{
                    stringBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(()->{
                    String aiMessage= stringBuilder.toString();
                    if (StrUtil.isNotBlank(aiMessage)) {
                        chatHistoryService.addChatHistory(loginUser,aiMessage,ChatHistoryMessageTypeEnum.AI.getValue(),appId);
                    }
                })
                .doOnError(error -> {
            // 如果AI回复失败，也要记录错误消息
            String errorMessage = "AI回复失败: " + error.getMessage();
            chatHistoryService.addChatHistory(loginUser, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), appId);
        });
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
        return AppConstant.CODE_DEPLOY_HOST+File.separator+deployKey;
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
