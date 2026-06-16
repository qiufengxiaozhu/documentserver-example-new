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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库配置类，用于SQLite数据库初始化
 */
@Slf4j
@Configuration
@Order(1)
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final DataSource dataSource;

    private static final String OPENAPI_TASK_DDL = "CREATE TABLE IF NOT EXISTS openapi_task (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "task_id VARCHAR(128) NOT NULL UNIQUE, " +
            "api_name VARCHAR(100) NOT NULL DEFAULT 'content/update', " +
            "doc_id VARCHAR(255) NOT NULL, " +
            "doc_name VARCHAR(255) NOT NULL, " +
            "op_type VARCHAR(50) NOT NULL, " +
            "request_body TEXT, " +
            "status VARCHAR(20) DEFAULT 'IN_QUEUE', " +
            "content_id VARCHAR(255), " +
            "result_filename VARCHAR(255), " +
            "result_doc_id VARCHAR(255), " +
            "error_msg TEXT, " +
            "server_response TEXT, " +
            "user_id VARCHAR(36), " +
            "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 初始化SQLite数据库目录和表结构
     */
    @PostConstruct
    public void initDatabase() {
        try {
            if (datasourceUrl.startsWith("jdbc:sqlite:")) {
                String dbPath = datasourceUrl.substring("jdbc:sqlite:".length());
                File dbFile = new File(dbPath);
                File parentDir = dbFile.getParentFile();

                if (parentDir != null && !parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        log.info("创建SQLite数据库目录: {}", parentDir.getAbsolutePath());
                    }
                }

                log.info("SQLite数据库文件路径: {}", dbFile.getAbsolutePath());

                if (!dbFile.exists() || dbFile.length() == 0) {
                    initializeDatabase();
                } else {
                    if (!isTableExists()) {
                        initializeDatabase();
                    }
                    ensureOpenApiTaskTable();
                }
            }
        } catch (Exception e) {
            log.error("初始化SQLite数据库失败", e);
        }
    }

    /**
     * 确保 openapi_task 表存在
     * 如果旧的 watermark_task 表存在，进行数据迁移
     */
    private void ensureOpenApiTaskTable() {
        boolean openApiTableExists = tableExists("openapi_task");
        boolean watermarkTableExists = tableExists("watermark_task");

        if (!openApiTableExists) {
            log.info("openapi_task 表不存在，开始创建");
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(OPENAPI_TASK_DDL);
                log.info("openapi_task 表创建成功");

                if (watermarkTableExists) {
                    migrateFromWatermarkTask(statement);
                }
            } catch (Exception e) {
                log.error("创建 openapi_task 表失败", e);
            }
        } else {
            ensureColumn("openapi_task", "result_doc_id", "VARCHAR(255)");
        }
    }

    /**
     * 从旧的 watermark_task 表迁移数据到 openapi_task
     */
    private void migrateFromWatermarkTask(Statement statement) {
        try {
            statement.execute("INSERT INTO openapi_task " +
                    "(task_id, api_name, doc_id, doc_name, op_type, request_body, status, " +
                    "content_id, result_filename, error_msg, user_id, create_time, update_time) " +
                    "SELECT task_id, 'content/update', doc_id, doc_name, watermark_type, params_json, status, " +
                    "content_id, result_filename, error_msg, user_id, create_time, update_time " +
                    "FROM watermark_task");
            log.info("watermark_task 数据迁移到 openapi_task 完成");
        } catch (Exception e) {
            log.warn("迁移 watermark_task 数据失败（可能无数据）: {}", e.getMessage());
        }
    }

    /**
     * 确保指定表中存在某列，不存在则自动添加
     */
    private void ensureColumn(String tableName, String columnName, String columnType) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return;
                }
            }
            String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
            statement.execute(sql);
            log.info("为 {} 表添加 {} 列", tableName, columnName);
        } catch (Exception e) {
            log.warn("添加列 {}.{} 失败: {}", tableName, columnName, e.getMessage());
        }
    }

    /**
     * 检查指定表是否存在
     */
    private boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查表是否存在（兼容旧逻辑）
     */
    private boolean isTableExists() {
        return tableExists("sys_user");
    }

    /**
     * 初始化数据库表结构和数据
     */
    private void initializeDatabase() {
        log.info("开始初始化SQLite数据库表结构和数据");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            executeScript(statement, "sql/ddl.sql");
            executeScript(statement, "sql/dml.sql");
            log.info("SQLite数据库初始化完成");
        } catch (Exception e) {
            log.error("初始化数据库脚本执行失败", e);
        }
    }

    /**
     * 执行SQL脚本
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
                        log.debug("执行SQL: {}", sqlStatement);
                    }
                    sql.setLength(0);
                }
            }
        }
    }
}
