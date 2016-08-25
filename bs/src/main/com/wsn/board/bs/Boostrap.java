package com.wsn.board.bs;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.wsn.board.bs.service.impl.FileService;
import com.wsn.board.bs.tl.service.impl.HandleTLService;
import com.wsn.board.bs.tl.service.impl.WarningService;
import com.wsn.board.bs.util.Config;
import com.wsn.board.bs.util.Log;
import com.wsn.board.bs.vp.service.HandleVPService;
import com.wsn.board.bs.vp.service.impl.VpWarningService;

public class Boostrap { 
	// 周期间隔秒数
	private static long periodSecond = 60;

	static {
		try {
			String periodSecondProp = Config.getProperties().getProperty("periodSecond");
			periodSecondProp = StringUtils.trim(periodSecondProp);
			if (StringUtils.isNotBlank(periodSecondProp) 
					&& StringUtils.isNumeric(periodSecondProp))
				periodSecond = Long.parseLong(periodSecondProp);
		} catch (IOException e) {
			Log.error("Read periodSecond failed", e);
		}
	}

	public static void main(String[] args) {
//		ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
//		service.scheduleAtFixedRate(new HandleTLService(new WarningService(),
//				new FileService()), 1000, 1000 * periodSecond, TimeUnit.MILLISECONDS);
//		service.scheduleAtFixedRate(new HandleVPService(new VpWarningService(),
//				new FileService()), 1000, 1000 * periodSecond, TimeUnit.MILLISECONDS);
		new HandleTLService(new WarningService(), new FileService()).run();
		new HandleVPService(new VpWarningService(), new FileService()).run();
	}

}
