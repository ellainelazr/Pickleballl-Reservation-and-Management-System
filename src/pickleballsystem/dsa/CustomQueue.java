package pickleballsystem.dsa;

import pickleballsystem.dsa.SinglyLinkedList;

/**
 * DSA: QUEUE (first in, first out).
 * Used by SystemData as the waiting list: customers who want a full court slot are enqueued in the order they ask.
 */
public class CustomQueue<T> {
    private final SinglyLinkedList<T> storage = new SinglyLinkedList<>();

    public void enqueue(T value) {
        storage.addLast(value);
    }

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T value = storage.getFirst();
        storage.removeFirst();
        return value;
    }

    public T peek() {
        return storage.getFirst();
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public int size() {
        return storage.size();
    }
}
