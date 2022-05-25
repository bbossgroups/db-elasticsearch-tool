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

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.db.DBConfigBuilder;
import org.frameworkset.tran.es.input.db.ES2DBExportBuilder;
import org.frameworkset.tran.schedule.CallInterceptor;
import org.frameworkset.tran.schedule.TaskContext;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <p>Description: 从es中查询数据导入数据库案例,基于时间戳增量同步，采用slicescroll检索
 * 同步处理程序，如需调试同步功能，直接运行main方法即可</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 14:39
 * @author biaoping.yin
 * @version 1.0
 */
public class ES2PostgresDBFullDemo {
	public static void main(String[] args){
		ES2PostgresDBFullDemo esDemo = new ES2PostgresDBFullDemo();
		esDemo.scheduleScrollRefactorImportData();
		System.out.println("complete.");
	}



	public void scheduleScrollRefactorImportData(){
		ES2DBExportBuilder importBuilder = new ES2DBExportBuilder();
		importBuilder.setBatchSize(100).setFetchSize(500);


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// log_id和数据库对应的字段一致,就不需要设置setLastValueColumn信息，
		// 但是需要设置setLastValueType告诉工具增量字段的类型
//		importBuilder.setSqlName("insertSQLnew"); //指定将es文档数据同步到数据库的sql语句名称，配置在dsl2ndSqlFile.xml中
		DBConfigBuilder dbConfigBuilder = new DBConfigBuilder();
		dbConfigBuilder
				.setSqlFilepath("dsl2ndSqlFile.xml")

				.setTargetDbName("postgres")//指定目标数据库，在application.properties文件中配置
//				.setTargetDbDriver("com.mysql.cj.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
//				.setTargetDbUrl("jdbc:mysql://localhost:3306/bboss?useCursorFetch=true") //通过useCursorFetch=true启用mysql的游标fetch机制，否则会有严重的性能隐患，useCursorFetch必须和jdbcFetchSize参数配合使用，否则不会生效
//				.setTargetDbUser("root")
//				.setTargetDbPassword("123456")
//		        .setSql("INSERT INTO batchtest ( name, author, content, title, optime, oper, subtitle, collecttime,ipinfo)\n" +
//						"                VALUES ( ?,\n" +
//						"                         ?,\n" +
//						"                         ?,\n" +
//						"                         ?,\n" +
//						"                         ?,\n" +
//						"                         ?,\n" +
//						"                         ?,\n" +
//						"                         ?,\n" +
//						"                         ?)")
//				.setTargetValidateSQL("select 1")
//				.setTargetUsePool(true)//是否使用连接池
				.setInsertSqlName("insertSQLpostgresql")//指定新增的sql语句名称，在配置文件中配置：sql-dbtran.xml
//				.setUpdateSqlName("updateSql")//指定修改的sql语句名称，在配置文件中配置：sql-dbtran.xml
//				.setDeleteSqlName("deleteSql")//指定删除的sql语句名称，在配置文件中配置：sql-dbtran.xml
				/**
				 * 是否在批处理时，将insert、update、delete记录分组排序
				 * true：分组排序，先执行insert、在执行update、最后执行delete操作
				 * false：按照原始顺序执行db操作，默认值false
				 * @param optimize
				 * @return
				 */
//				.setOptimize(true);//指定查询源库的sql语句，在配置文件中配置：sql-dbtran.xml
		;
		/**
		dbConfigBuilder.setStatementHandler(new StatementHandler() { //重要每条文档记录交个这个回调函数处理
			@Override
			public void handler(PreparedStatement stmt, DBRecord dbRecord) throws SQLException {
				for (int i = 0; i < dbRecord.size(); i++) {
					Param param = dbRecord.get(i);
					if(param.getName().equals("collecttime") || param.getName().equals("logOpertime"))
						stmt.setTimestamp(param.getIndex(), new Timestamp(((Date) param.getValue()).getTime()));
					else{
						stmt.setObject(param.getIndex(), param.getValue());
					}
				}
			}
		});*/
		importBuilder.setOutputDBConfig(dbConfigBuilder.buildDBImportConfig());
		/**
		 * es相关配置
		 */
		importBuilder
				.setDsl2ndSqlFile("dsl2ndSqlFile.xml")
				.setDslName("fullscrollQuery")
				.setScrollLiveTime("10m")
//				.setSliceQuery(true)
//				.setSliceSize(5)
				.setQueryUrl("newdbdemo/_search")


				.setIncreamentEndOffset(5000);

//		//定时任务配置，
//		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
////					 .setScheduleDate(date) //指定任务开始执行时间：日期
//				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
//				.setPeriod(10000L); //每隔period毫秒执行，如果不设置，只执行一次
//		//定时任务配置结束

		//设置任务执行拦截器，可以添加多个
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
		});
//		//设置任务执行拦截器结束，可以添加多个
		//增量配置开始
