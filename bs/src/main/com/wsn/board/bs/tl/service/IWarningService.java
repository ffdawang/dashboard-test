package com.wsn.board.bs.tl.service;

import java.sql.SQLException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface IWarningService {

	/**
	 * 查询仓位数量
	 * @return
	 * @throws SQLException 
	 */
	int findSpaceNumber() throws SQLException;

	/**
	 * 查询仓单数量
	 * @return
	 * @throws SQLException 
	 */
	int findReceiptNumber() throws SQLException;

	/**
	 * 查询今日完成的报警
	 * @return
	 * @throws SQLException 
	 */
	long findFinishedWarningNumber() throws SQLException;

	/**
	 * 查询今日未完成的报警
	 * @return
	 * @throws SQLException 
	 */
	long findUnfinishedWarningNumber() throws SQLException;

	/**
	 * 查询过期且未完成的报警
	 * @return
	 * @throws SQLException 
	 */
	long findExpiredWarningNumber() throws SQLException;

	/**
	 * 查询最新的10条报警信息
	 * @param topNumber
	 * @return
	 */
	List<JSONObject> findLatestWarnings() throws SQLException;
}
