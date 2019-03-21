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

import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.util.SQLUtil;
import org.frameworkset.spi.assemble.PropertiesContainer;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/20 22:36
 * @author biaoping.yin
 * @version 1.0
 */
public class DBTest {
	public static void main(String[] args) throws SQLException {
		PropertiesContainer propertiesContainer = new PropertiesContainer();
		propertiesContainer.addConfigPropertiesFile("application.properties");
		String dbName  = propertiesContainer.getProperty("db.name");
		String dbUser  = propertiesContainer.getProperty("db.user");
		String dbPassword  = propertiesContainer.getProperty("db.password");
		String dbDriver  = propertiesContainer.getProperty("db.driver");
		String dbUrl  = propertiesContainer.getProperty("db.url");


		String validateSQL  = propertiesContainer.getProperty("db.validateSQL");


		String _jdbcFetchSize = propertiesContainer.getProperty("db.jdbcFetchSize");
		Integer jdbcFetchSize = null;
		if(_jdbcFetchSize != null && !_jdbcFetchSize.equals(""))
			jdbcFetchSize  = Integer.parseInt(_jdbcFetchSize);

		//启动数据源
		SQLUtil.startPool(dbName,//数据源名称
				dbDriver,//mysql驱动
				dbUrl,//mysql链接串
				dbUser,dbPassword,//数据库账号和口令
				validateSQL, //数据库连接校验sql
				jdbcFetchSize // jdbcFetchSize
		);
		List<Map> datas = SQLExecutor.queryList(Map.class,"select * from td_cms_document id = ?",0);
		System.out.println();
	}
}
