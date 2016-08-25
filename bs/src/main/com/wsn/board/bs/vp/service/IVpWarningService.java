package com.wsn.board.bs.vp.service;

import java.sql.SQLException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;


public interface IVpWarningService {

	/**
	 * 查询停放点数量
	 */
	int findParkPointCount();
	
	/**
	 * 查询绑定车辆。当前有多少辆车处于绑定状态
	 * @return
	 */
	int findBindCarCount();
	
	/**
	 * 查询今天产生的已处理告警
	 * @return
	 */
	int findProcessedAlarmToday();
	
	/**
	 * 查询未处理的告警(当天产生的告警)
	 * @return
	 */
	int findUnProcessedAlarmToday();
	
	/**
	 * 查询未处理的历史告警
	 * @return
	 */
	int findUnProcessedAlarmHis();
	
	/**
	 * 查询最新10条告警（未处理.）
	 * 调整为查询所有未处理告警
	 * @return
	 * @throws SQLException
	 */
	List<JSONObject> findLatestWarnings() throws SQLException;
}
