package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @version 1.02
 * @since 2025
 */
public class ListenerHandler {

	// Off by default -- debug-only console output (see setDebugMode); a caller
	// that wants the verbose "adding X in Y, owner: Z" registration trace opts
	// in explicitly rather than every consumer of this class paying for it by
	// default.
	private boolean debugMode = false;

	// ---------------------------------------------------------
	// Internal registration
	// ---------------------------------------------------------

	private static class Registration {

		/**
		 * Can be null, such as in classical addListener scenarios where the owner does
		 * not matter.
		 * 
		 * Wrapped in a WeakReference so that the GC can do its job in case the owner is
		 * supposed to be GC-ed.
		 * 
		 * The class that triggers adding the listener. This class may also want to
		 * remove this listener at the end-of-life of the listener, or the entire class
		 * itself.
		 */
		final WeakReference<Object> ownerRef;

		/**
		 * The listener that it added. It has the behavior of the listener type and may
		 * belong to a controlling class that adds/removes the listener to the
		 * ListenerHandler.
		 */
		final Object listener;

		/**
		 * Type of listener. Typically the listener not the type of class that is added
		 * here, implementing the listener.
		 * 
		 * Note: In case this class information feels overly complicated it can be
		 * removed from here. It is only needed by the fire method, where it can be
		 * fetched through a parameter.
		 */
		final Class<?> listenerType;

		/**
		 * Optional. To remove the listener also somewhere else. For example l ->
		 * scoreTypeChangeListeners.remove(l) in the calling class, if there a list of
		 * listeners exists.
		 */
		final Consumer<Object> remover;

		/**
		 * 
		 * @param listenerType Type of listener. Typically the listener not the type of
		 *                     class that is added here, implementing the listener.
		 * 
		 *                     Note: In case this class information feels overly
		 *                     complicated it can be removed from here. It is only
		 *                     needed by the fire method, where it can be fetched
		 *                     through a parameter.
		 * @param listener     The listener that it added. It has the behavior of the
		 *                     listener type and may belong to a controlling class that
		 *                     adds/removes the listener to the ListenerHandler.
		 * @param owner        The class that triggers adding the listener. This class
		 *                     may also want to remove this listener at the end-of-life
		 *                     of the listener, or the entire class itself.
		 * @param remover      Optional. To remove the listener also somewhere else. For
		 *                     example l -> scoreTypeChangeListeners.remove(l) in the
		 *                     calling class, if there a list of listeners exists.
		 */

		Registration(Class<?> listenerType, Object listener, Object owner, Consumer<Object> remover) {
			this.listenerType = Objects.requireNonNull(listenerType,
					"Listener type in ListenerHandler, Registration must not be null");
			this.listener = Objects.requireNonNull(listener,
					"Listener in ListenerHandler, Registration must not be null");
			this.ownerRef = owner != null ? new WeakReference<>(owner) : null;
			this.remover = remover;
		}

		boolean isOfOwner(Object owner) {
			return ownerRef != null && ownerRef.get() == owner;
		}

		boolean isOwnerCollected() {
			return ownerRef != null && ownerRef.get() == null;
		}

		@Override
		public String toString() {
			String l = listenerType != null ? listenerType.getSimpleName() : "unknown listener class";
			String o = ownerRef != null && ownerRef.get() != null ? ownerRef.get().getClass().getSimpleName()
					: "unknown";
			return l + " in " + listener.getClass().getSimpleName() + ", owner: " + o;
		}
	}

	private final List<Registration> registrations = Collections.synchronizedList(new ArrayList<>());

	// ---------------------------------------------------------
	// Add / Remove
	// ---------------------------------------------------------

	/**
	 * Adds a listener of a given type and an owner (e.g., a JFrame or panel).
	 * 
	 * @param <T>
	 * @param listenerType
	 * @param listener
	 * @param owner
	 * @param remover      for example l -> scoreTypeChangeListeners.remove(l) in
	 *                     the calling class, if there a list of listeners exists.
	 */
	public <T> void addListener(Class<T> listenerType, T listener, Object owner, Consumer<T> remover) {
		Registration r = new Registration(listenerType, listener, owner,
				remover != null ? o -> remover.accept(listenerType.cast(o)) : null);

		if (debugMode)
			System.out.println("ListenerHandler: adding " + r);

		registrations.add(r);
	}

	/** Removes a specific listener instance. */
	public void removeListener(Object listener) {
		if (listener == null)
			return;

		synchronized (registrations) {
			Iterator<Registration> it = registrations.iterator();
			while (it.hasNext()) {
				Registration reg = it.next();
				if (reg.listener == listener) {
					removeFromRemoverConsumer(reg);
					it.remove(); // always remove the registration itself
				}
			}
		}
	}

	/** Removes all listeners of a given listenerType. */
	public void removeAllListenersOfType(Class<?> listenerType) {
		if (listenerType == null)
			return;

		synchronized (registrations) {
			Iterator<Registration> it = registrations.iterator();
			while (it.hasNext()) {
				Registration reg = it.next();
				if (listenerType.equals(reg.listenerType)) {
					removeFromRemoverConsumer(reg);
					it.remove();
				}
			}
		}
	}