//		importBuilder.setLastValueColumn("collecttime");//手动指定日期增量查询字段变量名称
//		importBuilder.setFromFirst(false);//setFromfirst(false)，如果作业停了，作业重启后从上次截止位置开始采集数据，
		//setFromfirst(true) 如果作业停了，作业重启后，重新开始采集数据
//		importBuilder.setLastValueStorePath("es2postgresdbdemo_import");//记录上次采集的增量字段值的文件路径，作为下次增量（或者重启后）采集数据的起点，不同的任务这个路径要不一样
//		importBuilder.setLastValueStoreTableName("logs");//记录上次采集的增量字段值的表，可以不指定，采用默认表名increament_tab
//		importBuilder.setLastValueType(ImportIncreamentConfig.TIMESTAMP_TYPE);//如果没有指定增量查询字段名称，则需要指定字段类型：ImportIncreamentConfig.NUMBER_TYPE 数字类型
		// 或者ImportIncreamentConfig.TIMESTAMP_TYPE 日期类型
		//指定日期增量字段日期格式，当增量字段为日期类型且日期格式不是默认的yyyy-MM-dd'T'HH:mm:ss.SSS'Z'时，需要设置字段相对应的日期格式，例如：yyyy-MM-dd HH:mm:ss
		//如果是默认utc格式yyyy-MM-dd'T'HH:mm:ss.SSS'Z'，则不需要手动设置指定
//		importBuilder.setLastValueDateformat("yyyy-MM-dd HH:mm:ss");
//		importBuilder.setLastValue(new Date());
		//增量配置结束

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
		importBuilder.addFieldValue("author","张无忌");
//		importBuilder.addFieldMapping("operModule","OPER_MODULE");
//		importBuilder.addFieldMapping("logContent","LOG_CONTENT");
//		importBuilder.addFieldMapping("logOperuser","LOG_OPERUSER");


		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception  {
				//可以根据条件定义是否丢弃当前记录
				//context.setDrop(true);return;
//				if(s.incrementAndGet() % 2 == 0) {
//					context.setDrop(true);
//					return;
//				}
				context.addFieldValue("testInt",2);
//				context.addFieldValue("id",context.getMetaValue("_id"));
				context.addFieldValue("id",SimpleStringUtil.getUUID());
				context.addFieldValue("author","duoduo");//将会覆盖全局设置的author变量
				context.addFieldValue("title","解放");
				context.addFieldValue("subtitle","小康");

//				context.addIgnoreFieldMapping("title");
				//上述三个属性已经放置到docInfo中，如果无需再放置到索引文档中，可以忽略掉这些属性
//				context.addIgnoreFieldMapping("author");

//				//修改字段名称title为新名称newTitle，并且修改字段的值
//				context.newName2ndData("title","newTitle",(String)context.getValue("title")+" append new Value");
				context.addIgnoreFieldMapping("subtitle");
				/**
				 * 获取ip对应的运营商和区域信息
				 */
				Map ipInfo = (Map)context.getValue("ipInfo");
				if(ipInfo != null)
					context.addFieldValue("ipinfo", SimpleStringUtil.object2json(ipInfo));
				else{
					context.addFieldValue("ipinfo", "");
				}
				DateFormat dateFormat = SerialUtil.getDateFormateMeta().toDateFormat();
				Date optime = context.getDateValue("logOpertime",dateFormat);
				Date collecttime = context.getDateValue("collecttime",dateFormat);
				context.addFieldValue("logOpertime",optime);
				context.addFieldValue("collecttime",collecttime);

				/**
				 //关联查询数据,单值查询
				 Map headdata = SQLExecutor.queryObjectWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
				 "select * from head where billid = ? and othercondition= ?",
				 context.getIntegerValue("billid"),"otherconditionvalue");//多个条件用逗号分隔追加
				 //将headdata中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
				 context.addFieldValue("headdata",headdata);
				 //关联查询数据,多值查询
				 List<Map> facedatas = SQLExecutor.queryListWithDBName(Map.class,context.getEsjdbc().getDbConfig().getDbName(),
				 "select * from facedata where billid = ?",
				 context.getIntegerValue("billid"));
				 //将facedatas中的数据,调用addFieldValue方法将数据加入当前es文档，具体如何构建文档数据结构根据需求定
				 context.addFieldValue("facedatas",facedatas);
				 */
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
//		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false，不打印响应报文将大大提升性能，只有在调试需要的时候才打开，log日志级别同时要设置为INFO
//		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认true，如果不需要响应报文将大大提升处理速度
		importBuilder.setPrintTaskLog(true);
		importBuilder.setDebugResponse(false);//设置是否将每次处理的reponse打印到日志文件中，默认false
		importBuilder.setDiscardBulkResponse(true);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false

		/**
		 * 执行es数据导入数据库表操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
	}
}
