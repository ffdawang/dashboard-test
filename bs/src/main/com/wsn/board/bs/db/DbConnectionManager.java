package com.wsn.board.bs.db;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wsn.board.bs.db.MetaData.DatabaseType;
import com.wsn.board.bs.util.ClassUtils;
import com.wsn.board.bs.util.Config;
import com.wsn.board.bs.util.Log;

public class DbConnectionManager {

	private static Map<String, ConnectionProvider> connectionProviders = new ConcurrentHashMap<>();
	private static Map<String, MetaData> metaDatas = new ConcurrentHashMap<>();
	private static final Object providerLock = new Object();

	/**
	 * Returns a database connection from the currently active connection
	 * provider. An exception will be thrown if no connection was found. (auto
	 * commit is set to true).
	 * @param index 
	 * @return a connection.
	 * @throws SQLException
	 *             if a SQL exception occurs or no connection was found.
	 */
	public static Connection getConnection(int index) throws SQLException {
		ConnectionProvider connectionProvider = connectionProviders.get(String.valueOf(index));
		if (connectionProvider == null) {
			synchronized (providerLock) {
				if (connectionProvider == null) {
					String className = null;
					try {
						className = Config.getProperties().getProperty("connectionProvider.className");
					} catch (IOException e) {
						Log.warn("Failed to get the connectionProvider property.", e);
					}
					if (className != null) {
						// Attempt to load the class.
						try {
							Class conClass = ClassUtils.forName(className);
							connectionProvider = setConnectionProvider((ConnectionProvider) conClass.newInstance(), index);
						} catch (Exception e) {
							Log.warn("Failed to create the " + "connection provider specified by connection"
									+ "Provider.className. Using the default pool.", e);
							connectionProvider = setConnectionProvider(new MySqlConnectionProvider(), index);
						}
					} else {
						connectionProvider = setConnectionProvider(new MySqlConnectionProvider(), index);
					}
				}
			}
		}

		// TODO: May want to make these settings configurable
		Integer retryCnt = 0;
		Integer retryMax = 3;
		Integer retryWait = 250; // milliseconds
		Connection con = null;
		SQLException lastException = null;
		do {
			try {
				con = connectionProvider.getConnection(index);
				if (con != null) {
					return con;
				}
			} catch (SQLException e) {
				// TODO distinguish recoverable from non-recoverable exceptions.
				lastException = e;
				Log.info("Unable to get a connection from the database pool " + "(attempt " + retryCnt + " out of "
						+ retryMax + ").", e);
			}
			try {
				Thread.sleep(retryWait);
			} catch (Exception e) {
				// Ignored
			}
			retryCnt++;
		} while (retryCnt <= retryMax);
		throw new SQLException("ConnectionManager.getConnection() " + "failed to obtain a connection after " + retryCnt
				+ " retries. " + "The exception from the last attempt is as follows: " + lastException);
	}

