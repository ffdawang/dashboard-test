package com.wsn.board.bs.service;

import java.io.IOException;

public interface IFileService {

	/**
	 * 写数据到文件
	 * @param path
	 * @param data
	 * @throws IOException 
	 */
	public void writeFile(String path, byte[] data) throws IOException;
}
