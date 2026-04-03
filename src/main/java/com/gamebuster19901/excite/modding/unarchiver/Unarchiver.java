package com.gamebuster19901.excite.modding.unarchiver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.gamebuster19901.excite.modding.concurrent.Batch;
import com.gamebuster19901.excite.modding.game.file.kaitai.TocMonster.Details;
import com.gamebuster19901.excite.modding.unarchiver.concurrent.DecisionType;
import com.gamebuster19901.excite.modding.util.FileUtils;

public class Unarchiver {

	private final Path sourceDir;
	private final Path destDir;
	
	public LinkedHashSet<Path> tocs = new LinkedHashSet<>();
	public LinkedHashSet<Path> archives = new LinkedHashSet<>();
	
	public Unarchiver(Path sourceDir, Path destDir) throws IOException {
		this.sourceDir = sourceDir;
		this.destDir = destDir;
		refresh();
	}
	
	public Collection<Path> getTocs() {
		return Collections.unmodifiableCollection(tocs);
	}
	
	public Collection<Batch<Pair<DecisionType, Callable<Void>>>> getCopyBatches() {
		LinkedHashSet<Batch<Pair<DecisionType, Callable<Void>>>> batches = new LinkedHashSet<>();
		for(Path toc : tocs) {
			batches.add(getCopyBatch(toc));
		}
		return batches;
	}
	
	public Batch<Pair<DecisionType, Callable<Void>>> getCopyBatch(Path tocFile) {
		Batch<Pair<DecisionType, Callable<Void>>> batch = new Batch<>(tocFile.getFileName().toString());
		try {
			Toc toc = new Toc(tocFile.toAbsolutePath());
			List<Details> details = toc.getFiles();
			
			final QuickAccessArchive QArchive = getArchive(toc);
			for(Details resource : details) {
				batch.addRunnable(() -> {
					try {
						String resourceName = resource.name();
						Path dest = destDir.resolve(tocFile.getFileName());
						System.out.println(tocFile.getFileName() + "/" + resourceName);
						Archive archive = QArchive.getArchive();
						ArchivedFile f = archive.getFile(resourceName);
						f.writeTo(dest);
						if(resource.name().endsWith("tex")) {
							return Pair.of(DecisionType.SKIP, () -> {return null;}); //the asset was successfully extracted, but we don't know how to process it
						}
						return Pair.of(DecisionType.PROCEED, () -> {return null;}); //the asset was successfully extracted, and will be submitted to the next batchRunner to convert into a viewable format
					}
					catch(Throwable t) {
						return Pair.of(DecisionType.IGNORE, () -> {throw t;}); //let the next batchrunner that an error ocurred, and will not be submitted to the next batchrunner.
					}
				});
			}

		}
		catch(Throwable t) {
			batch.addRunnable(() -> {
				return Pair.of(DecisionType.IGNORE, () -> {throw t;}); //let the next batchrunner know that an error occurred
			});
		}
		return batch;
	}
	
	public boolean isValid() {
		return FileUtils.isDirectory(sourceDir) && FileUtils.isDirectory(destDir);
	}
	
	public QuickAccessArchive getArchive(Toc toc) throws IOException {
		for(Path archivePath : archives) {
			if(getFileName(toc.getFile()).equals(getFileName(archivePath))) {
				return new QuickAccessArchive(toc, archivePath);
			}
		}
		throw new FileNotFoundException("Resource file for toc " + toc.getFile().getFileName());
	}
	
	private static String getFileName(Path f) {
		 String fileName = f.getFileName().toString();
		 int i = fileName.lastIndexOf('.');
		 return (i == -1) ? fileName : fileName.substring(0, i);
	}
	
	private void refresh() throws IOException {
		tocs.clear();
		archives.clear();
		try(Stream<Path> fileStream = Files.walk(sourceDir)) {
			fileStream.filter(Files::isRegularFile).forEach((f) -> {
				if(f.getFileName().toString().endsWith(".toc")) {
					tocs.add(f);
				}
				else {
					archives.add(f);
				}
			});
		}
	}
	
}
