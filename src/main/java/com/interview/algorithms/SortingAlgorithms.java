package com.interview.algorithms;

import java.util.Arrays;

/**
 * Comprehensive collection of sorting algorithms with detailed explanations
 *
 * This class contains implementations of the most important sorting algorithms
 * commonly asked about in technical interviews. Each algorithm includes:
 * - Detailed explanation of the approach
 * - Time and space complexity analysis
 * - Best/average/worst case scenarios
 * - When to use each algorithm
 *
 * Sorting Algorithm Summary:
 * ┌─────────────────┬──────────────┬──────────────┬──────────────┬───────────────┬──────────┐
 * │   Algorithm     │   Best Case  │ Average Case │  Worst Case  │ Space Complex │  Stable  │
 * ├─────────────────┼──────────────┼──────────────┼──────────────┼───────────────┼──────────┤
 * │ Quick Sort      │   O(n log n) │  O(n log n)  │    O(n²)     │    O(log n)   │    No    │
 * │ Merge Sort      │   O(n log n) │  O(n log n)  │  O(n log n)  │      O(n)     │   Yes    │
 * │ Heap Sort       │   O(n log n) │  O(n log n)  │  O(n log n)  │      O(1)     │    No    │
 * │ Bubble Sort     │      O(n)    │     O(n²)    │     O(n²)    │      O(1)     │   Yes    │
 * │ Insertion Sort  │      O(n)    │     O(n²)    │     O(n²)    │      O(1)     │   Yes    │
 * │ Selection Sort  │     O(n²)    │     O(n²)    │     O(n²)    │      O(1)     │    No    │
 * └─────────────────┴──────────────┴──────────────┴──────────────┴───────────────┴──────────┘
 *
 * @author Interview Preparation
 * @version 1.0
 */
public class SortingAlgorithms {

    /**
     * Quick Sort - Divide and Conquer Algorithm
     *
     * Quick Sort is one of the most efficient sorting algorithms and is widely used
     * in practice. It works by selecting a 'pivot' element and partitioning the array
     * around it, then recursively sorting the sub-arrays.
     *
     * How it works:
     * 1. Choose a pivot element (last element in this implementation)
     * 2. Partition: rearrange array so elements < pivot are on left, > pivot on right
     * 3. Recursively apply the same process to sub-arrays
     *
     * Advantages:
     * - Very fast in practice (average O(n log n))
     * - In-place sorting (minimal extra memory)
     * - Cache-efficient due to good locality of reference
     *
     * Disadvantages:
     * - Worst case O(n²) when pivot is always min/max
     * - Not stable (doesn't preserve relative order of equal elements)
     * - Performance depends heavily on pivot selection
     *
     * Time Complexity:
     * - Best Case: O(n log n) - when pivot divides array into equal halves
     * - Average Case: O(n log n) - with random pivot selection
     * - Worst Case: O(n²) - when array is already sorted or reverse sorted
     *
     * Space Complexity: O(log n) - for recursion stack in average case
     *
     * @param arr the array to sort
     * @param low starting index
     * @param high ending index
     */
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // Partition the array and get the pivot index
            // After partition: elements[low...pi-1] <= elements[pi] <= elements[pi+1...high]
            int pi = partition(arr, low, high);

