package com.gamebuster19901.excite.modding.concurrent;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.Thread.State;

public class Batch<T> implements Batcher<T> {

	private final String name;
	private final Set<BatchedCallable<T>> runnables = new HashSet<>();
	private final LinkedHashSet<BatchListener> listeners = new LinkedHashSet<>();
	private volatile boolean accepting = true;
	
	public Batch(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void addRunnable(BatchedCallable<T> batchedCallable) {
		if(accepting) {
			runnables.add(batchedCallable);
			updateListeners();
		}
		else {
			notAccepting();
		}
	}
	
	@Override
	public void addRunnable(Callable<T> runnable) {
		if(accepting) {
			BatchedCallable<T> b = new BatchedCallable<>(this, runnable);
			runnables.add(new BatchedCallable<>(this, runnable));
			updateListeners();
		}
		else {
			notAccepting();
		}
	}

	@Override
	public void addRunnable(Runnable runnable) {
		if(accepting) {
			BatchedCallable<T> b = new BatchedCallable<>(this, runnable);
			runnables.add(new BatchedCallable<>(this, runnable));
			updateListeners();
		}
		else {
			notAccepting();
		}
	}
	
	@Override
	public void addBatchListener(BatchListener listener) {
		if(accepting) {
			if(!listeners.add(listener)) {
				System.out.println("Warning: duplicate batch listener ignored.");
			}
		}
		else {
			notAccepting();
		}
	}
	
	@Override
	public Collection<BatchedCallable<T>> getRunnables() {
		return Set.copyOf(runnables);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<BatchListener> getListeners() {
		return (Collection<BatchListener>) listeners.clone();
	}

	public void shutdownNow() throws InterruptedException {
		accepting = false;
		Shutdown shutdown = new Shutdown();
		for(BatchedCallable<T> r : runnables) {
			if(r.getState() == State.NEW) {
				r.shutdown(shutdown);
			}
		}
		System.err.println(runnables.size());
	}
	
	public void updateListeners() {
		for(BatchListener listener : listeners) {
			listener.update();
		}
	}
	
	private void notAccepting() {
		throw new IllegalStateException("Batch is not accepting new tasks or listeners.");
	}
	
	/**
	 * A wrapper class for a {@link Callable} object that participates in a batch execution managed by a {@link Batcher}.
	 * 
	 * This class allows for the creation of callables that can be tracked and managed by a batching system. It provides methods
	 * to get the execution state, retrieve the result after completion, and handle exceptions.
	 *
	 * @param <T> the type of the result produced by the wrapped {@link Callable}
	 */
	public static class BatchedCallable<T> implements Callable<T> {
		
		private final Batcher<T> batch;
		private final Callable<T> child;
		private volatile WeakReference<Thread> threadRef;
		protected volatile Throwable thrown;
		private final Object startLock = new Object();
		private volatile Boolean started = false;
		protected volatile boolean finished = false;
		protected volatile T result;
		
		
		/**
		 * Creates a new BatchedCallable instance for a provided {@link Runnable} object.
		 * <p>
		 * This convenience constructor takes a Runnable and converts it to a Callable that simply calls the {@link Runnable#run} method
		 * and returns null. It then delegates to the main constructor with the converted callable.
		 *
		 * @param batch the {@link Batcher} instance managing this callable
		 * @param r the {@link Runnable} object to be wrapped
		 */
		public BatchedCallable(Batcher<T> batch, Runnable r) {
			this(batch, () -> {
				r.run();
				return null;
			});
		}
		
		/**
		 * Creates a new BatchedCallable instance for the provided {@link Callable} object.
		 * <p>
		 * This constructor wraps a given Callable and associates it with the specified Batcher. The Batcher is
		 * notified of updates to the state of this callable.
		 *
		 * @param batch the {@link Batcher} instance managing this callable
		 * @param c the {@link Callable} object to be wrapped
		 */
		public BatchedCallable(Batcher<T> batch, Callable<T> c) {
			this.batch = batch;
			this.child = c;
			batch.updateListeners();
		}
		
		/**
		 * Implements the `call` method of the {@link Callable} interface.
		 * <p>
		 * This method executes the wrapped `Callable` object and stores the result. It also updates the state of this object
		 * and notifies the associated Batcher before and after execution. If any exceptions occur during execution, they are
		 * stored but not re-thrown by this method. The caller of this method is responsible for handling any exceptions.
		 * 
		 * The behavior of this callable is undefined if call() is executed more than once.
		 *
		 * @return the result of the wrapped callable's execution (which may be null), or null if an exception occurred
		 */
		@Override
		public T call() {
			Thread thread;
			try {
				thread = Thread.currentThread();
				threadRef = new WeakReference<>(thread);
				batch.updateListeners();
				result = child.call();
				return result;
			}
			catch(Throwable t) {
				this.thrown = t;
			}
			finally {
				finished = true;
				batch.updateListeners();
			}
			return null;
		}
		
		/**
		 * Gets the current execution state of the wrapped callable.
		 * 
		 * This method examines the internal state and thread reference to determine the current execution state. It can return one of the following states:
		 *
		 * <ul>
		 *   <li><b>NEW:</b> The callable has not yet been submitted for execution.
		 *   <li><b>TERMINATED:</b> The callable has finished execution, either successfully or with an exception.
		 *   <li><b>The actual state of the thread running the callable (e.g., `RUNNING`, `WAITING`):</b> 
		 *   <p style="margin-left: 1em">If a thread is currently executing the callable, this state reflects the thread's lifecycle.
		 * </ul>
		 *
		 * @return the current state of the callable execution, as described above
		 */
		public State getState() {
			if(finished) { //the thread is no longer working on this runnable
				return State.TERMINATED;
			}
			if(threadRef == null) {
				return State.NEW;
			}
			Thread thread = threadRef.get();
			if(thread == null) {
				return State.TERMINATED; //thread has been garbage collected, so it has been terminated.
			}
			return thread.getState();
		}
		
		/**
		  * Retrieves the result of the computation after the {@link #call} method has finished executing.
		  *
		  * @throws IllegalStateException if the {@link #call} method has not yet finished executing.
		  * @return the result of the computation (which may be null), or {@code null} if the computation threw an exception.
		*/
		public T getResult() {
			if(!finished) {
				throw new IllegalStateException("Cannot obtain the result before it is calculated!");
			}
			return result;
		}
		
		/**
		 * Gets the exception thrown by the wrapped callable, if any.
		 * <p>
		 * This method returns the exception that was thrown during the execution of the wrapped callable, or null if no exception
		 * occurred.
		 *
		 * @return the exception thrown by the wrapped callable, or null if no exception occurred
		 */
		public Throwable getThrown() {
			return thrown;
		}
		
		/**
		 * Sets the state of this callable to a proper shutdown state
		 * 
		 * @param shutdown a dummy exception representing that this thread has failed to complete due to being shutdown.
		 */
		protected void shutdown(Shutdown shutdown) {
			finished = true;
			thrown = shutdown;
		}
	}

 }
