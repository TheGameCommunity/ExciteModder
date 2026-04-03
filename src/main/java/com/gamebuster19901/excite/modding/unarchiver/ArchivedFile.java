package com.gamebuster19901.excite.modding.unarchiver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import com.gamebuster19901.excite.modding.game.file.kaitai.TocMonster;

public class ArchivedFile {

	private final TocMonster.Details fileDetails;
	private final Archive archive;
	
	public ArchivedFile(TocMonster.Details fileDetails, Archive archive) {
		try {
			this.fileDetails = fileDetails;
			this.archive = archive;
			System.out.println("Archive size: " + archive.getUncompressedSize());
			System.out.println("File offset: " + fileDetails.fileOffset());
			System.out.println("File size: " + fileDetails.fileSize());
			System.out.println("File end: " + ((int)fileDetails.fileOffset() + (int)fileDetails.fileSize()));
			byte[] bytes = archive.getBytes();
			System.out.println("Array size: " + bytes.length);
		}
		catch(Throwable t) {
			System.err.println("Bad resource reference: " + getName() + " from " + archive.getArchiveFile().getFileName());
			t.printStackTrace();
			throw t;
		}
	}
	
	public String getName() {
		return fileDetails.name();
	}
	
	public byte[] getBytes() throws IOException {
		try {
			return Arrays.copyOfRange(archive.getBytes(), (int)fileDetails.fileOffset(), (int)(fileDetails.fileOffset() + (int)fileDetails.fileSize()));
		}
		catch(Throwable t) {
			throw new IOException(t);
		}
	}
	
	public void writeTo(Path directory) throws IOException {
		Path dir = Files.createDirectories(directory);
		Path f = dir.resolve(getName());
		Files.deleteIfExists(f);
		Files.createFile(f);
		Files.write(f, getBytes(), StandardOpenOption.CREATE);
	}
	
}
