package com.gamebuster19901.excite.modding.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.gamebuster19901.excite.modding.concurrent.Batch.BatchedCallable;

public class BatchRunner<T> implements BatchWorker<T>, BatcherContainer<T> {

	private final String name;
	private final ExecutorService executor;
	private final LinkedHashSet<Batcher<T>> batches = new LinkedHashSet<Batcher<T>>();
	private volatile boolean listenerAdded = false;
	
    public BatchRunner(String name) {
    	this(name, Runtime.getRuntime().availableProcessors());
    }
    
    public BatchRunner(String name, int threads) throws IllegalArgumentException {
    	this(name, Executors.newFixedThreadPool(threads));
    }
	
	public BatchRunner(String name, ExecutorService executor) {
		this.name = name;
		this.executor = executor;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addBatch(Batcher<T> batcher) {
		synchronized(executor) {
			if(listenerAdded) {
				throw new IllegalStateException("Cannot add a batch after a BatchListener has been added! Add all batches before adding listeners!");
			}
			if(executor.isShutdown()) {
				throw new IllegalStateException("Cannot add more batches after the batch runner has shut down!");
			}
			synchronized(batches) {
				batches.add(batcher);
			}
		}
	}

	@Override
	public void startBatch() throws InterruptedException {
		synchronized(executor) {
			if(executor.isShutdown()) {
				throw new IllegalStateException("BatchRunner has already been started!");
			}
			synchronized(batches) {
				ArrayList<BatchedCallable<T>> batchers = new ArrayList<>(getRunnables());
				Collections.shuffle(batchers); //So multiple threads don't wait for a single archive to lazily load, allows multiple archives to lazily load at a time
				executor.invokeAll(batchers);
				executor.close();
			}
		}
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public void shutdownNow() throws InterruptedException {
        executor.shutdownNow(); // Force shutdown of any remaining tasks
        try {
        	executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        finally {
	        for(Batcher<T> batch : batches) {
	        	batch.shutdownNow();
	        }
        }
	}

	@Override
	public Collection<BatchedCallable<T>> getRunnables() {
		LinkedHashSet<BatchedCallable<T>> ret = new LinkedHashSet<>();
		for(Batcher<T> batch : batches) {
			ret.addAll(batch.getRunnables());
		}
		return ret;
	}

	@Override
	public Collection<BatchListener> getListeners() {
		LinkedHashSet<BatchListener> ret = new LinkedHashSet<>();
		for(Batcher<T> batch : batches) {
			ret.addAll(batch.getListeners());
		}
		return ret;
	}
	
	@Override
	public void addBatchListener(BatchListener listener) {
		if(!listenerAdded) {
			listenerAdded = true;
		}
		for(Batcher<T> batch : batches) {
			batch.addBatchListener(listener);
		}
	}
	
	@Override
	public Collection<Batcher<T>> getBatches() {
		return (Collection<Batcher<T>>) batches.clone();
	}
	
}
