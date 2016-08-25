package com.wsn.board.bs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	private static final Logger logger = LoggerFactory.getLogger(Config.class.getSimpleName());

	private static final String CONF_PROPS_NAME = "conf/conf.properties";
	private static Config conf = null;
	private Properties properties;

	private void init() throws IOException {
		properties = new Properties();
		FileInputStream fis = new FileInputStream(new File(CONF_PROPS_NAME));
		properties.load(fis);
	}

	public static Properties getProperties() throws IOException {
		if (conf == null) {
			conf = new Config();
			conf.init();
		}
		return conf.properties;
	}

}
