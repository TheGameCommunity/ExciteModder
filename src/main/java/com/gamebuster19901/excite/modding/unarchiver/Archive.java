package com.gamebuster19901.excite.modding.unarchiver;

import com.gamebuster19901.excite.modding.game.file.kaitai.ResMonster;
import com.gamebuster19901.excite.modding.game.file.kaitai.TocMonster;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;

public class Archive {

	private final Path archiveFile;
	private final ResMonster archive;
	private final Toc toc;
	private final byte[] bytes;
	private final LinkedHashMap<String, ArchivedFile> files = new LinkedHashMap<>();
	
	public Archive(Path archivePath, Path tocPath) throws IOException {
		this(archivePath, new Toc(tocPath));
	}
	
	public Archive(Path archivePath, Toc toc) throws IOException {
		this.archiveFile = archivePath;
		this.archive = ResMonster.fromFile(archivePath.toAbsolutePath().toString());
		this.toc = toc;
		if(isCompressed()) {
			bytes = archive.data().compressedData().bytes();
		}
		else {
			bytes = archive.data().uncompressedData();
		}
		if(bytes == null) {
			throw new AssertionError();
		}
		if(bytes.length == 0) {
			throw new AssertionError();
		}
		for(TocMonster.Details fileDetails : getFileDetails()) {
			try {
				files.put(fileDetails.name(), new ArchivedFile(fileDetails, this));
			}
			catch(Throwable t) {
				//swallo
			}
		}

	}
	
	public Toc getToc() {
		return toc;
	}
	
	public Instant getCreationDate() {
		return toc.getCreationDate();
	}
	
	public long getFileCount() {
		return toc.getFileCount();
	}
	
	public long getCompressedSize() {
		return toc.getCompressedSize();
	}
	
	public long getUncompressedSize() {
		return toc.getUncompressedSize();
	}
	
	public SequencedCollection<ArchivedFile> getFiles() {
		return files.sequencedValues();
	}
	
	public ArchivedFile getFile(String name) {
		return files.get(name);
	}
	
	public List<TocMonster.Details> getFileDetails() {
		return toc.getFiles();
	}
	
	public List<TocMonster.Filenames> getFileNames() {
		return toc.getFileNames();
	}
	
	public long getHash() {
		return archive.header().hash();
	}
	
	public boolean isCompressed() {
		if (((archive.header().compressed() >>> 7) & 1) != 0) {
			return true;
		}
		else if (archive.header().compressed() == 0) {
			return false;
		}
		else {
			throw new IOError(new IOException("Unknown compression value in header: " + archive.header().compressed()));
		}
	}
	
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}
	
	public void validate() throws AssertionError {
		if(archive.header().unixTimestamp() != toc.getCreationDate().getEpochSecond()) {
			throw new AssertionError("Creation date mismatch!");
		}
	}
	
	public Path getTocFile() {
		return toc.getFile();
	}
	
	public Path getArchiveFile() {
		return archiveFile;
	}
	
	public void writeTo(Path directory) throws IOException {
		directory = directory.resolve(archiveFile.getFileName());
		Path dir = Files.createDirectories(directory);
		for(ArchivedFile file : files.sequencedValues()) {
			file.writeTo(dir);
		}
	}
	
}
