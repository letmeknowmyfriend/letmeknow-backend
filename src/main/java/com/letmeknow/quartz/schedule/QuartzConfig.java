package com.letmeknow.quartz.schedule;

import com.letmeknow.quartz.AutoWiringSpringBeanJobFactory;
import com.letmeknow.quartz.analyser.Analyser;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Properties;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
public class QuartzConfig {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setQuartzProperties(quartzProperties());
        schedulerFactoryBean.setTriggers(trigger());
        schedulerFactoryBean.setJobFactory(springBeanJobFactory());
        schedulerFactoryBean.setJobDetails(jobDetail());

        schedulerFactoryBean.setApplicationContext(applicationContext);

        schedulerFactoryBean.setTransactionManager(transactionManager);

        schedulerFactoryBean.setSchedulerName("crawlerScheduler");
        schedulerFactoryBean.start();

        return schedulerFactoryBean;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        return jobFactory;
    }

    @Bean
    public JobDetail jobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setApplicationContext(applicationContext);

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("name", "crawling");
        jobDetailFactoryBean.setJobDataMap(jobDataMap);
        jobDetailFactoryBean.setName("General Notice");
        jobDetailFactoryBean.setGroup("crawling");

        jobDetailFactoryBean.setJobClass(Analyser.class);
        jobDetailFactoryBean.setDurability(true);



        jobDetailFactoryBean.afterPropertiesSet();

        return jobDetailFactoryBean.getObject();
    }

    @Bean
    public Trigger trigger() {
        // Trigger the job to run now, and then repeat every 40 seconds
//        return newTrigger()
//                .withIdentity("trigger1", "group1")
//                .withSchedule(cronSchedule("30 10-17 * * *")) // 매일 10시 30분부터 17시 30분까지 1시간마다 실행
//                .forJob("job1", "group1")
//                .build();
        return newTrigger()
                .withIdentity("trigger1", "group1")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(1)
                        .withRepeatCount(0))
                .forJob(jobDetail())
                .build();
    }

//    @Bean
//    public Scheduler crawlingScheduler() throws SchedulerException {
//        Scheduler scheduler = schedulerFactoryBean();
////        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
////        stdSchedulerFactory.initialize(quartzProperties());
////        Scheduler scheduler = stdSchedulerFactory.getScheduler();
////        scheduler.scheduleJob(job(), trigger());
//        scheduler.start();
//        return scheduler;
//    }

    @Bean
    public Properties quartzProperties() {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(new ClassPathResource("quartz.yml"));
        yamlPropertiesFactoryBean.afterPropertiesSet();

        return yamlPropertiesFactoryBean.getObject();
    }
}
