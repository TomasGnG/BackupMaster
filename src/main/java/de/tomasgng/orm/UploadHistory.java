package de.tomasgng.orm;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class UploadHistory {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    public Date creationDate;

    @DatabaseField
    public String backupCreator;

    @DatabaseField
    public String link;

    public UploadHistory() {
    }

    public UploadHistory(Date creationDate, String backupCreator, String link) {
        this.creationDate = creationDate;
        this.backupCreator = backupCreator;
        this.link = link;
    }
}
