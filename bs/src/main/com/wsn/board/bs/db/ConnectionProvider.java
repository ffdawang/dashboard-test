package com.wsn.board.bs.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {

	public boolean isPooled();

	public Connection getConnection(int index) throws SQLException;

	public void start();

	public void restart();

	public void destroy();
}