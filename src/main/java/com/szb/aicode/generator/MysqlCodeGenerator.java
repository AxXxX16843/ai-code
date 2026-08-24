package com.szb.aicode.generator;


import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

public class MysqlCodeGenerator {

    private final static String TABLE_NAME = "app";

    public static void main(String[] args) {

        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String,Object> map = dict.getByPath("spring.datasource");
        String url = map.get("url").toString();
        String username = map.get("username").toString();
        String password = map.get("password").toString();

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }
    public static GlobalConfig createGlobalConfigUseStyle2() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("com.szb.aicode.gene");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAME)
                        .setLogicDeleteColumn("IsDeleted");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();

        globalConfig.enableController();

        globalConfig.enableServiceImpl();

        globalConfig.enableService();

        globalConfig.enableMapperXml();


        //可以单独配置某个列
        return globalConfig;
    }

}
