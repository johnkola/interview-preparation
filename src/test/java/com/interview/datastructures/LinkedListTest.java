package com.interview.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class LinkedListTest {
    private LinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new LinkedList<>();
    }

    @Test
    void testAddAndGet() {
        list.add(1);
        list.add(2);
        list.add(3);

        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0)).isEqualTo(1);
        assertThat(list.get(1)).isEqualTo(2);
        assertThat(list.get(2)).isEqualTo(3);
    }

    @Test
    void testAddFirst() {
        list.add(1);
        list.add(2);
        list.addFirst(0);

        assertThat(list.get(0)).isEqualTo(0);
        assertThat(list.get(1)).isEqualTo(1);
    }

    @Test
    void testRemove() {
        list.add(1);
        list.add(2);
        list.add(3);

        Integer removed = list.remove(1);
        assertThat(removed).isEqualTo(2);
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(1)).isEqualTo(3);
    }

    @Test
    void testReverse() {
        list.add(1);
        list.add(2);
        list.add(3);

        list.reverse();

        assertThat(list.get(0)).isEqualTo(3);
        assertThat(list.get(1)).isEqualTo(2);
        assertThat(list.get(2)).isEqualTo(1);
    }

    @Test
    void testIsEmpty() {
        assertThat(list.isEmpty()).isTrue();
        list.add(1);
        assertThat(list.isEmpty()).isFalse();
    }
}