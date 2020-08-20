package org.frameworkset.elasticsearch.imp.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * @author MECHREV
 */
public class Bootstrap {
    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    //2020.8.19.controller里面每一次来新的任务表,都直接调用了quartz.传入参数通过
//    @RequestMapping(value = "quartz")
//    public void sql2sql() throws SchedulerException, InterruptedException {
//        bootstrap.quartz(scheduler);
//    }
    public  void quartz(Scheduler scheduler) throws InterruptedException, SchedulerException {
        //2020.8.19在这定义加载的任务类,然后业务逻辑是每一次都在
        JobDetail jobDetail = JobBuilder.newJob(ImportDataJob.class)

                .withIdentity("test1", "group1").build();
        // 3、构建Trigger实例,每隔1s执行一次
        Trigger trigger = newTrigger()
                .withIdentity("test1", "group1")
                .withSchedule(cronSchedule("0/30 * * * * ? "))
                .forJob("test1", "group1")
                .build();
        //4、执行
        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println("--------scheduler start ! ------------");
        scheduler.start();
        //睡眠
//        TimeUnit.MINUTES.sleep(2);
//        scheduler.shutdown();
        System.out.println("--------scheduler shutdown ! ------------");
    }
}

