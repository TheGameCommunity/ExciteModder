package com.gamebuster19901.excite.modding.quicklz;

import java.io.IOException;

public class DecodingException extends IOException {

	public DecodingException(int exitCode) {
		super("QuickLZ process failed with exit code (" + exitCode + "): " + QuickLZ.ExitCode.get(exitCode));
	}
	
	public DecodingException(Throwable cause) {
		super(cause);
	}
	
}
