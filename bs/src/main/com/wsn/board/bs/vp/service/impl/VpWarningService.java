package com.wsn.board.bs.vp.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.wsn.board.bs.db.DbConnectionManager;
import com.wsn.board.bs.vp.service.IVpWarningService;

public class VpWarningService implements IVpWarningService {
	private Logger logger = LoggerFactory.getLogger(VpWarningService.class.getSimpleName());

	private final static int dataSourceIndex = 2;
	public VpWarningService () {}
	
	
	public int findParkPointCount(){
		Connection conn = null;
		PreparedStatement pstmt = null;
	 	try {
			conn = DbConnectionManager.getConnection(dataSourceIndex);
			pstmt = conn.prepareStatement("select count(*) from park_point");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the parkpoint number", e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}
	
	public int findBindCarCount(){
		Connection conn = null;
		PreparedStatement pstmt = null;
	 	try {
			conn = DbConnectionManager.getConnection(dataSourceIndex);
			pstmt = conn.prepareStatement("select count(*) from car where state in (0,3,5,6,7,11)");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the bind car number", e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return 0;
	}
	
	/**
	 * 查询已处理的告警（今天产生的）
	 */
	public int findProcessedAlarmToday(){
		int count = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
	 	try {
	 		//离线告警
			conn = DbConnectionManager.getConnection(dataSourceIndex);
			pstmt = conn.prepareStatement("select count( distinct id) from device_alarm_his where date(create_time) =curdate() and id not in (select id from device_alarm where date(create_time) =curdate())");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count += rs.getInt(1);
			}
			
			//强拆，低电量，位置
			pstmt = conn.prepareStatement("select count( distinct alarm_no) from his_device_state_alarm where  alarm_state = 3 and device_state in (12,13,22) and date(create_time) =curdate() and alarm_no not in (select alarm_no from device_state_alarm where date(create_time) =curdate())");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count += rs.getInt(1);
			}
			
		} catch (SQLException e) {
			logger.error("Cannot find the process alarm today number", e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
	 	
		return count;
		
	}
	
	public int findUnProcessedAlarmToday(){
		int count = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
	 	try {
			conn = DbConnectionManager.getConnection(dataSourceIndex);
			pstmt = conn.prepareStatement("select count(*) from device_alarm where date(create_time) = curdate()");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count += rs.getInt(1);
			}
			
			pstmt = conn.prepareStatement("select count(*) from device_state_alarm where date(create_time) = curdate() and device_state in (12,13,22) and car_id is not null");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count += rs.getInt(1);
			}
			
		} catch (SQLException e) {
			logger.error("Cannot find the unprocess alarm today number", e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
	 	
		return count;
	}
	
	public int findUnProcessedAlarmHis(){
		int count = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
	 	try {
			conn = DbConnectionManager.getConnection(dataSourceIndex);
			pstmt = conn.prepareStatement("select count(*) from device_alarm where date(create_time) < curdate()");
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count += rs.getInt(1);
			}
			
			pstmt = conn.prepareStatement("select count(*) from device_state_alarm where date(create_time) < curdate() and device_state in (12,13,22) and car_id is not null");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count += rs.getInt(1);
			}
			
		} catch (SQLException e) {
			logger.error("Cannot find unprocess alarm his number", e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
	 	
		return count;
	}
	
	public List<JSONObject> findLatestWarnings() throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		List<JSONObject> warnings = new LinkedList<> ();
		try {
			conn = DbConnectionManager.getConnection(dataSourceIndex);
			pstmt = conn.prepareStatement("select * from "
					+"(select id title,24 device_state,device_code,vin,create_time from device_alarm "
					+" union all"
					+" select a.alarm_no title,a.device_state,a.device_code,c.vin,a.create_time from device_state_alarm a ,car c where a.car_id = c.id and a.car_id is not null and a.device_state in (12,13,22)"
					+" ) b order by b.create_time desc");
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				JSONObject warning = new JSONObject();
				warning.put("title", rs.getString(1));
				warning.put("type", "alarm");
				String content = "";
				String deviceState = rs.getString(2);
				if("12".equals(deviceState)){
					content="强拆告警.";
				}else if("13".equals(deviceState)){
					content="低电量告警.";
				}else if("22".equals(deviceState)){
					content="位置告警.";
				}else if("24".equals(deviceState)){
					content="离线告警.";
				}
				content = content + "设备号:"+rs.getString(3);
				String vin = rs.getString(4);
				if(StringUtils.isNotBlank(vin)){
					content += "，车架号:"+vin;
				}
				warning.put("content", content);
				warning.put("time", rs.getString(5));
				warnings.add(warning);
			}
		} catch (SQLException e) {
			logger.error("Cannot find the latest warning list", e);
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, conn);
		}
		return warnings;
	}
}
