package com.filez.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Database configuration class for SQLite database initialization
 */
@Slf4j
@Configuration
@Order(1)
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final DataSource dataSource;

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Initialize SQLite database directory and table structure
     */
    @PostConstruct
    public void initDatabase() {
        try {
            // Extract file path from jdbc:sqlite:./data/filez_demo.db
            if (datasourceUrl.startsWith("jdbc:sqlite:")) {
                String dbPath = datasourceUrl.substring("jdbc:sqlite:".length());
                File dbFile = new File(dbPath);
                File parentDir = dbFile.getParentFile();
                
                // Ensure the parent directory of the database file exists
                if (parentDir != null && !parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        log.info("Created SQLite database directory: {}", parentDir.getAbsolutePath());
                    }
                }
                
                log.info("SQLite database file path: {}", dbFile.getAbsolutePath());
                
                // Check if database initialization is needed
                if (!dbFile.exists() || dbFile.length() == 0) {
                    initializeDatabase();
                } else {
                    // Check if tables exist
                    if (!isTableExists()) {
                        initializeDatabase();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize SQLite database", e);
        }
    }

    /**
     * Check if tables exist
     */
    private boolean isTableExists() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='sys_user'");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initialize database table structure and data
     */
    private void initializeDatabase() {
        log.info("Starting to initialize SQLite database table structure and data");
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Execute DDL script
            executeScript(statement, "sql/ddl.sql");
            
            // Execute DML script  
            executeScript(statement, "sql/dml.sql");
            
            log.info("SQLite database initialization completed");
        } catch (Exception e) {
            log.error("Failed to execute database initialization scripts", e);
        }
    }

    /**
     * Execute SQL script
     */
    private void executeScript(Statement statement, String scriptPath) throws Exception {
        Resource resource = new ClassPathResource(scriptPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                sql.append(line).append(" ");
                
                if (line.endsWith(";")) {
                    String sqlStatement = sql.toString().trim();
                    if (!sqlStatement.isEmpty()) {
                        statement.execute(sqlStatement);
                        log.debug("Executing SQL: {}", sqlStatement);
                    }
                    sql.setLength(0);
                }
            }
        }
    }
}
