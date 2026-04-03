package com.gamebuster19901.excite.modding.concurrent;

import java.util.Collection;
import java.util.concurrent.Callable;

public interface Batcher<T> extends BatchContainer<T> {
	
	public abstract void addRunnable(Callable<T> runnable);

	public abstract void addRunnable(Runnable runnable);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public default void addRunnables(Collection<?> runnables) {
		if(runnables.size() > 0) {
			Object o = runnables.iterator().next();
			if(o instanceof Callable) {
				runnables.forEach((r) -> {
					addRunnable((Callable)r);
				});
			}
			else if (o instanceof Runnable) {
				runnables.forEach((r) -> {
					addRunnable((Runnable)r);
				});
			}
			else {
				throw new IllegalArgumentException(o.getClass().toString());
			}
		}
		else {
			return;
		}
	}
	
}
