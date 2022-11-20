package de.schoenebaum.budgetbook.backup;

import java.io.File;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupService {

	private static final String JDBC_H2_FILE_PREFIX = "jdbc:h2:file:";
	private static final String BACKUP_FILE_NAME_TEMPLATE = "db-backup-%d.zip";
	private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);

	private final File backupFolder;
	private final EntityManager entityManager;

	@Autowired
	public BackupService(DataSourceProperties dataSourceProperties, EntityManager entityManager) {
		this.entityManager = entityManager;
		String dbUrl = dataSourceProperties.getUrl();
		if (dbUrl.startsWith(JDBC_H2_FILE_PREFIX)) {
			File dbFile = new File(dbUrl.substring(JDBC_H2_FILE_PREFIX.length()));
			backupFolder = dbFile.getParentFile();
			if (backupFolder == null) {
				throw new IllegalStateException("Can not get folder path of database");
			}
		} else {
			backupFolder = null;
			LOG.info("Can not extract backup folder from jdbc connection string");
		}

	}

	@Scheduled(cron = "0 10 0 * * *")
	@Transactional
	public void backupDatabase() {
		if (backupFolder != null) {
			int dayIndex = LocalDate.now()
				.getDayOfWeek()
				.minus(1)
				.getValue();
			String backupFileName = String.format(BACKUP_FILE_NAME_TEMPLATE, dayIndex);
			String backupFilePath = backupFolder.getPath() + "/" + backupFileName;

			try {
				Query query = entityManager.createNativeQuery("BACKUP TO :filePath");
				query.setParameter("filePath", backupFilePath);
				query.executeUpdate();
				LOG.info("Successfully created backup of database with path {}", backupFilePath);
			} catch (Exception e) {
				LOG.error("Could not backup database to path {}", backupFilePath, e);
			}
		}
	}

}
