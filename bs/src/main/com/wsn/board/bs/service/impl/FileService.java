package com.wsn.board.bs.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsn.board.bs.service.IFileService;

public class FileService implements IFileService{
	private Logger logger = LoggerFactory.getLogger(FileService.class.getSimpleName());

	@Override
	public void writeFile(String path, byte[] data) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(path);
			out.write(data);
		} catch (IOException e) {
			// error handling
			logger.error("Cannot write file " + path, e);
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

}
