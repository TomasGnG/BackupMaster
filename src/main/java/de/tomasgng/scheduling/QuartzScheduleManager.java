package de.tomasgng.scheduling;

import de.tomasgng.config.dataproviders.ConfigDataProvider;
import org.bukkit.Bukkit;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class QuartzScheduleManager {

    private final Logger logger = Bukkit.getLogger();
    private final ConfigDataProvider configDataProvider = new ConfigDataProvider();
    private Scheduler scheduler;

    public QuartzScheduleManager() {
        initializeScheduler();
    }

    private void initializeScheduler() {
        try {
            logger.info("Started quartz scheduler initialization...");
            scheduler = new StdSchedulerFactory().getScheduler();

            JobDetail jobDetail = JobBuilder.newJob(BackupJob.class)
                                            .withIdentity("backupJob")
                                            .build();

            List<Trigger> triggers = configDataProvider.getCronTriggers();

            if(triggers.isEmpty()) {
                logger.warning("No cron triggers found in config.yml! No backups will be scheduled.");
                return;
            }

            scheduler.scheduleJob(jobDetail, new HashSet<>(triggers), true);
            scheduler.start();
            logger.info("Quartz scheduler initialized!");
        } catch (SchedulerException e) {
            logger.severe("Couldn't initialize quartz scheduler! Cause: " + e.getMessage());
        }
    }

    public void onDisable() {
        if(scheduler != null) {
            try {
                scheduler.shutdown(true);
            } catch (org.quartz.SchedulerException e) {
                logger.severe("Couldn't shutdown quartz scheduler! Cause: " + e.getMessage());
            }

            scheduler = null;
        }
    }

}
