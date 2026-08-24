package com.szb.aicode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.model.entity.App;
import com.szb.aicode.mapper.AppMapper;
import com.szb.aicode.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author 86186
 * @since 2026-08-24
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
