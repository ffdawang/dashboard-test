package com.wsn.board.bs.tl.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wsn.board.bs.service.IFileService;
import com.wsn.board.bs.tl.service.IWarningService;
import com.wsn.board.bs.util.Config;

public class HandleTLService implements Runnable {
	private Logger logger = LoggerFactory.getLogger(HandleTLService.class.getSimpleName());
	private IWarningService waringService;
	private IFileService fileService;

	public HandleTLService(IWarningService waringService, IFileService fileService) {
		this.waringService = waringService;
		this.fileService = fileService;
	}

	@Override
	public void run() {
		try {
			int spaceNum = waringService.findSpaceNumber();
			int receiptNum = waringService.findReceiptNumber();
			long finishedWarningNum = waringService.findFinishedWarningNumber();
			long unfinishedWarningNum = waringService.findUnfinishedWarningNumber();
			long expiredWarningNum = waringService.findExpiredWarningNumber();
			List<JSONObject> warnings = waringService.findLatestWarnings();
			JSONObject warningJson = new JSONObject();
			warningJson.put("wh_spaces", spaceNum * 2);
			warningJson.put("wh_tickets", receiptNum * 2);
			warningJson.put("wh_finished_warnings", finishedWarningNum);
			warningJson.put("wh_unfinished_warnings", unfinishedWarningNum);
			warningJson.put("wh_today_alarms", unfinishedWarningNum);
			warningJson.put("wh_expired_alarms", expiredWarningNum);
			warningJson.put("wh_alarm_lists", warnings);	
			logger.info(JSON.toJSONString(warningJson));
			fileService.writeFile(Config.getProperties()
					.getProperty("tl.bs.output.file.path"), JSONObject.toJSONBytes(warningJson));
		} catch (SQLException | IOException e) {
			logger.error("Handle warning failed", e);
		}

	}

}
