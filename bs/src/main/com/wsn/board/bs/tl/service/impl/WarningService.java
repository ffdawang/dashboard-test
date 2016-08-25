package com.wsn.board.bs.tl.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.wsn.board.bs.db.DbConnectionManager;
import com.wsn.board.bs.tl.service.IWarningService;

public class WarningService implements IWarningService {
	private Logger logger = LoggerFactory.getLogger(WarningService.class.getSimpleName());

	public WarningService () {}

	@Override
	public int findSpaceNumber() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
	 	try {
			conn = DbConnectionManager.getConnection(1);
			pstmt = conn.prepareStatement("SELECT count(*) FROM wsnui_wh_spaceinfo");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the space number", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}

	@Override
	public int findReceiptNumber() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DbConnectionManager.getConnection(1);
			pstmt = conn.prepareStatement("SELECT count(*) FROM wsnui_wr_basicinfo where status < 9");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the receipt number", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}

	@Override
	public long findFinishedWarningNumber() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DbConnectionManager.getConnection(1);
			pstmt = conn.prepareStatement("SELECT count(*) FROM wsnui_wr_warning warning, wsnui_wr_basicinfo logic "
					+ "WHERE warning.wr_id = logic.id and logic.status < 9 and warning.processResult = 1 and warning.level > 0 and date(warning.createDate) = curdate()");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the finished warning number", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}

	@Override
	public long findUnfinishedWarningNumber() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DbConnectionManager.getConnection(1);
			pstmt = conn.prepareStatement("SELECT count(*) FROM wsnui_wr_warning warning, wsnui_wr_basicinfo logic "
					+ "where warning.wr_id = logic.id and logic.status < 9 and warning.processResult = 0 and warning.level > 0 and date(warning.createDate) = curdate()");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the unfinished warning number", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}

	@Override
	public long findExpiredWarningNumber() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DbConnectionManager.getConnection(1);
			pstmt = conn.prepareStatement("SELECT count(*) FROM wsnui_wr_warning warning, wsnui_wr_basicinfo logic"
					+ " where warning.wr_id = logic.id and logic.status < 9 and warning.processResult = 0 and warning.level > 0 and date(warning.createDate) < curdate()");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the expired warning number", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}

	@Override
	public List<JSONObject> findLatestWarnings() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		List<JSONObject> warnings = new LinkedList<> ();
		try {
			conn = DbConnectionManager.getConnection(1);
			pstmt = conn.prepareStatement("SELECT warning.uuid, warning.msg, warning.createDate FROM wsnui_wr_warning warning, wsnui_wr_basicinfo logic "
					+ "where warning.wr_id = logic.id and logic.status < 9 and warning.processResult = 0 and warning.level > 0 order by warning.createDate desc");
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				JSONObject warning = new JSONObject();
				warning.put("title", rs.getString(1));
				warning.put("type", "alarm");
				warning.put("time", rs.getString(3));
				warning.put("content", rs.getString(2));
				warnings.add(warning);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the expired warning number", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return warnings;
	}

}
