package com.interview.datastructures;

/**
 * Generic Singly Linked List Implementation
 *
 * A fundamental data structure consisting of nodes where each node contains data
 * and a reference to the next node. This implementation provides common operations
 * for linked list manipulation and is commonly asked about in technical interviews.
 *
 * Key characteristics:
 * - Dynamic size (grows/shrinks during runtime)
 * - No random access (must traverse from head)
 * - Efficient insertion/deletion at beginning (O(1))
 * - Memory efficient (only allocates what's needed)
 *
 * Time Complexities:
 * - Access: O(n)
 * - Search: O(n)
 * - Insertion: O(1) at head, O(n) at arbitrary position
 * - Deletion: O(1) at head, O(n) at arbitrary position
 *
 * Space Complexity: O(n)
 *
 * @param <T> the type of elements stored in the linked list
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class LinkedList<T> {
    /** Reference to the first node in the list */
    private Node<T> head;

    /** Current number of elements in the list */
    private int size;

    /**
     * Internal Node class representing each element in the linked list
     *
     * Each node contains:
     * - data: the actual value stored
     * - next: reference to the next node in the sequence
     *
     * This is a static nested class to avoid holding unnecessary
     * references to the outer LinkedList instance.
     *
     * @param <T> the type of data stored in the node
     */
    private static class Node<T> {
        /** The data stored in this node */
        T data;

        /** Reference to the next node in the list */
        Node<T> next;

        /**
         * Constructs a new node with the given data
         *
         * @param data the data to store in this node
         */
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    /**
     * Adds an element to the end of the linked list
     *
     * This method traverses the entire list to find the last node,
     * making it O(n) time complexity. For better performance when
     * frequently adding to the end, consider maintaining a tail pointer.
     *
     * Algorithm:
     * 1. Create a new node with the given data
     * 2. If list is empty, make new node the head
     * 3. Otherwise, traverse to the last node
     * 4. Set the last node's next to point to new node
     * 5. Increment size counter
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param data the element to add to the list
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    /**
     * Adds an element to the beginning of the linked list
     *
     * This is the most efficient way to insert into a linked list
     * since we don't need to traverse to find the insertion point.
     *
     * Algorithm:
     * 1. Create a new node with the given data
     * 2. Set new node's next to current head
     * 3. Update head to point to new node
     * 4. Increment size counter
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @param data the element to add to the beginning of the list
     */
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = head;
        head = newNode;
        size++;
    }

    /**
     * Removes and returns the element at the specified index
     *
     * This method handles two cases:
     * 1. Removing the head node (index 0) - O(1) operation
     * 2. Removing any other node - O(n) operation requiring traversal
     *
     * Algorithm:
     * 1. Validate index bounds
     * 2. If index is 0, remove head and update head pointer
     * 3. Otherwise, traverse to node before target
     * 4. Update next pointer to skip target node
     * 5. Return removed node's data and decrement size
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param index the index of the element to remove
     * @return the data that was removed
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == 0) {
            T data = head.data;
            head = head.next;
            size--;
            return data;
        }

        Node<T> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        T data = current.next.data;
        current.next = current.next.next;
        size--;
        return data;
    }

    /**
     * Reverses the linked list in-place
     *
     * This is a classic interview question that tests understanding
     * of pointer manipulation. The algorithm uses three pointers
     * to reverse the direction of all next pointers.
     *
     * Algorithm (Three-pointer technique):
     * 1. Initialize prev = null, current = head, next = null
     * 2. While current is not null:
     *    a. Store next node
     *    b. Reverse current node's pointer
     *    c. Move prev and current one step forward
     * 3. Update head to prev (new first node)
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * Example:
     * Before: 1 -> 2 -> 3 -> null
     * After:  null <- 1 <- 2 <- 3
     */
    public void reverse() {
        Node<T> prev = null;
        Node<T> current = head;
        Node<T> next = null;

        while (current != null) {
            next = current.next;        // Store next node
            current.next = prev;        // Reverse the link
            prev = current;             // Move prev forward
            current = next;             // Move current forward
        }
        head = prev;                    // Update head to new first node
    }

    /**
     * Detects if the linked list contains a cycle using Floyd's Algorithm
     *
     * Also known as the "tortoise and hare" algorithm, this method uses
     * two pointers moving at different speeds to detect cycles efficiently.
     * This is a popular interview question testing algorithm knowledge.
     *
     * Algorithm (Floyd's Cycle Detection):
     * 1. Use two pointers: slow (moves 1 step) and fast (moves 2 steps)
     * 2. If there's a cycle, fast will eventually meet slow
     * 3. If fast reaches null, there's no cycle
     *
     * Why this works:
     * - If there's a cycle, both pointers will eventually be inside it
     * - Fast gains on slow by 1 position each iteration
     * - They must meet when fast "laps" slow
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @return true if the list contains a cycle, false otherwise
     */
    public boolean hasCycle() {
        if (head == null) return false;

        Node<T> slow = head;           // Tortoise: moves 1 step
        Node<T> fast = head.next;      // Hare: moves 2 steps

        while (fast != null && fast.next != null) {
            if (slow == fast) return true;  // Cycle detected
            slow = slow.next;               // Move slow 1 step
            fast = fast.next.next;          // Move fast 2 steps
        }
        return false;                       // No cycle found
    }

    /**
     * Retrieves the element at the specified index without removing it
     *
     * Since linked lists don't support random access like arrays,
     * we must traverse from the head to reach the desired index.
     * This makes access operations inefficient compared to arrays.
     *
     * Algorithm:
     * 1. Validate index bounds
     * 2. Traverse from head, counting steps
     * 3. Return data when target index is reached
     *
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     *
     * @param index the index of the element to retrieve
     * @return the data at the specified index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Returns the number of elements in the linked list
     *
     * We maintain a size counter to provide O(1) size queries.
     * Alternative approach would be traversing the list each time (O(n)).
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @return the number of elements in the list
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the linked list is empty
     *
     * A list is empty when head points to null.
     * We could also check if size == 0, but checking head
     * is more direct and doesn't rely on size counter accuracy.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @return true if the list is empty, false otherwise
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Returns a string representation of the linked list
     *
     * Creates a string showing all elements in order, formatted
     * as [element1, element2, element3]. This method traverses
     * the entire list to build the string representation.
     *
     * Algorithm:
     * 1. Create StringBuilder with opening bracket
     * 2. Traverse list, appending each element
     * 3. Add commas between elements (but not after last)
     * 4. Add closing bracket and return
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n) - for the string being built
     *
     * @return string representation of the list
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}