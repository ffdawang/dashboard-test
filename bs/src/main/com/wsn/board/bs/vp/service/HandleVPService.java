package com.wsn.board.bs.vp.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wsn.board.bs.service.IFileService;
import com.wsn.board.bs.util.Config;

public class HandleVPService implements Runnable {
	private Logger logger = LoggerFactory.getLogger(HandleVPService.class.getSimpleName());
	private IVpWarningService waringService;
	private IFileService fileService;

	public HandleVPService(IVpWarningService waringService, IFileService fileService) {
		this.waringService = waringService;
		this.fileService = fileService;
	}

	@Override
	public void run() {
		try {
			JSONObject warningJson = new JSONObject();
			
			int parkPointNum = waringService.findParkPointCount();
			int bindCarNum = waringService.findBindCarCount();
			warningJson.put("car_spots", parkPointNum * 2);
			warningJson.put("car_bound", bindCarNum * 10);
			
			
			long finishedWarningNum = waringService.findProcessedAlarmToday();
			
			long unfinishedWarningNum = waringService.findUnProcessedAlarmToday();//未处理告警（今天产生）
			
			
			long car_today_alarms = waringService.findUnProcessedAlarmToday();//未处理告警（今天产生）
			long car_expired_alarms = waringService.findUnProcessedAlarmHis();//未处理告警（历史）
			
			List<JSONObject> warnings = waringService.findLatestWarnings();
			
			
			warningJson.put("car_finished_warnings", finishedWarningNum);
			warningJson.put("car_unfinished_warnings", unfinishedWarningNum);
			warningJson.put("car_today_alarms", car_today_alarms);
			warningJson.put("car_expired_alarms", car_expired_alarms);
			warningJson.put("car_alarm_lists", warnings);
			warningJson.put("car_work_lists", null);
			
			logger.info(JSON.toJSONString(warningJson));
			fileService.writeFile(Config.getProperties()
					.getProperty("vp.bs.output.file.path"), JSONObject.toJSONBytes(warningJson));
			
			
		} catch (SQLException | IOException e) {
			logger.error("Handle vp warning failed", e);
		}
	}

}
