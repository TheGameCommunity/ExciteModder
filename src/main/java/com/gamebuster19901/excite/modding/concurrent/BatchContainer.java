package com.gamebuster19901.excite.modding.concurrent;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.gamebuster19901.excite.modding.concurrent.Batch.BatchedCallable;

public interface BatchContainer<T> {
	
	public abstract String getName();

	public Collection<BatchedCallable<T>> getRunnables();
	
	public default Collection<T> getResults() throws IllegalStateException {
		LinkedHashSet<T> results = new LinkedHashSet<>();
		for(BatchedCallable<T> callable : getRunnables()) {
			T result = callable.getResult();
			if(result != null || (result == null && callable.getThrown() == null)) {
				results.add(result);
			}
		}
		return results;
	}
	
	public Collection<BatchListener> getListeners();
	
	public void addBatchListener(BatchListener listener);
	
	public default void updateListeners() {
		for(BatchListener l : getListeners()) {
			l.update();
		}
	}
	
	public void shutdownNow() throws InterruptedException;
	
}
