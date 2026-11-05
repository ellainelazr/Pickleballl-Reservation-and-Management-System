package pickleballsystem.dsa;

import pickleballsystem.dsa.SinglyLinkedList;

/**
 * DSA: STACK (last in, first out).
 * Built on SinglyLinkedList. push adds to the front and pop removes from the front.
 */
public class CustomStack<T> {
    private final SinglyLinkedList<T> storage = new SinglyLinkedList<>();

    public void push(T value) {
        storage.addFirst(value);
    }

    public T pop() {
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
