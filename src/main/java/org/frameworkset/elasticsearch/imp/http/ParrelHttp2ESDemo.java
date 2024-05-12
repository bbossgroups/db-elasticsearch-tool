package org.frameworkset.elasticsearch.imp.http;
/**
 * Copyright 2022 bboss
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

import org.apache.http.HttpResponse;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.config.ImportBuilder;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.metrics.TaskMetrics;
import org.frameworkset.tran.plugin.es.output.ElasticsearchOutputConfig;
import org.frameworkset.tran.plugin.http.input.HttpInputConfig;
import org.frameworkset.tran.plugin.http.input.HttpRecord;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.TaskContext;
import org.frameworkset.tran.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * <p>Description: http并行查询同步数据到Elasticsearch案例
 * 并行查询不支持增量同步处理
 * </p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2022/7/1
 * @author biaoping.yin
 * @version 1.0
 */
public class ParrelHttp2ESDemo {
	private static Logger logger = LoggerFactory.getLogger(ParrelHttp2ESDemo.class);
	public static void main(String[] args){


		ImportBuilder importBuilder = new ImportBuilder() ;
		importBuilder.setFetchSize(50).setBatchSize(10);
		HttpInputConfig httpInputConfig = new HttpInputConfig();
		//指定导入数据的dsl语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：


		httpInputConfig.setDslFile("httpdsl.xml")
				.setQueryDslName("parrelqueryDsl")
				.setQueryUrl("/httpservice/getData.api")
                .setShowDsl(true)
				.addSourceHttpPoolName("http.poolNames","datatran")
				.addHttpInputConfig("datatran.http.health","/health")
				.addHttpInputConfig("datatran.http.hosts","192.168.137.1:808")
				.addHttpInputConfig("datatran.http.timeoutConnection","5000")
				.addHttpInputConfig("datatran.http.timeoutSocket","50000")
				.addHttpInputConfig("datatran.http.connectionRequestTimeout","50000")
				.addHttpInputConfig("datatran.http.maxTotal","200")
				.addHttpInputConfig("datatran.http.defaultMaxPerRoute","100")
				.addHttpInputConfig("datatran.http.failAllContinue","true");

		importBuilder.setInputConfig(httpInputConfig);
        //设置并行查询线程数和等待队长度、结果异步处理队列长度
        httpInputConfig.setQueryThread(10);
        httpInputConfig.setQueryThreadQueue(20);
        httpInputConfig.setQueryResultQueue(20);
        //添加并行查询参数组
		importBuilder.addJobInputParam("otherParam","陈雨菲2:0战胜戴资颖");
        importBuilder.makeParamGroup();
        importBuilder.addJobInputParam("otherParam","安塞龙1:2惜败黄智勇");
        importBuilder.makeParamGroup();
        importBuilder.addJobInputParam("otherParam","桃田0:2惨败昆拉武特");
        importBuilder.makeParamGroup();
        importBuilder.addJobInputParam("otherParam","石宇奇2:1胜黄智勇");
        importBuilder.makeParamGroup();
        importBuilder.addJobInputParam("otherParam","翁弘扬2:0横扫乔纳坦");
        importBuilder.makeParamGroup();

		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("default")
				.setIndex("parrelhttps2es")
				.setEsIdField("log_id")//设置文档主键，不设置，则自动产生文档id
				.setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
				.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
		/**
		 elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator() {
		 //如果指定EsIdGenerator，则根据下面的方法生成文档id，
		 // 否则根据setEsIdField方法设置的字段值作为文档id，
		 // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id

		 @Override
		 public Object genId(Context context) throws Exception {
		 return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
		 }
		 });
		 */
//				.setIndexType("dbdemo") ;//es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType;
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
		/**
		 * es相关配置
		 */
//		elasticsearchOutputConfig.setTargetElasticsearch("default,test");//同步数据到两个es集群

		importBuilder.setOutputConfig(elasticsearchOutputConfig);
		importBuilder.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				;  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(1000L * 60); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
//
//		//设置任务执行拦截器，可以添加多个，定时任务每次执行的拦截器
		importBuilder.addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				System.out.println("preCall");
			}

			@Override
			public void afterCall(TaskContext taskContext) {
				System.out.println("afterCall");
			}

			@Override
			public void throwException(TaskContext taskContext, Throwable e) {
				System.out.println("throwException");
			}
		}).addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				System.out.println("preCall 1");
			}

			@Override
			public void afterCall(TaskContext taskContext) {
				System.out.println("afterCall 1");
			}

			@Override
			public void throwException(TaskContext taskContext, Throwable e) {
				System.out.println("throwException 1");
			}
		});
//		//设置任务执行拦截器结束，可以添加多个

		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				long logTime = context.getLongValue("logTime");
				context.addFieldValue("logTime",new Date(logTime));
				long oldLogTime = context.getLongValue("oldLogTime");
				context.addFieldValue("oldLogTime",new Date(oldLogTime));
				long oldLogTimeEndTime = context.getLongValue("oldLogTimeEndTime");
				context.addFieldValue("oldLogTimeEndTime",new Date(oldLogTimeEndTime));
//				Date date = context.getDateValue("LOG_OPERTIME");

				HttpRecord record = (HttpRecord) context.getCurrentRecord();
				HttpResponse response = record.getResponse();//可以从httpresponse中获取head之类的信息
				context.addFieldValue("collecttime",new Date());//添加采集时间

			}
		});
		//映射和转换配置结束

		/**
		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行

		importBuilder.setExportResultHandler(new ExportResultHandler<String>() {
			@Override
			public void success(TaskCommand<String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
				logger.debug(result);
			}

			@Override
			public void error(TaskCommand<String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
				logger.debug(result);
			}

			@Override
			public void exception(TaskCommand<String> taskCommand, Throwable exception) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.debug(taskMetrics.toString());
			}

		});
		/**
		 * 执行http服务数据导入es作业
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
	}
}
