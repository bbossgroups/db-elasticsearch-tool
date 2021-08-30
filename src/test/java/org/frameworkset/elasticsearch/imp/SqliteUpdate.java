package org.frameworkset.elasticsearch.imp;
/**
 * Copyright 2020 bboss
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

import java.sql.SQLException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2021/8/27 16:37
 * @author biaoping.yin
 * @version 1.0
 */
public class SqliteUpdate {
	public static void main(String[] args) throws SQLException {
		SQLUtil.startPool("test",//数据源名称
				"org.sqlite.JDBC",//oracle驱动
				"jdbc:sqlite:E:\\gatewayoom\\filelog\\filelog_importbak" ,//mysql链接串
				"root","123456",//数据库账号和口令
				"select 1 " //数据库连接校验sql
		);
		SQLExecutor.updateWithDBName("test","update increament_tab set status = 0");
	}
}
