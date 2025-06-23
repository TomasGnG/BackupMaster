package de.tomasgng.scheduling;

import de.tomasgng.BackupMasterPlugin;
import org.bukkit.Bukkit;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class BackupJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        BackupMasterPlugin.getPlugin().getBackupManager().backupWorlds(Bukkit.getConsoleSender());
    }
}
