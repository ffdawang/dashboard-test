package com.wsn.board.bs.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsn.board.bs.util.Config;

public class MySqlConnectionProvider implements ConnectionProvider {

	private static Logger logger = LoggerFactory.getLogger(MySqlConnectionProvider.class.getSimpleName());
	// 数据库驱动名称
	private static String driver = "com.mysql.jdbc.Driver";
	// 数据库连接地址
	private static String serverURL;
	// 数据库用户名
	private static String username;
	// 数据库密码
	private static String password;
	// 连接池初始化大小
	private static int initialSize = 10;
	// 连接池最大连接数量
	private static int maxPoolSize = 100;
	// 最小逐出时间，100秒
	private static int maxIdle = 100;
	// 最小空闲连接
	private static int minIdle = 4;
	// 连接失败重试次数
	// private static int retryAttempts = 10;
	// 当连接池连接耗尽时获取连接数
	// private static int acquireIncrement = 5;
	// Tomcat Jdbc Pool数据源
	private static Map<String, DataSource> tomcatDataSources = getTomcatDataSources();
	// 查询次数
	private static int count = 100;
	
	/**
	 * 获取Apache tomcat jdbc pool数据源
	 * 
	 * @return
	 */
	private static Map<String, DataSource> getTomcatDataSources() {
		Map<String, DataSource> tomcatDataSources = new ConcurrentHashMap<>();
		try {
			for (int dataSourceIndex = 1; dataSourceIndex <= 5; dataSourceIndex++) {
				loadProperties(dataSourceIndex);
				if (StringUtils.isBlank(serverURL) || 
						StringUtils.isBlank(username) || StringUtils.isBlank(password))
					continue;
				DataSource ds = new DataSource();
				ds.setUrl(serverURL);
				ds.setUsername(username);
				ds.setPassword(password);
				ds.setDriverClassName(driver);

				ds.setInitialSize(initialSize);
				ds.setMaxActive(maxPoolSize);
				ds.setMaxIdle(maxIdle);
				ds.setMinIdle(minIdle);

				ds.setTimeBetweenEvictionRunsMillis(30000);
				ds.setMinEvictableIdleTimeMillis(60000);
				ds.setTestWhileIdle(true);
				ds.setValidationQuery("SELECT 1");

				ds.setTestOnBorrow(true);
				ds.setTestOnConnect(false);
				ds.setTestOnReturn(false);
				ds.setValidationInterval(30000);
				tomcatDataSources.put(String.valueOf(dataSourceIndex), ds);
			}
		} catch (IOException e) {
			logger.error("Build datasource failed", e);
		}
		return tomcatDataSources;
	}

	@Override
	public boolean isPooled() {
		return true;
	}

	@Override
	public Connection getConnection(int index) throws SQLException {
		DataSource ds = tomcatDataSources.get(String.valueOf(index));
		if (ds == null)
			return null;
		return ds.getConnection();
	}

	@Override
	public void start() {}

	@Override
	public void restart() {}

	@Override
	public void destroy() {}

	/**
	 * Load properties that already exist from Jive properties.
	 * 
	 * @throws IOException
	 */
	private static void loadProperties(int index) throws IOException {
		driver = Config.getProperties().getProperty("database.defaultProvider.driver." + index);
		serverURL = Config.getProperties().getProperty("database.defaultProvider.serverURL." + index);
		username = Config.getProperties().getProperty("database.defaultProvider.username." + index);
		password = Config.getProperties().getProperty("database.defaultProvider.password." + index);

		String confInitialSize = Config.getProperties().getProperty("database.defaultProvider.initConnections." + index);
		String confMaxPoolSize = Config.getProperties().getProperty("database.defaultProvider.maxConnections." + index);
		String confMaxIdle = Config.getProperties().getProperty("database.defaultProvider.maxIdle." + index);
		String confMinIdle = Config.getProperties().getProperty("database.defaultProvider.minIdle." + index);
//		String confRetryAttempts = Config.getProperties().getProperty("database.defaultProvider.retryAttempts." + index);
//		String confAcquireIncrement = Config.getProperties().getProperty("database.defaultProvider.acquireIncrement." + index);

		if (confInitialSize != null)
			initialSize = Integer.parseInt(confInitialSize);
		if (confMaxPoolSize != null)
			maxPoolSize = Integer.parseInt(confMaxPoolSize);
		if (confMaxIdle != null)
			maxIdle = Integer.parseInt(confMaxIdle);
		if (confMinIdle != null)
			minIdle = Integer.parseInt(confMinIdle);
//		if (confRetryAttempts != null) {
//			retryAttempts = Integer.parseInt(confRetryAttempts);
//		}
//		if (confAcquireIncrement != null) {
//			acquireIncrement = Integer.parseInt(confAcquireIncrement);
//		}
	}

}
