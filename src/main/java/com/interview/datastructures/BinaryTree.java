package com.interview.datastructures;

import java.util.*;

/**
 * Generic Binary Search Tree (BST) Implementation
 *
 * A binary search tree is a hierarchical data structure where each node has at most
 * two children, and the tree maintains the BST property: for any node, all values in
 * the left subtree are smaller, and all values in the right subtree are larger.
 *
 * Key characteristics:
 * - Hierarchical structure with parent-child relationships
 * - Self-organizing: maintains sorted order automatically
 * - Efficient searching, insertion, and deletion operations
 * - In-order traversal yields sorted sequence
 *
 * Time Complexities (Average case for balanced tree):
 * - Search: O(log n)
 * - Insert: O(log n)
 * - Delete: O(log n)
 * - Traversal: O(n)
 *
 * Time Complexities (Worst case - degenerate tree):
 * - All operations: O(n)
 *
 * Space Complexity: O(n) for storage, O(log n) for recursion stack in balanced tree
 *
 * @param <T> the type of elements stored in the tree, must be Comparable
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class BinaryTree<T extends Comparable<T>> {
    /** Reference to the root node of the tree */
    private TreeNode<T> root;

    /**
     * Internal TreeNode class representing each node in the binary tree
     *
     * Each node contains:
     * - data: the actual value stored
     * - left: reference to the left child (smaller values)
     * - right: reference to the right child (larger values)
     *
     * This is a static nested class to avoid holding unnecessary
     * references to the outer BinaryTree instance.
     *
     * @param <T> the type of data stored in the node
     */
    private static class TreeNode<T> {
        /** The data stored in this node */
        T data;

        /** Reference to the left child node (contains smaller values) */
        TreeNode<T> left;

        /** Reference to the right child node (contains larger values) */
        TreeNode<T> right;

        /**
         * Constructs a new tree node with the given data
         *
         * @param data the data to store in this node
         */
        TreeNode(T data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Inserts a new element into the binary search tree
     *
     * This method maintains the BST property by placing smaller values
     * to the left and larger values to the right. Duplicate values
     * are ignored in this implementation.
     *
     * The method uses recursion to find the correct insertion point.
     *
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(log n) for recursion stack
     *
     * @param data the element to insert into the tree
     */
    public void insert(T data) {
        root = insertRec(root, data);
    }

    /**
     * Recursive helper method for inserting elements
     *
     * This method implements the core BST insertion algorithm:
     * 1. If current node is null, create new node here
     * 2. If new data is smaller, go left
     * 3. If new data is larger, go right
     * 4. If equal, ignore (no duplicates)
     *
     * Algorithm maintains BST invariant at each step.
     *
     * @param root the current node being examined
     * @param data the data to insert
     * @return the root of the subtree after insertion
     */
    private TreeNode<T> insertRec(TreeNode<T> root, T data) {
        // Base case: found insertion point
        if (root == null) {
            return new TreeNode<>(data);
        }

        // Recursive case: navigate to correct subtree
        if (data.compareTo(root.data) < 0) {
            root.left = insertRec(root.left, data);   // Insert in left subtree
        } else if (data.compareTo(root.data) > 0) {
            root.right = insertRec(root.right, data); // Insert in right subtree
        }
        // If equal, do nothing (no duplicates allowed)

        return root;
    }

    /**
     * Searches for an element in the binary search tree
     *
     * Uses the BST property to efficiently navigate to the target value.
     * At each node, compares the target with current data and goes
     * left (if smaller) or right (if larger).
     *
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(log n) for recursion stack
     *
     * @param data the element to search for
     * @return true if the element is found, false otherwise
     */
    public boolean search(T data) {
        return searchRec(root, data);
    }

    /**
     * Recursive helper method for searching elements
     *
     * This method implements binary search on the BST:
     * 1. If current node is null, element not found
     * 2. If data matches current node, found
     * 3. If data is smaller, search left subtree
     * 4. If data is larger, search right subtree
     *
     * @param root the current node being examined
     * @param data the data to search for
     * @return true if found, false otherwise
     */
    private boolean searchRec(TreeNode<T> root, T data) {
        // Base case: reached null (not found)
        if (root == null) {
            return false;
        }

        // Base case: found the data
        if (data.equals(root.data)) {
            return true;
        }

        // Recursive case: search appropriate subtree
        if (data.compareTo(root.data) < 0) {
            return searchRec(root.left, data);   // Search left subtree
        }
        return searchRec(root.right, data);      // Search right subtree
    }

    /**
     * Performs in-order traversal of the binary search tree
     *
     * In-order traversal visits nodes in this order: Left -> Root -> Right
     * For a BST, this produces elements in sorted ascending order.
     * This is one of the key properties that makes BSTs useful.
     *
     * Algorithm:
     * 1. Recursively traverse left subtree
     * 2. Visit current node (add to result)
     * 3. Recursively traverse right subtree
     *
     * Time Complexity: O(n) - visits every node once
     * Space Complexity: O(n) for result list + O(log n) for recursion
     *
     * @return list of elements in sorted order
     */
    public List<T> inorderTraversal() {
        List<T> result = new ArrayList<>();
        inorderRec(root, result);
        return result;
    }

    /**
     * Recursive helper for in-order traversal
     *
     * @param root the current node being processed
     * @param result the list to accumulate results
     */
    private void inorderRec(TreeNode<T> root, List<T> result) {
        if (root != null) {
            inorderRec(root.left, result);    // Traverse left subtree
            result.add(root.data);           // Visit current node
            inorderRec(root.right, result);  // Traverse right subtree
        }
    }

    /**
     * Performs pre-order traversal of the binary search tree
     *
     * Pre-order traversal visits nodes in this order: Root -> Left -> Right
     * This traversal is useful for copying/serializing tree structure
     * or prefix expression evaluation.
     *
     * Algorithm:
     * 1. Visit current node (add to result)
     * 2. Recursively traverse left subtree
     * 3. Recursively traverse right subtree
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n) for result + O(log n) for recursion
     *
     * @return list of elements in pre-order
     */
    public List<T> preorderTraversal() {
        List<T> result = new ArrayList<>();
        preorderRec(root, result);
        return result;
    }

    /**
     * Recursive helper for pre-order traversal
     *
     * @param root the current node being processed
     * @param result the list to accumulate results
     */
    private void preorderRec(TreeNode<T> root, List<T> result) {
        if (root != null) {
            result.add(root.data);           // Visit current node
            preorderRec(root.left, result);  // Traverse left subtree
            preorderRec(root.right, result); // Traverse right subtree
        }
    }

    /**
     * Performs post-order traversal of the binary search tree
     *
     * Post-order traversal visits nodes in this order: Left -> Right -> Root
     * This traversal is useful for deleting tree nodes safely
     * or postfix expression evaluation.
     *
     * Algorithm:
     * 1. Recursively traverse left subtree
     * 2. Recursively traverse right subtree
     * 3. Visit current node (add to result)
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n) for result + O(log n) for recursion
     *
     * @return list of elements in post-order
     */
    public List<T> postorderTraversal() {
        List<T> result = new ArrayList<>();
        postorderRec(root, result);
        return result;
    }

    /**
     * Recursive helper for post-order traversal
     *
     * @param root the current node being processed
     * @param result the list to accumulate results
     */
    private void postorderRec(TreeNode<T> root, List<T> result) {
        if (root != null) {
            postorderRec(root.left, result);  // Traverse left subtree
            postorderRec(root.right, result); // Traverse right subtree
            result.add(root.data);           // Visit current node
        }
    }

    /**
     * Performs level-order (breadth-first) traversal of the binary tree
     *
     * Level-order traversal visits all nodes at depth 0, then depth 1,
     * then depth 2, etc. This produces a list of lists where each
     * inner list contains all nodes at the same level.
     *
     * Algorithm (using queue):
     * 1. Start with root in queue
     * 2. For each level:
     *    a. Process all nodes currently in queue
     *    b. Add their children to queue for next level
     * 3. Continue until queue is empty
     *
     * Time Complexity: O(n)
     * Space Complexity: O(w) where w is maximum width of tree
     *
     * @return list of lists, each containing nodes at the same level
     */
    public List<List<T>> levelOrder() {
        List<List<T>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode<T>> queue = new java.util.LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();    // Number of nodes at current level
            List<T> level = new ArrayList<>();

            // Process all nodes at current level
            for (int i = 0; i < levelSize; i++) {
                TreeNode<T> node = queue.poll();
                level.add(node.data);

                // Add children for next level
                if (node.left != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }
            result.add(level);
        }
        return result;
    }

    /**
     * Calculates the height of the binary tree
     *
     * Height is defined as the number of edges on the longest path
     * from root to a leaf. An empty tree has height 0, and a tree
     * with only root has height 1.
     *
     * Algorithm (recursive):
     * 1. If node is null, height is 0
     * 2. Otherwise, height is 1 + max(leftHeight, rightHeight)
     *
     * Time Complexity: O(n) - visits every node
     * Space Complexity: O(log n) for recursion stack in balanced tree
     *
     * @return the height of the tree
     */
    public int height() {
        return heightRec(root);
    }

    /**
     * Recursive helper for calculating height
     *
     * @param root the current node
     * @return height of subtree rooted at this node
     */
    private int heightRec(TreeNode<T> root) {
        if (root == null) {
            return 0;
        }
        return 1 + Math.max(heightRec(root.left), heightRec(root.right));
    }

    /**
     * Checks if the binary tree is height-balanced
     *
     * A tree is balanced if for every node, the heights of its two
     * subtrees differ by at most 1. This is the definition used
     * by AVL trees, which maintain balance for optimal performance.
     *
     * Algorithm:
     * - Use modified height calculation
     * - Return -1 if any subtree is unbalanced
     * - Otherwise return actual height
     *
     * Time Complexity: O(n)
     * Space Complexity: O(log n) for recursion
     *
     * @return true if tree is balanced, false otherwise
     */
    public boolean isBalanced() {
        return isBalancedRec(root) != -1;
    }

    /**
     * Recursive helper for balance checking
     *
     * Returns height if balanced, -1 if unbalanced
     *
     * @param root the current node
     * @return height if balanced, -1 if unbalanced
     */
    private int isBalancedRec(TreeNode<T> root) {
        if (root == null) return 0;

        // Check if left subtree is balanced
        int leftHeight = isBalancedRec(root.left);
        if (leftHeight == -1) return -1;

        // Check if right subtree is balanced
        int rightHeight = isBalancedRec(root.right);
        if (rightHeight == -1) return -1;

        // Check if current node is balanced
        if (Math.abs(leftHeight - rightHeight) > 1) return -1;

        return Math.max(leftHeight, rightHeight) + 1;
    }

    /**
     * Finds the minimum element in the binary search tree
     *
     * In a BST, the minimum element is always the leftmost node.
     * We keep going left until we find a node with no left child.
     *
     * Algorithm:
     * 1. Start at root
     * 2. Keep going left while left child exists
     * 3. Return data of leftmost node
     *
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(1)
     *
     * @return the minimum element, or null if tree is empty
     */
    public T findMin() {
        if (root == null) return null;
        TreeNode<T> current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current.data;
    }

    /**
     * Finds the maximum element in the binary search tree
     *
     * In a BST, the maximum element is always the rightmost node.
     * We keep going right until we find a node with no right child.
     *
     * Algorithm:
     * 1. Start at root
     * 2. Keep going right while right child exists
     * 3. Return data of rightmost node
     *
     * Time Complexity: O(log n) average, O(n) worst case
     * Space Complexity: O(1)
     *
     * @return the maximum element, or null if tree is empty
     */
    public T findMax() {
        if (root == null) return null;
        TreeNode<T> current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.data;
    }
}