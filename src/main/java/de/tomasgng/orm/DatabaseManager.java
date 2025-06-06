package de.tomasgng.orm;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseManager {

    public static DatabaseManager instance = new DatabaseManager();

    private ConnectionSource connectionSource;

    private DatabaseManager() {
        new File("plugins/BackupMaster/").mkdirs();
        setupOrmLite();
    }

    private void setupOrmLite() {
        try {
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:plugins/BackupMaster/data.db");

            TableUtils.createTableIfNotExists(connectionSource, UploadHistory.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createUploadHistory(Date creationDate, String backupCreatorName, String link) {
        try {
            Dao<UploadHistory, Long> dao = DaoManager.createDao(connectionSource, UploadHistory.class);

            dao.create(new UploadHistory(creationDate, backupCreatorName, link));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UploadHistory> getUploadHistoryList(long limit) {
        try {
            Dao<UploadHistory, Long> dao = DaoManager.createDao(connectionSource, UploadHistory.class);

            return dao.queryBuilder().orderBy("creationDate", false).limit(limit).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
