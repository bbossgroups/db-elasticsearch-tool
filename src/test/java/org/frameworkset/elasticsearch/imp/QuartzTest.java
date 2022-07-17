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

import org.frameworkset.task.SchedulejobInfo;
import org.frameworkset.task.TaskService;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/5 12:46
 * @author biaoping.yin
 * @version 1.0
 */
public class QuartzTest {
	public static void main(String[] args){
		TaskService taskService = TaskService.getTaskService();
		taskService.startService();

		SchedulejobInfo jobinfo = new SchedulejobInfo();
		jobinfo.setName("QuartzImportTask");
		jobinfo.setId("QuartzImportTask");

		jobinfo.setUsed(true);
//		jobinfo.setBeanName(jobPro.getStringExtendAttribute("bean-name"));
		QuartzImportTask quartzImportTask = new org.frameworkset.elasticsearch.imp.QuartzImportTask();
		quartzImportTask.init();
		jobinfo.setBean(quartzImportTask);
		jobinfo.setMethod("execute");
//		jobinfo.setMethodConstruction(jobPro.getConstruction());
		jobinfo.setShouldRecover(false);
		jobinfo.setCronb_time("*/20 * * * * ?");
		//添加作业参数
		jobinfo.addParameter("jobid", "xxxxxxx");
		taskService.startExecuteJob(jobinfo);
//		setParameters(jobPro, jobinfo);
	}
}
