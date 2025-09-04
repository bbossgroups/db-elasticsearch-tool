package org.frameworkset.elasticsearch.imp;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.frameworkset.tran.config.DynamicParam;
import org.frameworkset.tran.config.DynamicParamContext;
import org.frameworkset.tran.config.ImportBuilder;
import org.frameworkset.tran.plugin.db.input.DBInputConfig;
import org.frameworkset.tran.plugin.es.output.ElasticsearchOutputConfig;
import org.frameworkset.tran.schedule.DataStreamBuilder;
import org.frameworkset.tran.schedule.ImportIncreamentConfig;
import org.frameworkset.tran.schedule.quartz.AbstractQuartzJobHandlerV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Description: 使用quartz等外部环境定时运行导入数据，需要调试测试quatz作业同步功能，按如下配置进行操作：</p>
 *  *  * 1.在配置文件中添加quartz作业配置-resources/org/frameworkset/task/quarts-task.xml相关内容
 *  *  * <list>
 *  *  *
 *  *  * 			<property name="QuartzTimestampImportTask" jobid="QuartzTimestampImportTask"
 *  *  * 							  bean-name="QuartzTimestampImportTask"
 *  *  * 							  method="execute"
 *  *  * 							  cronb_time="${quartzImportTask.crontime:*\/20 * * * * ?}" used="false"
 *  *  * 							  shouldRecover="false"
 *  *  * 					/>
 *  *  *
 *  *  * </list>
 *  *  *
 *  *  * 	<property name="QuartzTimestampImportTask" class="org.frameworkset.elasticsearch.imp.QuartzTimestampImportTask"
 *  *  * 			  destroy-method="destroy"
 *  *  * 			  init-method="init"
 *  *  * 	/>
 *  *  *
 *  *  * 2.添加一个带main方法的作业运行
 *  *  * public class QuartzTest {
 *  *  * 	public static void main(String[] args){
 *  *  * 		TaskService.getTaskService().startService();
 *  *  *        }
 *  *  * }
 *  *  * 然后运行main方法即可
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/13 13:45
 * @author biaoping.yin
 * @version 1.0
 */
public class CustomTimestampImportTask extends AbstractQuartzJobHandlerV2 {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected  DataStreamBuilder buildDataStreamBuilder(){
        return params -> {
            ImportBuilder importBuilder = new ImportBuilder();


            //指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
            // 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
            // select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
            // log_id和数据库对应的字段一致,就不需要设置setLastValueColumn信息，
            // 但是需要设置setLastValueType告诉工具增量字段的类型
            DBInputConfig dbInputConfig = new DBInputConfig();
            dbInputConfig.setSql("select * from td_sm_log where LOG_OPERTIME < #[date]");
            importBuilder.addJobDynamicInputParam("date", new DynamicParam() {
                @Override
                public Object getValue(String paramName, DynamicParamContext dynamicParamContext) {
                    return new Date();
                }
            });
            importBuilder.setIncreamentImport(false);
            importBuilder.setInputConfig(dbInputConfig);
//		importBuilder.addIgnoreFieldMapping("remark1");
            /**
             * es相关配置
             */
            ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
            elasticsearchOutputConfig
                    .setIndex("dbdemo"); //必填项
//					.setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
            elasticsearchOutputConfig.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id

            elasticsearchOutputConfig.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
            elasticsearchOutputConfig.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
            importBuilder.setOutputConfig(elasticsearchOutputConfig);

            importBuilder.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
                    .setUseLowcase(false)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
                    .setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
                    .setBatchSize(10);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理
 
            //映射和转换配置结束

            /**
             * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
             */
            importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
            importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
            importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
            importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行

            return importBuilder;

        };
        
    }
    
    public static void main(String[] args){
        CustomTimestampImportTask customTimestampImportTask = new CustomTimestampImportTask();
        customTimestampImportTask.init();
        Date lastValue = null;
        while(true){
            customTimestampImportTask.execute();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
