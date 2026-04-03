package com.gamebuster19901.excite.modding.unarchiver;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.lang3.concurrent.ConcurrentException;

public class QuickAccessArchive {

	private final Toc toc;
	private final Path archivePath;
	private volatile Archive archive; //This MUST be volatile or double checked locking will not work!
	
	public QuickAccessArchive(Toc toc, Path archivePath) {
		this.toc = toc;
		this.archivePath = archivePath;
	}
	
	public Archive getArchive() throws IOException, ConcurrentException {
		if(archive == null) {
			synchronized(this) {
				if(archive == null) {
					archive = new Archive(archivePath, toc);
				}
			}
		}
		return archive;
	}
	
}
