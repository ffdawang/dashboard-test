package com.wsn.board.bs.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {

	private static final org.slf4j.Logger Logger = org.slf4j.LoggerFactory.getLogger(Log.class);

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#isErrorEnabled()}.
	 *             Functionality of this method is delegated there.
	 */
	@Deprecated
    public static boolean isErrorEnabled() {
        return Logger.isErrorEnabled();
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#isDebugEnabled()}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static boolean isDebugEnabled() {
        return Logger.isDebugEnabled();
    }

    public static void setDebugEnabled(boolean enabled) {
        // SLF4J doesn't provide a hook into the logging implementation. We'll have to do this 'direct', bypassing slf4j.
    	final org.apache.log4j.Level newLevel;
    	if (enabled) {
    		newLevel = org.apache.log4j.Level.ALL;
    	} else {
    		newLevel = org.apache.log4j.Level.INFO;
    	}
    		
    	org.apache.log4j.LogManager.getRootLogger().setLevel(newLevel);
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#isInfoEnabled()}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static boolean isInfoEnabled() {
        return Logger.isInfoEnabled();
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#isWarnEnabled()}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static boolean isWarnEnabled() {
        return Logger.isWarnEnabled();
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#debug(String)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void debug(String s) {
        if (isDebugEnabled()) {
            Logger.debug(s);
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#debug(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void debug(Throwable throwable) {
        if (isDebugEnabled()) {
            Logger.debug("", throwable);
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#debug(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void debug(String s, Throwable throwable) {
        if (isDebugEnabled()) {
            Logger.debug(s, throwable);
        }
    }

    public static void markDebugLogFile(String username) {
    	String message = getMarkMessage(username);
        debug(message);
    }

    public static void rotateDebugLogFile() {
        // SLF4J doesn't provide a hook into the logging implementation. We'll have to do this 'direct', bypassing slf4j.
        File logFile = new File(Log.getLogDirectory(), "debug.log");
        emptyFile(logFile);
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#info(String)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void info(String s) {
        if (isInfoEnabled()) {
            Logger.info(s);
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#info(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void info(Throwable throwable) {
        if (isInfoEnabled()) {
            Logger.info("", throwable);
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#info(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void info(String s, Throwable throwable) {
        if (isInfoEnabled()) {
            Logger.info(s, throwable);
        }
    }

    public static void markInfoLogFile(String username) {
    	String message = getMarkMessage(username);
        info(message);
    }

    public static void rotateInfoLogFile() {
        // SLF4J doesn't provide a hook into the logging implementation. We'll have to do this 'direct', bypassing slf4j.
        File logFile = new File(Log.getLogDirectory(), "info.log");
        emptyFile(logFile);
    }
    
	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#warn(String)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void warn(String s) {
        if (isWarnEnabled()) {
            Logger.warn(s);
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#warn(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void warn(Throwable throwable) {
        if (isWarnEnabled()) {
            Logger.warn("", throwable);
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#debug(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void warn(String s, Throwable throwable) {
        if (isWarnEnabled()) {
            Logger.warn(s, throwable);
        }
    }

    public static void markWarnLogFile(String username) {
    	String message = getMarkMessage(username);
        warn(message);
    }

    public static void rotateWarnLogFile() {
        // SLF4J doesn't provide a hook into the logging implementation. We'll have to do this 'direct', bypassing slf4j.
        File logFile = new File(Log.getLogDirectory(), "warn.log");
        emptyFile(logFile);
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#error(String)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void error(String s) {
        if (isErrorEnabled()) {
            Logger.error(s);
            if (isDebugEnabled()) {
                printToStdErr(s, null);
            }
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#error(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void error(Throwable throwable) {
        if (isErrorEnabled()) {
            Logger.error("", throwable);
            if (isDebugEnabled()) {
                printToStdErr(null, throwable);
            }
        }
    }

	/**
	 * @deprecated replaced by {@link org.slf4j.Logger#error(String, Throwable)}.
	 *             Functionality of this method is delegated there.
	 */
    @Deprecated
	public static void error(String s, Throwable throwable) {
        if (isErrorEnabled()) {
            Logger.error(s, throwable);
            if (isDebugEnabled()) {
                printToStdErr(s, throwable);
            }
        }
    }

    public static void markErrorLogFile(String username) {
    	String message = getMarkMessage(username);
        error(message);
    }

    public static void rotateErrorLogFile() {
        // SLF4J doesn't provide a hook into the logging implementation. We'll have to do this 'direct', bypassing slf4j.
        File logFile = new File(Log.getLogDirectory(), "error.log");
        emptyFile(logFile);
    }

    /**
     * Returns the directory that log files exist in. The directory name will
     * have a File.separator as the last character in the string.
     *
     * @return the directory that log files exist in.
     */
    public static String getLogDirectory() {
        // SLF4J doesn't provide a hook into the logging implementation. We'll have to do this 'direct', bypassing slf4j.
    	final StringBuilder sb = new StringBuilder();
    	String home = ".";
    	try {
			home = Config.getProperties().getProperty("home.directory", ".");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sb.append(home);
    	if (!sb.substring(sb.length()-1).startsWith(File.separator)) {
    		sb.append(File.separator);
    	}
    	sb.append("logs");
    	sb.append(File.separator);
    	return sb.toString();
    }

    private static String getMarkMessage(String username) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[Maker]")
    		.append("[").append(username).append("]")
    		.append("[").append(DateUtils.formatDateToString(new java.util.Date(), DateUtils.DATE_FORMAT)).append("]");
    	return sb.toString();
    }
    
    private static void printToStdErr(String s, Throwable throwable) {
        if (s != null) {
            System.err.println(s);
        }
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            System.err.print(sw.toString());
            System.err.print("\n");
        }
    }

    private static void emptyFile(File logFile) {
    	BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(logFile));
            out.write("");
        } catch (IOException ex) {
        	Log.warn("Could not empty file " + logFile.getName(), ex);
        } finally {
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException ex) {
					Log.warn("Could not close file.", ex);
				}
        	}
        }
	}
}