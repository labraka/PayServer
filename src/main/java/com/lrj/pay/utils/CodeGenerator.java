package com.lrj.pay.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;

import java.util.Collections;

public class CodeGenerator {

    private static final String url="jdbc:mysql://localhost:3306/pay?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true";
    private static final String username="root";
    private static final String password="123456";

    private static final String author="lrj";
    private static final String outputDir="/Payment/src/main/java/com/lrj/pay/";
    private static final String[] tableName={"refund"};

    public static void main(String[] args) {

        // 获取当前工作目录绝对路径
        String projectPath = System.getProperty("user.dir");
        // 数据源配置
        DataSourceConfig.Builder dataSourceConfig = new DataSourceConfig
                .Builder(url, username, password)
                .dbQuery(new MySqlQuery())
                .typeConvert(new MySqlTypeConvert())
                .keyWordsHandler(new MySqlKeyWordsHandler());

        FastAutoGenerator.create(dataSourceConfig)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author(author) //设置作者
                            .commentDate("YYYY-MM-DD HH:mm:ss")//注释日期
                            .outputDir(projectPath + outputDir); //指定输出目录
                })
                // 包名配置
                .packageConfig(builder -> {
                    builder.parent("com.lrj.pay")// 设置父包名
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .entity("entity")
                            .mapper("mapper")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, projectPath + outputDir + "/src/main/resources/mapper"));
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(tableName) // 设置需要生成的表名
                            .addTablePrefix("tb_"); // 设置过滤表前缀
                    builder.entityBuilder().enableLombok();//开启 lombok 模型
                    builder.entityBuilder().enableTableFieldAnnotation();//开启生成实体时生成字段注解
                    builder.controllerBuilder().enableRestStyle();//开启生成@RestController 控制器

                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }


}