	/**
	 * Returns a Connection from the currently active connection provider that
	 * is ready to participate in transactions (auto commit is set to false).
	 * 
	 * @param index
	 * @return a connection with transactions enabled.
	 * @throws SQLException
	 *             if a SQL exception occurs.
	 */
	public static Connection getTransactionConnection(int index) throws SQLException {
		Connection con = getConnection(index);
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isTransactionsSupported()) {
			con.setAutoCommit(false);
		}
		return con;
	}

	/**
	 * Closes a PreparedStatement and Connection. However, it first rolls back
	 * the transaction or commits it depending on the value of
	 * <code>abortTransaction</code>.
	 * 
	 * @param pstmt
	 *            the prepared statement to close.
	 * @param con
	 *            the connection to close.
	 * @param abortTransaction
	 *            true if the transaction should be rolled back.
	 */
	public static void closeTransactionConnection(PreparedStatement pstmt, Connection con, boolean abortTransaction, int index) {
		closeStatement(pstmt);
		closeTransactionConnection(con, abortTransaction, index);
	}

	/**
	 * Closes a Connection. However, it first rolls back the transaction or
	 * commits it depending on the value of <code>abortTransaction</code>.
	 * 
	 * @param con
	 *            the connection to close.
	 * @param abortTransaction
	 *            true if the transaction should be rolled back.
	 */
	public static void closeTransactionConnection(Connection con, boolean abortTransaction, int index) {
		// Rollback or commit the transaction
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isTransactionsSupported()) {
			try {
				if (abortTransaction) {
					con.rollback();
				} else {
					con.commit();
				}
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
			// Reset the connection to auto-commit mode.
			try {
				con.setAutoCommit(true);
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
		closeConnection(con);
	}

	/**
	 * Closes a result set. This method should be called within the finally
	 * section of your database logic, as in the following example:
	 * 
	 * <pre>
	 *  public void doSomething(Connection con) {
	 *      ResultSet rs = null;
	 *      PreparedStatement pstmt = null;
	 *      try {
	 *          pstmt = con.prepareStatement("select * from blah");
	 *          rs = pstmt.executeQuery();
	 *          ....
	 *      }
	 *      catch (SQLException sqle) {
	 *          Log.error(sqle.getMessage(), sqle);
	 *      }
	 *      finally {
	 *          ConnectionManager.closeResultSet(rs);
	 *          ConnectionManager.closePreparedStatement(pstmt);
	 *      }
	 * }
	 * </pre>
	 * 
	 * @param rs
	 *            the result set to close.
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				Log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Closes a statement. This method should be called within the finally
	 * section of your database logic, as in the following example:
	 * 
	 * <pre>
	 *  public void doSomething(Connection con) {
	 *      PreparedStatement pstmt = null;
	 *      try {
	 *          pstmt = con.prepareStatement("select * from blah");
	 *          ....
	 *      }
	 *      catch (SQLException sqle) {
	 *          Log.error(sqle.getMessage(), sqle);
	 *      }
	 *      finally {
	 *          ConnectionManager.closeStatement(pstmt);
	 *      }
	 * }
	 * </pre>
	 * 
	 * @param stmt
	 *            the statement.
	 */
	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Closes a statement and a result set. This method should be called within
	 * the finally section of your database logic, as in the following example:
	 * 
	 * <pre>
	 *  public void doSomething(Connection con) {
	 *      PreparedStatement pstmt = null;
	 *      ResultSet rs = null;
	 *      try {
	 *          pstmt = con.prepareStatement("select * from blah");
	 *          rs = ...
	 *          ....
	 *      }
	 *      catch (SQLException sqle) {
	 *          Log.error(sqle.getMessage(), sqle);
	 *      }
	 *      finally {
	 *          ConnectionManager.closeStatement(rs, pstmt);
	 *      }
	 * }
	 * </pre>
	 * 
	 * @param stmt
	 *            the statement.
	 */
	public static void closeStatement(ResultSet rs, Statement stmt) {
		closeResultSet(rs);
		closeStatement(stmt);
	}

	/**
	 * Closes a statement. This method should be called within the try section
	 * of your database logic when you reuse a statement. It may throws an
	 * exception, so don't place it in the finally section.<br>
	 * Example:
	 * 
	 * <pre>
	 *  public void doSomething(Connection con) {
	 *      PreparedStatement pstmt = null;
	 *      try {
	 *          pstmt = con.prepareStatement("select * from dual");
	 *          pstmt.executeUpdate();
	 *          ...
	 *          <b>ConnectionManager.fastcloseStmt(pstmt);</b>
	 *          pstmt = con.prepareStatement("select * from blah");
	 *          ...
	 *      }
	 *      ...
	 * }
	 * </pre>
	 * 
	 * @param rs
	 *            the result set to close.
	 * @param stmt
	 *            the statement to close.
	 */
	public static void fastcloseStmt(PreparedStatement pstmt) throws SQLException {
		pstmt.close();
	}

	/**
	 * Closes a statement and a result set. This method should be called within
	 * the try section of your database logic when you reuse a statement. It may
	 * throw an exception, so don't place it in the finally section.<br>
	 * Example:
	 * 
	 * <pre>
	 *  public void doSomething(Connection con) {
	 *      PreparedStatement pstmt = null;
	 *      try {
	 *          pstmt = con.prepareStatement("select * from blah");
	 *          rs = pstmt.executeQuery();
	 *          ...
	 *          ConnectionManager.fastcloseStmt(rs, pstmt);
	 *          pstmt = con.prepareStatement("select * from blah");
	 *          ...
	 *      }
	 *      ...
	 * }
	 * </pre>
	 * 
	 * @param rs
	 *            the result set to close.
	 * @param stmt
	 *            the statement to close.
	 */
	public static void fastcloseStmt(ResultSet rs, PreparedStatement pstmt) throws SQLException {
		rs.close();
		pstmt.close();
	}

	/**
	 * Closes a result set, statement and database connection (returning the
	 * connection to the connection pool). This method should be called within
	 * the finally section of your database logic, as in the following example:
	 * 
	 * <pre>
	 * Connection con = null;
	 * PrepatedStatment pstmt = null;
	 * ResultSet rs = null;
	 * try {
	 *     con = ConnectionManager.getConnection();
	 *     pstmt = con.prepareStatement("select * from blah");
	 *     rs = psmt.executeQuery();
	 *     ....
	 * }
	 * catch (SQLException sqle) {
	 *     Log.error(sqle.getMessage(), sqle);
	 * }
	 * finally {
	 *     ConnectionManager.closeConnection(rs, pstmt, con);
	 * }
	 * </pre>
	 * 
	 * @param rs
	 *            the result set.
	 * @param stmt
	 *            the statement.
	 * @param con
	 *            the connection.
	 */
	public static void closeConnection(ResultSet rs, Statement stmt, Connection con) {
		closeResultSet(rs);
		closeStatement(stmt);
		closeConnection(con);
	}

	/**
	 * Closes a statement and database connection (returning the connection to
	 * the connection pool). This method should be called within the finally
	 * section of your database logic, as in the following example:
	 * <p/>
	 * 
	 * <pre>
	 * Connection con = null;
	 * PrepatedStatment pstmt = null;
	 * try {
	 *     con = ConnectionManager.getConnection();
	 *     pstmt = con.prepareStatement("select * from blah");
	 *     ....
	 * }
	 * catch (SQLException sqle) {
	 *     Log.error(sqle.getMessage(), sqle);
	 * }
	 * finally {
	 *     DbConnectionManager.closeConnection(pstmt, con);
	 * }
	 * </pre>
	 * 
	 * @param stmt
	 *            the statement.
	 * @param con
	 *            the connection.
	 */
	public static void closeConnection(Statement stmt, Connection con) {
		closeStatement(stmt);
		closeConnection(con);
	}

	/**
	 * Closes a database connection (returning the connection to the connection
	 * pool). Any statements associated with the connection should be closed
	 * before calling this method. This method should be called within the
	 * finally section of your database logic, as in the following example:
	 * <p/>
	 * 
	 * <pre>
	 * Connection con = null;
	 * try {
	 *     con = ConnectionManager.getConnection();
	 *     ....
	 * }
	 * catch (SQLException sqle) {
	 *     Log.error(sqle.getMessage(), sqle);
	 * }
	 * finally {
	 *     DbConnectionManager.closeConnection(con);
	 * }
	 * </pre>
	 * 
	 * @param con
	 *            the connection.
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Creates a scroll insensitive Statement if the JDBC driver supports it, or
	 * a normal Statement otherwise.
	 * 
	 * @param con
	 *            the database connection.
	 * @return a Statement
	 * @throws SQLException
	 *             if an error occurs.
	 */
	@Deprecated
	public static Statement createScrollableStatement(Connection con, int index) throws SQLException {
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isScrollResultsSupported()) {
			return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} else {
			return con.createStatement();
		}
	}

	/**
	 * Creates a scroll insensitive PreparedStatement if the JDBC driver
	 * supports it, or a normal PreparedStatement otherwise.
	 * 
	 * @param con
	 *            the database connection.
	 * @param sql
	 *            the SQL to create the PreparedStatement with.
	 * @return a PreparedStatement
	 * @throws java.sql.SQLException
	 *             if an error occurs.
	 */
	public static PreparedStatement createScrollablePreparedStatement(Connection con, String sql, int index) throws SQLException {
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isScrollResultsSupported()) {
			return con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} else {
			return con.prepareStatement(sql);
		}
	}

	/**
	 * Scrolls forward in a result set the specified number of rows. If the JDBC
	 * driver supports the feature, the cursor will be moved directly.
	 * Otherwise, we scroll through results one by one manually by calling
	 * <tt>rs.next()</tt>.
	 * 
	 * @param rs
	 *            the ResultSet object to scroll.
	 * @param rowNumber
	 *            the row number to scroll forward to.
	 * @throws SQLException
	 *             if an error occurs.
	 */
	public static void scrollResultSet(ResultSet rs, int rowNumber, int index) throws SQLException {
		// If the driver supports scrollable result sets, use that feature.
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isScrollResultsSupported()) {
			if (rowNumber > 0) {
				// We will attempt to do a relative fetch. This may fail in SQL
				// Server if
				// <resultset-navigation-strategy> is set to absolute. It would
				// need to be
				// set to looping to work correctly.
				// If so, manually scroll to the correct row.
				try {
					rs.setFetchDirection(ResultSet.FETCH_FORWARD);
					rs.relative(rowNumber);
				} catch (SQLException e) {
					// TODO change "Error ..." to "Disabling ..."
					Log.error("Error in JDBC method rs.relative(rowNumber).", e);
					// Log.error("Disabling JDBC method rs.relative(rowNumber).",
					// e);
					// scrollResultsSupported = false;
					for (int i = 0; i < rowNumber; i++) {
						rs.next();
					}
				}
			}
		}
		// Otherwise, manually scroll to the correct row.
		else {
			for (int i = 0; i < rowNumber; i++) {
				rs.next();
			}
		}
	}

	/**
	 * Limits the number of the results in a result set (to startIndex +
	 * numResults). Sets the fetch size depending on the features of the JDBC
	 * driver and make sure that the size is not bigger than 500.
	 * 
	 * @param pstmt
	 *            the PreparedStatement
	 * @param startIndex
	 *            the first row with interesting data
	 * @param numResults
	 *            the number of interesting results
	 */
	public static void limitRowsAndFetchSize(PreparedStatement pstmt, int startIndex, int numResults, int index) {
		MetaData metaData = metaDatas.get(String.valueOf(index));
		final int MAX_FETCHRESULTS = 500;
		final int maxRows = startIndex + numResults;
		setMaxRows(pstmt, maxRows, metaData);
		if (metaData != null && metaData.isPstmt_fetchSizeSupported()) {
			if (metaData.isScrollResultsSupported()) {
				setFetchSize(pstmt, Math.min(MAX_FETCHRESULTS, numResults), metaData);
			} else {
				setFetchSize(pstmt, Math.min(MAX_FETCHRESULTS, maxRows), metaData);
			}
		}
	}

	/**
	 * Sets the number of rows that the JDBC driver should buffer at a time. The
	 * operation is automatically bypassed if Openfire knows that the the JDBC
	 * driver or database doesn't support it.
	 * 
	 * @param pstmt
	 *            the PreparedStatement to set the fetch size for.
	 * @param fetchSize
	 *            the fetchSize.
	 */
	public static void setFetchSize(PreparedStatement pstmt, int fetchSize, MetaData metaData) {
		if (metaData != null && metaData.isPstmt_fetchSizeSupported()) {
			try {
				pstmt.setFetchSize(fetchSize);
			} catch (Throwable t) {
				// Ignore. Exception may happen if the driver doesn't support
				// this operation and we didn't set meta-data correctly.
				// However, it is a good idea to update the meta-data so that
				// we don't have to incur the cost of catching an exception
				// each time.
				Log.error("Disabling JDBC method pstmt.setFetchSize(fetchSize).", t);
				metaData.setPstmt_fetchSizeSupported(false);
			}
		}
	}

	/**
	 * Returns the current connection provider. The only case in which this
	 * method should be called is if more information about the current
	 * connection provider is needed. Database connections should always be
	 * obtained by calling the getConnection method of this class.
	 * 
	 * @return the connection provider.
	 */
	public static ConnectionProvider getConnectionProvider(int index) {
		return connectionProviders.get(String.valueOf(index));
	}

	/**
	 * Sets the connection provider. The old provider (if it exists) is shut
	 * down before the new one is started. A connection provider <b>should
	 * not</b> be started before being passed to the connection manager because
	 * the manager will call the start() method automatically.
	 * 
	 * @param provider
	 *            the ConnectionProvider that the manager should obtain
	 *            connections from.
	 */
	public static ConnectionProvider setConnectionProvider(ConnectionProvider provider, int index) {
		synchronized (providerLock) {
			ConnectionProvider connectionProvider = connectionProviders.get(String.valueOf(index));
			if (connectionProvider != null) {
				connectionProvider.destroy();
				connectionProvider = null;
				connectionProviders.remove(String.valueOf(index));
			}
			connectionProvider = provider;
			connectionProvider.start();
			// Now, get a connection to determine meta data.
			Connection con = null;
			try {
				con = connectionProvider.getConnection(index);
				if (!metaDatas.containsKey(String.valueOf(index)) && con != null)
					setMetaData(con, index);

			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			} finally {
				connectionProviders.put(String.valueOf(index), connectionProvider);
				closeConnection(con);
			}
			return connectionProvider;
		}
	}

	/**
	 * Destroys the currennt connection provider. Future calls to
	 * {@link #getConnectionProvider()} will return <tt>null</tt> until a new
	 * ConnectionProvider is set, or one is automatically loaded by a call to
	 * {@link #getConnection()}.
	 */
	public static void destroyConnectionProvider(int index) {
		synchronized (providerLock) {
			ConnectionProvider connectionProvider = connectionProviders.get(String.valueOf(index));
			if (connectionProvider != null) {
				connectionProvider.destroy();
				connectionProvider = null;
				connectionProviders.remove(String.valueOf(index));
			}
		}
	}

	/**
	 * Retrives a large text column from a result set, automatically performing
	 * streaming if the JDBC driver requires it. This is necessary because
	 * different JDBC drivers have different capabilities and methods for
	 * retrieving large text values.
	 * 
	 * @param rs
	 *            the ResultSet to retrieve the text field from.
	 * @param columnIndex
	 *            the column in the ResultSet of the text field.
	 * @return the String value of the text field.
	 * @throws SQLException
	 *             if an SQL exception occurs.
	 */
	public static String getLargeTextField(ResultSet rs, int columnIndex, int index) throws SQLException {
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isStreamTextRequired()) {
			Reader bodyReader = null;
			String value = null;
			try {
				bodyReader = rs.getCharacterStream(columnIndex);
				if (bodyReader == null) {
					return null;
				}
				char[] buf = new char[256];
				int len;
				StringWriter out = new StringWriter(256);
				while ((len = bodyReader.read(buf)) >= 0) {
					out.write(buf, 0, len);
				}
				value = out.toString();
				out.close();
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
				throw new SQLException("Failed to load text field");
			} finally {
				try {
					if (bodyReader != null) {
						bodyReader.close();
					}
				} catch (Exception e) {
					// Ignore.
				}
			}
			return value;
		} else {
			return rs.getString(columnIndex);
		}
	}

	/**
	 * Sets a large text column in a result set, automatically performing
	 * streaming if the JDBC driver requires it. This is necessary because
	 * different JDBC drivers have different capabilities and methods for
	 * setting large text values.
	 * 
	 * @param pstmt
	 *            the PreparedStatement to set the text field in.
	 * @param parameterIndex
	 *            the index corresponding to the text field.
	 * @param value
	 *            the String to set.
	 * @throws SQLException
	 *             if an SQL exception occurs.
	 */
	public static void setLargeTextField(PreparedStatement pstmt, int parameterIndex, String value, int index) throws SQLException {
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isStreamTextRequired()) {
			Reader bodyReader;
			try {
				bodyReader = new StringReader(value);
				pstmt.setCharacterStream(parameterIndex, bodyReader, value.length());
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
				throw new SQLException("Failed to set text field.");
			}
			// Leave bodyReader open so that the db can read from it. It
			// *should*
			// be garbage collected after it's done without needing to call
			// close.
		} else {
			pstmt.setString(parameterIndex, value);
		}
	}

	/**
	 * Sets the max number of rows that should be returned from executing a
	 * statement. The operation is automatically bypassed if Jive knows that the
	 * the JDBC driver or database doesn't support it.
	 * 
	 * @param stmt
	 *            the Statement to set the max number of rows for.
	 * @param maxRows
	 *            the max number of rows to return.
	 */
	public static void setMaxRows(Statement stmt, int maxRows, MetaData metaData) {
		if (metaData != null && metaData.isMaxRowsSupported()) {
			try {
				stmt.setMaxRows(maxRows);
			} catch (Throwable t) {
				// Ignore. Exception may happen if the driver doesn't support
				// this operation and we didn't set meta-data correctly.
				// However, it is a good idea to update the meta-data so that
				// we don't have to incur the cost of catching an exception
				// each time.
				Log.error("Disabling JDBC method stmt.setMaxRows(maxRows).", t);
				metaData.setMaxRowsSupported(false);
			}
		}
	}

	/**
	 * Sets the number of rows that the JDBC driver should buffer at a time. The
	 * operation is automatically bypassed if Jive knows that the the JDBC
	 * driver or database doesn't support it.
	 * 
	 * @param rs
	 *            the ResultSet to set the fetch size for.
	 * @param fetchSize
	 *            the fetchSize.
	 */
	public static void setFetchSize(ResultSet rs, int fetchSize, int index) {
		MetaData metaData = metaDatas.get(String.valueOf(index));
		if (metaData != null && metaData.isFetchSizeSupported()) {
			try {
				rs.setFetchSize(fetchSize);
			} catch (Throwable t) {
				// Ignore. Exception may happen if the driver doesn't support
				// this operation and we didn't set meta-data correctly.
				// However, it is a good idea to update the meta-data so that
				// we don't have to incur the cost of catching an exception
				// each time.
				Log.error("Disabling JDBC method rs.setFetchSize(fetchSize).", t);
				metaData.setFetchSizeSupported(false);
			}
		}
	}

	/**
	 * Uses a connection from the database to set meta data information about
	 * what different JDBC drivers and databases support.
	 * 
	 * @param con
	 *            the connection.
	 * @throws SQLException
	 *             if an SQL exception occurs.
	 */
	private static void setMetaData(Connection con, int index) throws SQLException {
		DatabaseMetaData conMetaData = con.getMetaData();
		MetaData metaData = new MetaData();
		// Supports transactions?
		metaData.setTransactionsSupported(conMetaData.supportsTransactions());
		// Supports subqueries?
		metaData.setSubqueriesSupported(conMetaData.supportsCorrelatedSubqueries());
		// Supports scroll insensitive result sets? Try/catch block is a
		// workaround for DB2 JDBC driver, which throws an exception on
		// the method call.
		try {
			metaData.setScrollResultsSupported(conMetaData.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
		} catch (Exception e) {
			metaData.setScrollResultsSupported(false);
		}
		// Supports batch updates
		metaData.setBatchUpdatesSupported(conMetaData.supportsBatchUpdates());

		// Set defaults for other meta properties
		metaData.setStreamTextRequired(false);
		metaData.setMaxRowsSupported(true);
		metaData.setFetchSizeSupported(true);

		// Get the database name so that we can perform meta data settings.
		String dbName = conMetaData.getDatabaseProductName().toLowerCase();
		String driverName = conMetaData.getDriverName().toLowerCase();

		// Oracle properties.
		if (dbName.indexOf("oracle") != -1) {
			metaData.setDatabaseType(DatabaseType.oracle);
			metaData.setStreamTextRequired(true);
			metaData.setScrollResultsSupported(false);
			/*
			 * TODO comment and test this, it should be supported since 10g
			 */
			// The i-net AUGURO JDBC driver
			if (driverName.indexOf("auguro") != -1) {
				metaData.setStreamTextRequired(false);
				metaData.setFetchSizeSupported(true);
				metaData.setMaxRowsSupported(false);
			}
		}
		// Postgres properties
		else if (dbName.indexOf("postgres") != -1) {
			metaData.setDatabaseType(DatabaseType.postgresql);
			// Postgres blows, so disable scrolling result sets.
			metaData.setScrollResultsSupported(false);
			metaData.setFetchSizeSupported(false);
		}
		// Interbase properties
		else if (dbName.indexOf("interbase") != -1) {
			metaData.setDatabaseType(DatabaseType.interbase);
			metaData.setFetchSizeSupported(false);
			metaData.setMaxRowsSupported(false);
		}
		// SQLServer
		else if (dbName.indexOf("sql server") != -1) {
			metaData.setDatabaseType(DatabaseType.sqlserver);
			// JDBC driver i-net UNA properties
			if (driverName.indexOf("una") != -1) {
				metaData.setFetchSizeSupported(true);
				metaData.setMaxRowsSupported(false);
			}
		}
		// MySQL properties
		else if (dbName.indexOf("mysql") != -1) {
			metaData.setDatabaseType(DatabaseType.mysql);
			metaData.setTransactionsSupported(false);
			/*
			 * TODO comment and test this, it should be supported since 5.0
			 */
		}
		// HSQL properties
		else if (dbName.indexOf("hsql") != -1) {
			metaData.setDatabaseType(DatabaseType.hsqldb);
			// scrollResultsSupported = false; /* comment and test this, it
			// should be supported since 1.7.2 */
		}
		// DB2 properties.
		else if (dbName.indexOf("db2") != 1) {
			metaData.setDatabaseType(DatabaseType.db2);
		}
		metaDatas.put(String.valueOf(index), metaData);
	}

	private DbConnectionManager() {
		// Not instantiable.
	}
}