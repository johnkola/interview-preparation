package com.interview.problems;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class StringProblemsTest {

    @Test
    void testIsPalindrome() {
        assertThat(StringProblems.isPalindrome("A man, a plan, a canal: Panama")).isTrue();
        assertThat(StringProblems.isPalindrome("race a car")).isFalse();
        assertThat(StringProblems.isPalindrome("")).isTrue();
    }

    @Test
    void testIsAnagram() {
        assertThat(StringProblems.isAnagram("anagram", "nagaram")).isTrue();
        assertThat(StringProblems.isAnagram("rat", "car")).isFalse();
    }

    @Test
    void testReverseWords() {
        assertThat(StringProblems.reverseWords("the sky is blue")).isEqualTo("blue is sky the");
        assertThat(StringProblems.reverseWords("  hello world  ")).isEqualTo("world hello");
    }

    @Test
    void testLongestCommonPrefix() {
        String[] strs1 = {"flower", "flow", "flight"};
        assertThat(StringProblems.longestCommonPrefix(strs1)).isEqualTo("fl");

        String[] strs2 = {"dog", "racecar", "car"};
        assertThat(StringProblems.longestCommonPrefix(strs2)).isEqualTo("");
    }

    @Test
    void testLengthOfLongestSubstring() {
        assertThat(StringProblems.lengthOfLongestSubstring("abcabcbb")).isEqualTo(3);
        assertThat(StringProblems.lengthOfLongestSubstring("bbbbb")).isEqualTo(1);
        assertThat(StringProblems.lengthOfLongestSubstring("pwwkew")).isEqualTo(3);
    }

    @Test
    void testGroupAnagrams() {
        String[] strs = {"eat", "tea", "tan", "ate", "nat", "bat"};
        List<List<String>> result = StringProblems.groupAnagrams(strs);
        assertThat(result).hasSize(3);
    }

    @Test
    void testIsValidParentheses() {
        assertThat(StringProblems.isValidParentheses("()")).isTrue();
        assertThat(StringProblems.isValidParentheses("()[]{}")).isTrue();
        assertThat(StringProblems.isValidParentheses("(]")).isFalse();
        assertThat(StringProblems.isValidParentheses("([)]")).isFalse();
    }
}