package com.wsn.board.bs.db;

public class MetaData {
	// True if connection profiling is turned on. Always false by default.
	private boolean profilingEnabled = false;
	// True if the database support transactions.
	private boolean transactionsSupported;
	// True if the database requires large text fields to be streamed.
	private boolean streamTextRequired;
	/** True if the database supports the Statement.setMaxRows() method. */
	private boolean maxRowsSupported;
	/** True if the database supports the rs.setFetchSize() method. */
	private boolean fetchSizeSupported;
	// True if the database supports correlated subqueries.
	private boolean subqueriesSupported;
	// True if the database supports scroll-insensitive results.
	private boolean scrollResultsSupported;
	// True if the database supports batch updates.
	private boolean batchUpdatesSupported;
	/** True if the database supports the Statement.setFetchSize()) method. */
	private boolean pstmt_fetchSizeSupported = true;
	private DatabaseType databaseType = DatabaseType.unknown;

	/**
	 * Returns the database type. The possible types are constants of the
	 * DatabaseType class. Any database that doesn't have its own constant falls
	 * into the "Other" category.
	 * 
	 * @return the database type.
	 */
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	/**
	 * Returns true if connection profiling is turned on. You can collect
	 * profiling statistics by using the static methods of the
	 * ProfiledConnection class.
	 * 
	 * @return true if connection profiling is enabled.
	 */
	public boolean isProfilingEnabled() {
		return profilingEnabled;
	}

	public boolean isTransactionsSupported() {
		return transactionsSupported;
	}

	public boolean isStreamTextRequired() {
		return streamTextRequired;
	}

	public boolean isMaxRowsSupported() {
		return maxRowsSupported;
	}

	public boolean isFetchSizeSupported() {
		return fetchSizeSupported;
	}

	public boolean isPstmtFetchSizeSupported() {
		return pstmt_fetchSizeSupported;
	}

	public boolean isSubqueriesSupported() {
		return subqueriesSupported;
	}

	public boolean isScrollResultsSupported() {
		return scrollResultsSupported;
	}

	public boolean isBatchUpdatesSupported() {
		return batchUpdatesSupported;
	}

	public boolean isPstmt_fetchSizeSupported() {
		return pstmt_fetchSizeSupported;
	}

	public void setPstmt_fetchSizeSupported(boolean pstmt_fetchSizeSupported) {
		this.pstmt_fetchSizeSupported = pstmt_fetchSizeSupported;
	}

	public void setProfilingEnabled(boolean profilingEnabled) {
		this.profilingEnabled = profilingEnabled;
	}

	public void setTransactionsSupported(boolean transactionsSupported) {
		this.transactionsSupported = transactionsSupported;
	}

	public void setStreamTextRequired(boolean streamTextRequired) {
		this.streamTextRequired = streamTextRequired;
	}

	public void setMaxRowsSupported(boolean maxRowsSupported) {
		this.maxRowsSupported = maxRowsSupported;
	}

	public void setFetchSizeSupported(boolean fetchSizeSupported) {
		this.fetchSizeSupported = fetchSizeSupported;
	}

	public void setSubqueriesSupported(boolean subqueriesSupported) {
		this.subqueriesSupported = subqueriesSupported;
	}

	public void setScrollResultsSupported(boolean scrollResultsSupported) {
		this.scrollResultsSupported = scrollResultsSupported;
	}

	public void setBatchUpdatesSupported(boolean batchUpdatesSupported) {
		this.batchUpdatesSupported = batchUpdatesSupported;
	}

	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	public static String getTestSQL(String driver) {
		if (driver == null) {
			return "select 1";
		} else if (driver.contains("db2")) {
			return "select 1 from sysibm.sysdummy1";
		} else if (driver.contains("oracle")) {
			return "select 1 from dual";
		} else {
			return "select 1";
		}
	}

	/**
	 * A class that identifies the type of the database that Jive is connected
	 * to. In most cases, we don't want to make any database specific calls and
	 * have no need to know the type of database we're using. However, there are
	 * certain cases where it's critical to know the database for performance
	 * reasons.
	 */
	@SuppressWarnings({ "UnnecessarySemicolon" })
	// Support for QDox parsing
	public enum DatabaseType {

		oracle,

		postgresql,

		mysql,

		hsqldb,

		db2,

		sqlserver,

		interbase,

		unknown;
	}
}