	/** Removes all listeners belonging to a specific owner. */
	public void removeAllListenersOfOwner(Object owner) {
		if (owner == null)
			return;

		synchronized (registrations) {
			Iterator<Registration> it = registrations.iterator();
			while (it.hasNext()) {
				Registration reg = it.next();
				if (reg.isOfOwner(owner)) {
					removeFromRemoverConsumer(reg);
					it.remove();
				}
			}
		}
	}

	/** Removes all listeners whose owner has been GC-collected. */
	public void cleanupCollectedOwners() {
		synchronized (registrations) {
			Iterator<Registration> it = registrations.iterator();
			while (it.hasNext()) {
				Registration reg = it.next();
				if (reg.isOwnerCollected()) {
					removeFromRemoverConsumer(reg);
					it.remove();
				}
			}
		}
	}

	/** Removes all listeners of all types. */
	public void clearAll() {
		synchronized (registrations) {
			for (Registration reg : registrations)
				removeFromRemoverConsumer(reg);
			registrations.clear();
		}
	}

	/**
	 * Safely invokes a remover (if present) and logs failures.
	 * 
	 * @param reg
	 */
	private void removeFromRemoverConsumer(Registration reg) {
		if (reg == null)
			return;
		if (reg.remover != null) {
			try {
				reg.remover.accept(reg.listener);
			} catch (Exception e) {
				System.err.println("Warning: remover failed for listener " + reg.listener + ": " + e);
			}
		}
	}

	// ---------------------------------------------------------
	// Event Dispatching
	// ---------------------------------------------------------

	/**
	 * Fires an event to all listeners of a given listener type.
	 *
	 * @param listenerType the listener interface class
	 * @param eventAction  what to do with each listener (e.g. call a method). For
	 *                     example: l -> l.scoreTypeChanged(event)
	 * @param <T>          type of listener
	 * @return number of listeners that fired.
	 */
	public <T> int fire(Class<T> listenerType, Consumer<T> eventAction) {
		List<Registration> snapshot;
		synchronized (registrations) {
			snapshot = new ArrayList<>(registrations);
		}

		int count = 0;
		for (Registration reg : snapshot) {
			if (reg.listenerType != null && reg.listenerType.equals(listenerType)) {
				try {
					// Memory logging retired from here -- it fired unconditionally with every
					// event dispatch whenever debugMode was on, which is a poor fit for leak
					// hunting (readings tied to arbitrary event volume, not to named loading
					// phases) and unrelated to what this method is actually debugging (listener
					// dispatch). Deliberate, labeled checkpoints (MemoryTools.logCheckpoint) at
					// meaningful phase boundaries replace it -- see e.g.
					// PostgreSQLTools.selectContainer in stocksapi.
					if (debugMode)
						System.out.println(getClass().getSimpleName() + ".fire: (" + (count + 1) + ") "
								+ listenerType.getSimpleName() + " in " + reg.listener.getClass().getSimpleName());

					eventAction.accept(listenerType.cast(reg.listener));
					count++;
				} catch (Exception ex) {
					ex.printStackTrace(); // prevent one listener from blocking others
				}
			}
		}

		return count;
	}

	// ---------------------------------------------------------
	// Diagnostics
	// ---------------------------------------------------------

	public int size() {
		return registrations.size();
	}

	public long countForOwner(Object owner) {
		synchronized (registrations) {
			return registrations.stream().filter(r -> r.isOfOwner(owner)).count();
		}
	}

	public long countOfType(Class<?> listenerType) {
		synchronized (registrations) {
			return registrations.stream().filter(r -> r.listenerType.equals(listenerType)).count();
		}
	}

	/**
	 * Returns a string representation of the current listener registrations, useful
	 * for debugging ownership or memory leak issues.
	 * 
	 * @return a formatted string listing all current registrations
	 */
	public String dump() {
		StringBuilder sb = new StringBuilder();
		synchronized (registrations) {
			sb.append("ListenerHandler dump (").append(registrations.size()).append(" total):\n");
			int i = 1;
			for (Registration r : registrations) {
				sb.append("  [").append(i++).append("] ").append(r.toString()).append('\n');
			}
		}
		return sb.toString();
	}

	public String dumpDetailed() {
		StringBuilder sb = new StringBuilder();
		synchronized (registrations) {
			sb.append("ListenerHandler dump (").append(registrations.size()).append(" total):\n");

			registrations.stream().map(r -> r.listenerType.getSimpleName()).distinct().forEach(type -> {
				long count = registrations.stream().filter(r -> r.listenerType.getSimpleName().equals(type)).count();
				sb.append("  ").append(type).append(": ").append(count).append('\n');
			});

			sb.append("\nDetailed list:\n");
			int i = 1;
			for (Registration r : registrations)
				sb.append("  [").append(i++).append("] ").append(r).append('\n');
		}
		return sb.toString();
	}

	/**
	 * For debugging memory leaks or ownership issues, this is very useful.
	 */
	public void printStatus() {
		System.out.println(dump());
	}

	/**
	 * For debugging memory leaks or ownership issues, this is very useful.
	 */
	public void printStatusDetailed() {
		System.out.println(dumpDetailed());
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
}