            // Recursively sort elements before and after partition
            quickSort(arr, low, pi - 1);    // Sort left sub-array
            quickSort(arr, pi + 1, high);   // Sort right sub-array
        }
    }

    /**
     * Partitioning function for Quick Sort (Lomuto partition scheme)
     *
     * This function takes the last element as pivot, places it at its correct
     * position in sorted array, and places all smaller elements to left of pivot
     * and all greater elements to right of pivot.
     *
     * Algorithm:
     * 1. Choose pivot as last element
     * 2. Keep track of index of smaller element
     * 3. Traverse through array, if element <= pivot, swap with smaller element
     * 4. Finally, place pivot in its correct position
     *
     * @param arr array to partition
     * @param low starting index
     * @param high ending index
     * @return index of pivot after partitioning
     */
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];    // Choose rightmost element as pivot
        int i = (low - 1);        // Index of smaller element, indicates right position of pivot

        for (int j = low; j < high; j++) {
            // If current element is smaller than or equal to pivot
            if (arr[j] <= pivot) {
                i++;              // Increment index of smaller element
                swap(arr, i, j);  // Swap elements
            }
        }
        swap(arr, i + 1, high);   // Place pivot in correct position
        return i + 1;             // Return pivot index
    }

    /**
     * Merge Sort - Stable Divide and Conquer Algorithm
     *
     * Merge Sort is a stable, comparison-based sorting algorithm that uses
     * divide-and-conquer approach. It consistently performs in O(n log n) time
     * regardless of input distribution.
     *
     * How it works:
     * 1. Divide: Split array into two halves
     * 2. Conquer: Recursively sort both halves
     * 3. Combine: Merge the sorted halves
     *
     * Advantages:
     * - Guaranteed O(n log n) performance (stable performance)
     * - Stable sorting (maintains relative order of equal elements)
     * - Predictable behavior regardless of input
     * - Parallelizable (different sub-arrays can be sorted independently)
     *
     * Disadvantages:
     * - Requires O(n) extra space for merging
     * - Not in-place
     * - Slightly slower than Quick Sort in practice due to overhead
     *
     * Time Complexity: O(n log n) in all cases
     * Space Complexity: O(n) - for temporary arrays during merging
     *
     * @param arr array to sort
     * @param left starting index
     * @param right ending index
     */
    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            // Find the middle point to divide array into two halves
            int mid = left + (right - left) / 2;  // Avoids integer overflow

            // Recursively sort first and second halves
            mergeSort(arr, left, mid);      // Sort left half
            mergeSort(arr, mid + 1, right); // Sort right half

            // Merge the sorted halves
            merge(arr, left, mid, right);
        }
    }

    /**
     * Merges two sorted sub-arrays into a single sorted array
     *
     * This function merges two sorted sub-arrays arr[left...mid] and arr[mid+1...right]
     * The merge process maintains the sorted order while combining the arrays.
     *
     * Algorithm:
     * 1. Create temporary arrays for left and right sub-arrays
     * 2. Copy data to temporary arrays
     * 3. Compare elements from both arrays and merge in sorted order
     * 4. Copy remaining elements if any
     *
     * @param arr main array containing both sub-arrays
     * @param left starting index of left sub-array
     * @param mid ending index of left sub-array
     * @param right ending index of right sub-array
     */
    private static void merge(int[] arr, int left, int mid, int right) {
        // Calculate sizes of sub-arrays to be merged
        int n1 = mid - left + 1;  // Size of left sub-array
        int n2 = right - mid;     // Size of right sub-array

        // Create temporary arrays
        int[] L = new int[n1];    // Left sub-array
        int[] R = new int[n2];    // Right sub-array

        // Copy data to temporary arrays
        System.arraycopy(arr, left, L, 0, n1);      // Copy left sub-array
        System.arraycopy(arr, mid + 1, R, 0, n2);   // Copy right sub-array

        // Merge the temporary arrays back into arr[left...right]
        int i = 0, j = 0, k = left;  // Initial indexes

        // Compare and merge elements in sorted order
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k++] = L[i++];  // Take from left array
            } else {
                arr[k++] = R[j++];  // Take from right array
            }
        }

        // Copy remaining elements from left array, if any
        while (i < n1) {
            arr[k++] = L[i++];
        }

        // Copy remaining elements from right array, if any
        while (j < n2) {
            arr[k++] = R[j++];
        }
    }

    /**
     * Heap Sort - In-place sorting using heap data structure
     *
     * Heap Sort is a comparison-based sorting algorithm that uses a binary heap
     * data structure. It's an improvement over selection sort with better time complexity.
     *
     * How it works:
     * 1. Build a max heap from input array
     * 2. Repeatedly extract maximum element and place at end
     * 3. Maintain heap property after each extraction
     *
     * Advantages:
     * - Guaranteed O(n log n) performance
     * - In-place sorting (O(1) extra space)
     * - Not dependent on input distribution
     * - Good for systems with memory constraints
     *
     * Disadvantages:
     * - Not stable
     * - Poor cache performance due to non-sequential memory access
     * - Slightly slower than Quick Sort in practice
     *
     * Time Complexity: O(n log n) in all cases
     * Space Complexity: O(1) - only uses constant extra space
     *
     * @param arr array to sort
     */
    public static void heapSort(int[] arr) {
        int n = arr.length;

        // Build max heap (rearrange array)
        // Start from last non-leaf node and heapify each node
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // Extract elements from heap one by one
        for (int i = n - 1; i > 0; i--) {
            // Move current root (maximum) to end
            swap(arr, 0, i);

            // Call heapify on the reduced heap (exclude sorted elements)
            heapify(arr, i, 0);
        }
    }

    /**
     * Maintains the max heap property for a subtree rooted at given index
     *
     * This function assumes that the binary trees rooted at left and right
     * children of i are max heaps, but arr[i] might be smaller than its children.
     * It fixes this by moving arr[i] down until heap property is satisfied.
     *
     * Max Heap Property: For every node i, arr[i] >= arr[2*i+1] and arr[i] >= arr[2*i+2]
     *
     * @param arr array representing the heap
     * @param n size of heap
     * @param i root index of subtree to heapify
     */
    private static void heapify(int[] arr, int n, int i) {
        int largest = i;          // Initialize largest as root
        int left = 2 * i + 1;     // Left child index
        int right = 2 * i + 2;    // Right child index

        // If left child exists and is greater than root
        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }

        // If right child exists and is greater than largest so far
        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }

        // If largest is not root, swap and recursively heapify affected subtree
        if (largest != i) {
            swap(arr, i, largest);
            heapify(arr, n, largest);  // Recursively heapify affected subtree
        }
    }

    /**
     * Bubble Sort - Simple comparison-based algorithm
     *
     * Bubble Sort is the simplest sorting algorithm that works by repeatedly
     * swapping adjacent elements if they are in wrong order. Named "bubble"
     * because smaller elements "bubble" to the beginning.
     *
     * How it works:
     * 1. Compare adjacent elements
     * 2. Swap if they're in wrong order
     * 3. Repeat until no swaps needed
     *
     * Advantages:
     * - Simple to understand and implement
     * - Stable sorting algorithm
     * - In-place sorting
     * - Can detect if list is already sorted
     *
     * Disadvantages:
     * - Very inefficient for large datasets
     * - Poor performance compared to other O(n²) algorithms
     * - Makes unnecessary comparisons
     *
     * Time Complexity:
     * - Best Case: O(n) - when array is already sorted
     * - Average Case: O(n²)
     * - Worst Case: O(n²) - when array is reverse sorted
     *
     * Space Complexity: O(1) - only uses constant extra space
     *
     * @param arr array to sort
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;  // Flag to optimize - detect if array is sorted

            // Last i elements are already in place after each iteration
            for (int j = 0; j < n - i - 1; j++) {
                // Swap if elements are in wrong order
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    swapped = true;
                }
            }

            // If no swapping occurred, array is sorted
            if (!swapped) break;  // Optimization: early termination
        }
    }

    /**
     * Insertion Sort - Build sorted array one element at a time
     *
     * Insertion Sort builds the final sorted array one element at a time.
     * It's similar to how you sort playing cards in your hands - take one
     * card and insert it in the right position among already sorted cards.
     *
     * How it works:
     * 1. Start with second element (first is considered sorted)
     * 2. Compare with elements in sorted portion
     * 3. Shift larger elements right and insert current element in correct position
     * 4. Repeat for all elements
     *
     * Advantages:
     * - Simple implementation
     * - Efficient for small datasets
     * - Stable and in-place
     * - Online algorithm (can sort list as it receives it)
     * - Very efficient for nearly sorted arrays
     *
     * Disadvantages:
     * - Inefficient for large datasets
     * - More writes compared to selection sort
     *
     * Time Complexity:
     * - Best Case: O(n) - when array is already sorted
     * - Average Case: O(n²)
     * - Worst Case: O(n²) - when array is reverse sorted
     *
     * Space Complexity: O(1) - only uses constant extra space
     *
     * @param arr array to sort
     */
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {           // Start from second element
            int key = arr[i];                   // Current element to be positioned
            int j = i - 1;                      // Index of last element in sorted portion

            // Move elements greater than key one position ahead
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];            // Shift element right
                j--;                            // Move to previous element
            }
            arr[j + 1] = key;                   // Insert key in correct position
        }
    }

    /**
     * Selection Sort - Select minimum element and place at beginning
     *
     * Selection Sort divides the array into sorted and unsorted regions.
     * It repeatedly finds the minimum element from unsorted region and
     * places it at the beginning of unsorted region.
     *
     * How it works:
     * 1. Find minimum element in unsorted array
     * 2. Swap it with first element of unsorted portion
     * 3. Move boundary between sorted and unsorted portions
     * 4. Repeat until entire array is sorted
     *
     * Advantages:
     * - Simple to understand and implement
     * - In-place sorting
     * - Minimum number of swaps (at most n-1)
     * - Performance doesn't depend on input (always O(n²))
     *
     * Disadvantages:
     * - Not stable (changes relative order of equal elements)
     * - Always O(n²) comparisons even if array is sorted
     * - Not efficient for large datasets
     *
     * Time Complexity: O(n²) in all cases
     * Space Complexity: O(1) - only uses constant extra space
     *
     * @param arr array to sort
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        // Move boundary of unsorted sub-array one by one
        for (int i = 0; i < n - 1; i++) {
            // Find minimum element in unsorted array
            int minIdx = i;                     // Assume first element is minimum
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;                 // Update minimum index
                }
            }
            // Swap the found minimum element with first element
            swap(arr, minIdx, i);
        }
    }

    /**
     * Utility method to swap two elements in an array
     *
     * This helper method is used by multiple sorting algorithms to exchange
     * the positions of two elements in the array.
     *
     * @param arr the array containing elements to swap
     * @param i index of first element
     * @param j index of second element
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * Demonstration of all sorting algorithms with performance comparison
     *
     * This main method shows how each sorting algorithm works on the same
     * input array and can be used to compare their performance.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        int[] originalArray = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Original array: " + Arrays.toString(originalArray));
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SORTING ALGORITHM DEMONSTRATION                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        // Quick Sort
        int[] quickSortArray = originalArray.clone();
        long startTime = System.nanoTime();
        quickSort(quickSortArray, 0, quickSortArray.length - 1);
        long quickSortTime = System.nanoTime() - startTime;
        System.out.println("Quick Sort:    " + Arrays.toString(quickSortArray) +
                          " | Time: " + quickSortTime + " ns");

        // Merge Sort
        int[] mergeSortArray = originalArray.clone();
        startTime = System.nanoTime();
        mergeSort(mergeSortArray, 0, mergeSortArray.length - 1);
        long mergeSortTime = System.nanoTime() - startTime;
        System.out.println("Merge Sort:    " + Arrays.toString(mergeSortArray) +
                          " | Time: " + mergeSortTime + " ns");

        // Heap Sort
        int[] heapSortArray = originalArray.clone();
        startTime = System.nanoTime();
        heapSort(heapSortArray);
        long heapSortTime = System.nanoTime() - startTime;
        System.out.println("Heap Sort:     " + Arrays.toString(heapSortArray) +
                          " | Time: " + heapSortTime + " ns");

        // Bubble Sort
        int[] bubbleSortArray = originalArray.clone();
        startTime = System.nanoTime();
        bubbleSort(bubbleSortArray);
        long bubbleSortTime = System.nanoTime() - startTime;
        System.out.println("Bubble Sort:   " + Arrays.toString(bubbleSortArray) +
                          " | Time: " + bubbleSortTime + " ns");

        // Insertion Sort
        int[] insertionSortArray = originalArray.clone();
        startTime = System.nanoTime();
        insertionSort(insertionSortArray);
        long insertionSortTime = System.nanoTime() - startTime;
        System.out.println("Insertion Sort:" + Arrays.toString(insertionSortArray) +
                          " | Time: " + insertionSortTime + " ns");

        // Selection Sort
        int[] selectionSortArray = originalArray.clone();
        startTime = System.nanoTime();
        selectionSort(selectionSortArray);
        long selectionSortTime = System.nanoTime() - startTime;
        System.out.println("Selection Sort:" + Arrays.toString(selectionSortArray) +
                          " | Time: " + selectionSortTime + " ns");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALGORITHM RECOMMENDATIONS:");
        System.out.println("• General purpose: Quick Sort or Merge Sort");
        System.out.println("• Guaranteed performance: Merge Sort or Heap Sort");
        System.out.println("• Memory constrained: Heap Sort or Quick Sort");
        System.out.println("• Small arrays: Insertion Sort");
        System.out.println("• Nearly sorted: Insertion Sort");
        System.out.println("• Stable sort required: Merge Sort");
        System.out.println("=".repeat(70));
    }
}