package com.gamebuster19901.excite.modding.concurrent;

import java.util.Collection;

public interface BatcherContainer<T> extends BatchContainer<T>{

	public abstract Collection<Batcher<T>> getBatches();
	
	public abstract void addBatch(Batcher<T> batcher);
	
}
