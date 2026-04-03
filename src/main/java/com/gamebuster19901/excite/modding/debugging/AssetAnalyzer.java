package com.gamebuster19901.excite.modding.debugging;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.gamebuster19901.excite.modding.concurrent.Batch;
import com.gamebuster19901.excite.modding.concurrent.BatchRunner;
import com.gamebuster19901.excite.modding.game.file.kaitai.TocMonster.Details;
import com.gamebuster19901.excite.modding.unarchiver.QuickAccessArchive;
import com.gamebuster19901.excite.modding.unarchiver.Toc;
import com.gamebuster19901.excite.modding.unarchiver.Unarchiver;
import com.thegamecommunity.excite.modding.game.file.AssetType;

public class AssetAnalyzer {

	private static final Path current = Path.of("").toAbsolutePath();
	private static final Path run = current.resolve("run");
	private static final Path assets = current.resolve("gameData");
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(current);
		System.out.println(run);
		System.out.println(assets);
		
		Unarchiver unarchiver = new Unarchiver(assets, run);
		Collection<Path> tocs = unarchiver.getTocs();
		BatchRunner<Pair<AssetType, Pair<String, Long>>> runner = new BatchRunner<>("Analyzer");
		
		for(Path tocFile : tocs) {
			String tocName = tocFile.getFileName().toString();
			Batch<Pair<AssetType, Pair<String, Long>>> batch = new Batch<Pair<AssetType, Pair<String, Long>>>(tocName);
			Toc toc = new Toc(tocFile);
			List<Details> details = toc.getFiles();

			final QuickAccessArchive QArchive = unarchiver.getArchive(toc);
			for(Details resource : details) {
				String resourceName = resource.name();
				AssetType type = AssetType.getAssetType(resourceName);
				if(type == AssetType.UNRECOGNIZED) {
					throw new AssertionError("Unrecognized asset type: " + resourceName + " in " + tocName);
				}
				batch.addRunnable(() -> {
					return Pair.of(type, Pair.of(resource.typeCode(), resource.typeCodeInt()));
				});
			}
			runner.addBatch(batch);
		}
		
		runner.startBatch();
		Collection<Pair<AssetType, Pair<String, Long>>> results = runner.getResults();
		HashMap<AssetType, HashSet<String>> stringCodes = new HashMap<AssetType, HashSet<String>>();
		HashMap<AssetType, HashSet<Long>> intCodes = new HashMap<AssetType, HashSet<Long>>();
		for(AssetType type : AssetType.values()) {
			stringCodes.put(type, new HashSet<>());
			intCodes.put(type, new HashSet<>());
		}
		for(Pair<AssetType, Pair<String, Long>> pair : results) {
			Pair<String, Long> pair2 = pair.getValue();
			stringCodes.get(pair.getKey()).add(pair2.getKey());
			intCodes.get(pair.getKey()).add(pair2.getValue());
		}
		
		for(AssetType assetType : AssetType.values()) {
			HashSet<String> strings = stringCodes.get(assetType);
			HashSet<Long> ints = intCodes.get(assetType);
			System.out.println("============" + assetType.toString().toUpperCase() + "============");
			System.out.println("Analysis of '" + assetType + "' type:");
			System.out.println("Total string typecodes: " + strings.size());
			System.out.println("Total int typecodes: " + ints.size());
			System.out.println("List of string typecodes below: \n");
			stringCodes.get(assetType).forEach((forCode) -> {
				if(forCode.indexOf('\0') != -1) {
					System.out.print('[');
					for(char c : forCode.toCharArray()) {
						System.out.print(Integer.toHexString((int)c) + " ");
					}
					System.out.println(']');
				}
				else {
					System.out.println(forCode);
				}
			});
			System.out.println("List of int typecodes below: \n");
			intCodes.get(assetType).forEach((forCode) -> {
				System.out.println(forCode);
			});
		}
	}
	
}
