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

import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.config.ImportBuilder;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.metrics.TaskMetrics;
import org.frameworkset.tran.plugin.db.input.DBInputConfig;
import org.frameworkset.tran.plugin.es.output.ElasticsearchOutputConfig;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.ImportIncreamentConfig;
import org.frameworkset.tran.schedule.TaskContext;
import org.frameworkset.tran.schedule.timer.TimeUtil;
import org.frameworkset.tran.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: 同步处理程序，如需调试同步功能，直接运行main方法即可，定时按特定条件增量同步数据
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/27 20:38
 * @author biaoping.yin
 * @version 1.0
 */
public class PostgresCollecttimeIncreamentDbdemo {
	private static Logger logger = LoggerFactory.getLogger(PostgresCollecttimeIncreamentDbdemo.class);
	public static void main(String args[]){
		PostgresCollecttimeIncreamentDbdemo dbdemo = new PostgresCollecttimeIncreamentDbdemo();
		boolean dropIndice = true;//CommonLauncher.getBooleanAttribute("dropIndice",false);//同时指定了默认值
//		dbdemo.fullImportData(  dropIndice);
//		dbdemo.scheduleImportData(dropIndice);
		dbdemo.scheduleTimestampImportData(dropIndice);
//		dbdemo.scheduleImportData(dropIndice);
//		args[1].charAt(0) == args[2].charAt(0);
	}

	/**
	 * elasticsearch地址和数据库地址都从外部配置文件application.properties中获取，加载数据源配置和es配置
	 */
	public void scheduleTimestampImportData(boolean dropIndice){
		ImportBuilder importBuilder = new ImportBuilder() ;
		//增量定时任务不要删表，但是可以通过删表来做初始化操作
		if(dropIndice) {
			try {
				//清除测试表,导入的时候回重建表，测试的时候加上为了看测试效果，实际线上环境不要删表
				String repsonse = ElasticSearchHelper.getRestClientUtil().dropIndice("postgresdbdemo");
				System.out.println(repsonse);
			} catch (Exception e) {
			}
		}

		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// log_id和数据库对应的字段一致,就不需要设置setLastValueColumn信息，
		// 但是需要设置setLastValueType告诉工具增量字段的类型
		DBInputConfig dbInputConfig = new DBInputConfig();
		dbInputConfig.setDbName("postgres");
		dbInputConfig.setJdbcFetchSize(2000);
//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from batchtest1 where optime >= #[start_optime] and optime < #[end_optime] and collecttime > #[collecttime]");
		importBuilder.setInputConfig(dbInputConfig);

		importBuilder.setLastValueColumn("collecttime");
		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);

		importBuilder.addParam("start_optime", TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2018-03-21 00:27:21"))
				.addParam("end_optime",TimeUtil.parserDate("yyyy-MM-dd HH:mm:ss","2019-12-30 00:27:21"));
//		importBuilder.addFieldMapping("LOG_CONTENT","message");
//		importBuilder.addIgnoreFieldMapping("remark1");
//		importBuilder.setSql("select * from td_sm_log ");
		/**
		 * es相关配置
		 */
//		importBuilder.setTargetElasticsearch("default,test");//同步数据到两个es集群
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("test");
		elasticsearchOutputConfig
				.setIndex("postgresdbdemo"); //必填项
//				.setIndexType("dbdemo") //es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType
//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
		//importBuilder.setEsIdField("log_id");//设置文档主键，不设置，则自动产生文档id
//		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度

		elasticsearchOutputConfig.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
		elasticsearchOutputConfig.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
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
		importBuilder.setOutputConfig(elasticsearchOutputConfig);

		importBuilder.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
				.setUseLowcase(true)  //可选项，true 列名称转小写，false列名称不转换小写，默认false，只要在UseJavaName为false的情况下，配置才起作用
				.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				.setBatchSize(10);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

		//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
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
			public void throwException(TaskContext taskContext, Exception e) {
				System.out.println("throwException");
			}
		}).addCallInterceptor(new CallInterceptor() {
			@Override
			public void preCall(TaskContext taskContext) {
				System.out.println("preCall 1");
			}

			@Override
			public void afterCall(TaskContext taskContext) {
				logger.info("afterCall ----------"+taskContext.getJobTaskMetrics().toString());
			}

			@Override
			public void throwException(TaskContext taskContext, Exception e) {
				logger.info("afterCall ----------"+taskContext.getJobTaskMetrics().toString(),e);
			}
		});
//		//设置任务执行拦截器结束，可以添加多个
		//增量配置开始

		//映射和转换配置开始
//		/**
//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
//		 */
//		importBuilder.addFieldMapping("document_id","docId")
//				.addFieldMapping("docwtime","docwTime")
//				.addIgnoreFieldMapping("channel_id");//添加忽略字段
//
//
//		/**
//		 * 为每条记录添加额外的字段和值
//		 * 可以为基本数据类型，也可以是复杂的对象
//		 */
//		importBuilder.addFieldValue("testF1","f1value");
//		importBuilder.addFieldValue("testInt",0);
//		importBuilder.addFieldValue("testDate",new Date());
//		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
//		TestObject testObject = new TestObject();
//		testObject.setId("testid");
//		testObject.setName("jackson");
//		importBuilder.addFieldValue("testObject",testObject);
//
		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
//				Date date = context.getDateValue("LOG_OPERTIME");
//				context.addFieldValue("collecttime",new Date());
				logger.info("refactor");
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
		importBuilder.setAsyn(false);//true 异步方式执行，不等待所有导入作业任务结束，方法快速返回；false（默认值） 同步方式执行，等待所有导入作业任务结束，所有作业结束后方法才返回

		importBuilder.setExportResultHandler(new ExportResultHandler<String,String>() {
			@Override
			public void success(TaskCommand<String,String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
				logger.debug(result);
			}

			@Override
			public void error(TaskCommand<String,String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
				logger.debug(result);
			}

			@Override
			public void exception(TaskCommand<String,String> taskCommand, Exception exception) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.debug(taskMetrics.toString());
			}

			@Override
			public int getMaxRetry() {
				return 0;
			}
		});
		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
//		dataStream.destroy();//释放资源
		System.out.println();


	}


}
