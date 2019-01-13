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

import com.frameworkset.common.poolman.BatchHandler;
import com.frameworkset.common.poolman.ConfigSQLExecutor;
import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.util.SQLUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.DataStream;
import org.frameworkset.elasticsearch.client.ExportBuilder;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: 从es中查询数据导入数据库案例</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 14:39
 * @author biaoping.yin
 * @version 1.0
 */
public class ESDemo {
	/**
	 * 采用原始方法导入数据
	 */
	public void directExport()  {

		//启动数据源
		SQLUtil.startPool("test",//数据源名称
				"com.mysql.jdbc.Driver",//mysql驱动
				"jdbc:mysql://localhost:3306/bboss?useCursorFetch=true",//mysql链接串
				"root","123456",//数据库账号和口令
				"select 1 ", //数据库连接校验sql
				10000 // jdbcFetchSize
		);
		//scroll分页检索
		final int batchSize = 5000;

		final String sql = "insert into batchtest (name) values(?)";
		String url = "dbdemo/_search";
		String dslName = "scrollQuery";
		final String sqlName = "insertSQL";
		String dsl2ndSqlFile = "dsl2ndSqlFile.xml";
		String scrollLiveTime = "100m";
		Map params = new HashMap();
		params.put("size", batchSize);//每页5000条记录
		final BatchHandler<Map> batchHandler = new BatchHandler<Map>() {
			@Override
			public void handler(PreparedStatement stmt, Map esrecord, int i) throws SQLException {
				stmt.setString(1, (String) esrecord.get("logContent"));
			}
		};

		//清空数据
		try {
			SQLExecutor.delete("delete from batchtest");
		}
		catch (Exception e){

		}
		//采用自定义handler函数处理每个scroll的结果集后，response中只会包含总记录数，不会包含记录集合
		//scroll上下文有效期1分钟；大数据量时可以采用handler函数来处理每次scroll检索的结果，规避数据量大时存在的oom内存溢出风险
		final ConfigSQLExecutor configSQLExecutor = new ConfigSQLExecutor(dsl2ndSqlFile);
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil(dsl2ndSqlFile);
		ESDatas<Map> response = clientUtil.scroll(url, dslName, scrollLiveTime, params, Map.class, new ScrollHandler<Map>() {
			public void handle(ESDatas<Map> response) throws Exception {//自己处理每次scroll的结果
				List<Map> datas = response.getDatas();
				long totalSize = response.getTotalSize();
				System.out.println("totalSize:"+totalSize+",datas.size:"+datas.size());
				if(sql == null) {
					configSQLExecutor.executeBatch(sqlName, datas, batchSize, batchHandler);
				}
				else{
					SQLExecutor.executeBatch(sql, datas, batchSize, batchHandler);
				}
			}
		});

	}

	/**
	 * 串行导入
	 */
	public void exportData(){

		ExportBuilder exportBuilder = new ExportBuilder();
		exportBuilder.setBatchSize(5000)
				     .setInsertBatchSize(5000)
					 .setDsl2ndSqlFile("dsl2ndSqlFile.xml")
				     .setSqlName("insertSQL")
					 .setDslName("scrollQuery")
					 .setScrollLiveTime("10m")
//					 .setSliceQuery(true)
//				     .setSliceSize(5)
					 .setQueryUrl("dbdemo/_search")
//				     .setPrintTaskLog(true)
					 .setBatchHandler(new BatchHandler<Map>() {
							@Override
							public void handler(PreparedStatement stmt, Map esrecord, int i) throws SQLException {
								stmt.setString(1, (String) esrecord.get("logContent"));
							}
						})
//				//添加dsl中需要用到的参数及参数值
//				.addParam("var1","v1")
//				.addParam("var2","v2")
//				.addParam("var3","v3")
		;

		DataStream dataStream = exportBuilder.builder();
		dataStream.execute();
	}

	/**
	 * 并行导入
	 */

	public void exportSliceData(){

		ExportBuilder exportBuilder = new ExportBuilder();
		exportBuilder.setBatchSize(5000)
				.setInsertBatchSize(5000)
				.setDsl2ndSqlFile("dsl2ndSqlFile.xml")
				.setSqlName("insertSQL")
				.setDslName("scrollSliceQuery")
				.setScrollLiveTime("10m")
					 .setSliceQuery(true)
				     .setSliceSize(5)
				.setQueryUrl("dbdemo/_search")
//				     .setPrintTaskLog(true)
				.setBatchHandler(new BatchHandler<Map>() {
					@Override
					public void handler(PreparedStatement stmt, Map esrecord, int i) throws SQLException {
						stmt.setString(1, (String) esrecord.get("logContent"));
					}
				})
//				//添加dsl中需要用到的参数及参数值
//				.addParam("var1","v1")
//				.addParam("var2","v2")
//				.addParam("var3","v3")
				;

		DataStream dataStream = exportBuilder.builder();
		dataStream.execute();
	}

	/**
	 * 在代码中写sql导入
	 */
	public void exportDataUseSQL(){
		ExportBuilder exportBuilder = new ExportBuilder();
		exportBuilder.setBatchSize(5000)
				.setInsertBatchSize(5000)
				.setDsl2ndSqlFile("dsl2ndSqlFile.xml")
				.setSql("insert into batchtest (name) values(?)")
				.setDslName("scrollQuery")
				.setScrollLiveTime("10m")
				.setSliceQuery(true)
				.setSliceSize(5)
				.setQueryUrl("dbdemo/_search")
				.setBatchHandler(new BatchHandler<Map>() {
					@Override
					public void handler(PreparedStatement stmt, Map esrecord, int i) throws SQLException {
						stmt.setString(1, (String) esrecord.get("logContent"));
					}
				})
//				//添加dsl中需要用到的参数及参数值
//				.addParam("var1","v1")
//				.addParam("var2","v2")
//				.addParam("var3","v3")
		;

		DataStream dataStream = exportBuilder.builder();
		dataStream.execute();
	}
}
