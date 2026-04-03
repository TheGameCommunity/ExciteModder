package com.gamebuster19901.excite.modding.concurrent;

import java.util.concurrent.TimeUnit;

public interface BatchWorker<T> extends BatchContainer<T> {

	public abstract void startBatch() throws InterruptedException;
	
	public abstract boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
	
	public void shutdownNow() throws InterruptedException;
	
}
