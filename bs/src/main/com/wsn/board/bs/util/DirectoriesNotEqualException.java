package com.wsn.board.bs.util;

public class DirectoriesNotEqualException extends FileUtilitiesException {
	public DirectoriesNotEqualException() {
	}

	public DirectoriesNotEqualException(String message) {
		super(message);
	}

	public DirectoriesNotEqualException(Throwable cause) {
		super(cause);
	}

	public DirectoriesNotEqualException(String message, Throwable cause) {
		super(message, cause);
	}
}