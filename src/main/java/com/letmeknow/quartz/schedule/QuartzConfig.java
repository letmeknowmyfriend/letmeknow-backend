package com.letmeknow.quartz.schedule;

import com.letmeknow.analyser.Analyser;
import com.letmeknow.enumstorage.SpringProfile;
import com.letmeknow.quartz.AutoWiringSpringBeanJobFactory;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {
    private final ApplicationContext applicationContext;
    private final PlatformTransactionManager transactionManager;

    @Value("${spring.profiles.active}")
    private String activeProfile;

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
        TriggerBuilder<Trigger> triggerTriggerBuilder = newTrigger();
        triggerTriggerBuilder
                .withIdentity("trigger1", "group1")
                .forJob(jobDetail());

        // Local Profile은 10초 후에 1번만 실행
        if (activeProfile.equals(SpringProfile.LOCAL.getProfile())) {
            triggerTriggerBuilder
                .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.SECOND)) // 10초 후에 실행
                .withSchedule(simpleSchedule()
                .withRepeatCount(0)); // 1번만 실행
        }
        // Prod Profile은 10초 후에 시작해서, 1시간마다 실행
        else if (activeProfile.equals(SpringProfile.PROD.getProfile())) {
            triggerTriggerBuilder
                .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.SECOND)) // 10초 후에 실행
                .withSchedule(simpleSchedule()
                .withIntervalInHours(1)
                .repeatForever());
        }

        return triggerTriggerBuilder.build();
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
