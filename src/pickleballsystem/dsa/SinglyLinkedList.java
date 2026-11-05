package pickleballsystem.dsa;

import java.util.ArrayList;

/**
 * DSA: LINKED LIST.
 * A singly linked list built from nodes. CustomQueue, CustomStack, and ReservationHistory all store their items in it.
 */
public class SinglyLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    public void addLast(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public boolean removeFirst() {
        if (isEmpty()) {
            return false;
        }
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return true;
    }

    public T getFirst() {
        return isEmpty() ? null : head.value;
    }

    public boolean removeLast() {
        if (isEmpty()) {
            return false;
        }
        if (size == 1) {
            head = null;
            tail = null;
            size = 0;
            return true;
        }
        Node<T> current = head;
        while (current.next != tail) {
            current = current.next;
        }
        current.next = null;
        tail = current;
        size--;
        return true;
    }

    public boolean remove(T value) {
        if (isEmpty()) {
            return false;
        }
        if (head.value == null ? value == null : head.value.equals(value)) {
            return removeFirst();
        }
        Node<T> current = head;
        while (current.next != null) {
            if (current.next.value == null ? value == null : current.next.value.equals(value)) {
                current.next = current.next.next;
                if (current.next == null) {
                    tail = current;
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public T find(T value) {
        Node<T> current = head;
        while (current != null) {
            if (current.value == null ? value == null : current.value.equals(value)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public boolean contains(T value) {
        Node<T> current = head;
        while (current != null) {
            if (valuesEqual(current.value, value)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    private boolean valuesEqual(T left, T right) {
        return left == null ? right == null : left.equals(right);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public ArrayList<T> toArrayList() {
        ArrayList<T> result = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            result.add(current.value);
            current = current.next;
        }
        return result;
    }

    private static final class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }
}
