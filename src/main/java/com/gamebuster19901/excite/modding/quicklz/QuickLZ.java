package com.gamebuster19901.excite.modding.quicklz;

import java.io.IOException;
import java.nio.file.Path;

import com.thegamecommunity.excite.modding.util.foreign.c.dependency.ForeignDependencies;

public class QuickLZ {

	static {
		if(!ForeignDependencies.EQUICKLZ.isAvailable()) {
			try {
				ForeignDependencies.downloadAndCompileAllDeps();
			} catch (LinkageError e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static enum Mode {
		help,
		compress,
		decompress
	}
	
	public static enum ExitCode {
		SUCCESS,
		HELP_SUCCESS,
		UNKNOWN_MODE,
		MISSING_MODE_ARG,
		MISSING_SOURCE_ARG,
		MISSING_DEST_ARG,
		TOO_MANY_ARGS,
		SOURCE_ERR,
		DEST_ERR,
		UNKNOWN;
		
		public static ExitCode get(int i) {
			if(i < values().length && i >= 0) {
				return values()[i];
			}
			return UNKNOWN;
		}
	}
	
	private static final QuickLZImpl impl = QuickLZImpl.get();
	
	public static void compress(Path file, Path dest) {
		throw new UnsupportedOperationException("Not Yet Implemented");
	}
	
	public static Process decompress(Path file, Path dest) throws IOException {
		return impl.getProcess(Mode.decompress, file, dest).start();
	}
	
}
