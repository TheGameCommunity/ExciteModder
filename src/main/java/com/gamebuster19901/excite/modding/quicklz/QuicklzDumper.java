package com.gamebuster19901.excite.modding.quicklz;

import java.io.IOException;
import java.io.IOError;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.gamebuster19901.excite.modding.quicklz.QuickLZ.ExitCode;
import com.gamebuster19901.excite.modding.util.FileUtils;

public class QuicklzDumper {
	
	private final Path input;
	private final Path output;
	
	{
		try {
			input = FileUtils.createTempFile();
			output = FileUtils.createTempFile();
		}
		catch(IOException e) {
			throw new IOError(e);
		}
	}
	
	public byte[] decode(byte[] _raw_data) {
		try {
			Files.write(input, _raw_data, StandardOpenOption.CREATE);
			Process process = QuickLZ.decompress(input, output);
			int exitCode = process.waitFor();
			if(exitCode != ExitCode.SUCCESS.ordinal()) {
				throw new DecodingException(exitCode);
			}
			return Files.readAllBytes(output);
		}
		catch(IOException | InterruptedException e) {
			IOError err = new IOError(e);
			err.printStackTrace();
			throw err;
		}
	}
	
}
