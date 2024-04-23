/*
 * MIT License
 *
 * Copyright (c) 2024 Roman Khlebnov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.suppierk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class HashUnionFindSetTest {
  @Test
  @SuppressWarnings("squid:S5778")
  void constructor() {
    assertThrows(
        IllegalArgumentException.class, () -> new HashUnionFindSet<Integer, Integer>(null));
    assertDoesNotThrow(() -> new HashUnionFindSet<Integer, Integer>(Function.identity()));

    assertThrows(
        IllegalArgumentException.class, () -> new HashUnionFindSet<Integer, Integer>(null, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HashUnionFindSet<Integer, Integer>(null, Function.identity()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HashUnionFindSet<Integer, Integer>(Set.of(1), null));
    assertDoesNotThrow(() -> new HashUnionFindSet<>(Set.of(1), Function.identity()));

    assertThrows(
        IllegalArgumentException.class, () -> new HashUnionFindSet<Integer, Integer>(1, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HashUnionFindSet<Integer, Integer>(-1, Function.identity()));
    assertDoesNotThrow(() -> new HashUnionFindSet<>(1, Function.identity()));

    assertThrows(
        IllegalArgumentException.class, () -> new HashUnionFindSet<Integer, Integer>(1, 1f, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HashUnionFindSet<Integer, Integer>(-1, -1f, Function.identity()));
    assertDoesNotThrow(() -> new HashUnionFindSet<>(1, 1f, Function.identity()));
  }

  @Test
  void iterator() {
    Set<Integer> expectedValues = new HashSet<>();
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    for (int i = 0; i < 10_000; i++) {
      expectedValues.add(i);
      hufs.add(i);
    }

    Iterator<Integer> i = hufs.iterator();
    assertNotNull(i, "Iterator must not be null");

    while (i.hasNext()) {
      int element = i.next();
      expectedValues.remove(element);
    }

    assertTrue(expectedValues.isEmpty(), "After test we should have seen all values");
  }

  @Test
  void iterator_concurrentModificationException() {
    HashUnionFindSet<Integer, Integer> hufs1 = new HashUnionFindSet<>(Function.identity());
    hufs1.add(1);
    hufs1.add(2);
    hufs1.add(3);

    try {
      for (Integer i : hufs1) {
        hufs1.remove(i);
      }

      fail("Should throw ConcurrentModificationException");
    } catch (ConcurrentModificationException cme) {
      // Excepted
    }

    HashUnionFindSet<Integer, Integer> hufs2 = new HashUnionFindSet<>(Function.identity());
    hufs2.add(1);
    hufs2.add(2);
    hufs2.add(3);

    try {
      for (Integer i : hufs2) {
        hufs2.union(1, i);
      }

      fail("Should throw ConcurrentModificationException");
    } catch (ConcurrentModificationException cme) {
      // Excepted
    }

    HashUnionFindSet<Integer, Integer> hufs3 = new HashUnionFindSet<>(Function.identity());
    hufs3.add(1);
    hufs3.add(2);
    hufs3.add(3);

    try {
      hufs3.forEach(hufs3::remove);

      fail("Should throw ConcurrentModificationException");
    } catch (ConcurrentModificationException cme) {
      // Excepted
    }
  }

  @Test
  void size() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    assertEquals(0, hufs.size(), "Size must be zero by default");

    hufs.add(1);
    assertEquals(1, hufs.size(), "Size must increase appropriately");

    hufs.add(2);
    assertEquals(2, hufs.size(), "Size must increase appropriately");

    hufs.add(3);
    assertEquals(3, hufs.size(), "Size must increase appropriately");
  }

  @Test
  void isEmpty() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    assertTrue(hufs.isEmpty(), "isEmpty must be true by default");

    hufs.add(1);
    assertFalse(hufs.isEmpty(), "isEmpty must return false after an element was added");

    hufs.remove(1);
    assertTrue(hufs.isEmpty(), "isEmpty must return true after an element was removed");
  }

  @Test
  void contains() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    assertFalse(hufs.contains(1), "contains must be false by default");

    hufs.add(1);
    assertTrue(hufs.contains(1), "contains must return true after an element was added");

    hufs.remove(1);
    assertFalse(hufs.contains(1), "contains must return false after an element was removed");
  }

  @Test
  void add() {
    HashUnionFindSet<Integer, Integer> hufsNullRepresentativeRetriever =
        new HashUnionFindSet<>(integer -> null);
    assertThrows(
        IllegalArgumentException.class,
        () -> hufsNullRepresentativeRetriever.add(1),
        "Representative value retriever returning nulls must result in a failure");

    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    assertEquals(0, hufs.representatives.size(), "Empty set must not have any representatives");
    assertThrows(
        IllegalArgumentException.class,
        () -> hufs.add(null),
        "Adding null value must result in null");

    assertTrue(
        assertDoesNotThrow(() -> hufs.add(1), "Adding normal value should be OK"),
        "Adding new value must return true");
    assertEquals(1, hufs.representatives.size(), "Amount of representatives must increase by one");

    assertTrue(
        assertDoesNotThrow(() -> hufs.add(2), "Adding normal value should be OK"),
        "Adding new value must return true");
    assertEquals(2, hufs.representatives.size(), "Amount of representatives must increase by one");

    assertTrue(
        assertDoesNotThrow(() -> hufs.add(3), "Adding normal value should be OK"),
        "Adding new value must return true");
    assertEquals(3, hufs.representatives.size(), "Amount of representatives must increase by one");

    assertFalse(
        assertDoesNotThrow(() -> hufs.add(1), "Adding normal value should be OK"),
        "Adding existing value must return false");
    assertEquals(3, hufs.representatives.size(), "Amount of representatives must increase by one");
  }

  @Test
  void remove_standaloneSetElement() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(
        3,
        hufs.representatives.size(),
        "After adding three values there must be three disjoint sets");
    assertFalse(
        assertDoesNotThrow(
            () -> hufs.remove(Integer.MAX_VALUE),
            "Removal of non-existing value must not throw an exception"),
        "Removal of non-existing value must return false");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.remove(1), "Removal of existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertEquals(2, hufs.representatives.size(), "Amount of representatives must reduce by one");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.remove(2), "Removal of existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertEquals(1, hufs.representatives.size(), "Amount of representatives must reduce by one");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.remove(3), "Removal of existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertEquals(0, hufs.representatives.size(), "Amount of representatives must reduce by one");

    assertTrue(hufs.isEmpty(), "At the end there must be no elements in the set");
    assertTrue(
        hufs.representatives.isEmpty(), "At the end there must be no representatives in the set");
  }

  @Test
  void remove_rootRepresentativeWithChildren() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    // Model a case, where we have a representative linear tree as [1 -> 2 -> 3]
    HashUnionFindSet.Representative<Integer, Integer> firstRep =
        new HashUnionFindSet.Representative<>(1);
    HashUnionFindSet.Representative<Integer, Integer> secondRep =
        new HashUnionFindSet.Representative<>(2);
    HashUnionFindSet.Representative<Integer, Integer> thirdRep =
        new HashUnionFindSet.Representative<>(3);

    firstRep.disjointSetValues.add(1);
    firstRep.disjointSetParent = null;
    firstRep.disjointSetChildren.put(secondRep.value, secondRep);

    secondRep.disjointSetValues.add(2);
    secondRep.disjointSetParent = firstRep;
    secondRep.disjointSetChildren.put(thirdRep.value, thirdRep);

    thirdRep.disjointSetValues.add(3);
    thirdRep.disjointSetParent = secondRep;

    // Add values to the set
    hufs.representatives.put(1, firstRep);

    hufs.map.put(1, firstRep);
    hufs.map.put(2, secondRep);
    hufs.map.put(3, thirdRep);

    // Remove root
    assertTrue(
        assertDoesNotThrow(
            () -> hufs.remove(1), "Removal of existing root must not throw an exception"),
        "Removal of existing root must return true");

    // Ensure that the representative tree hierarchy shifted
    assertFalse(
        hufs.representatives.containsKey(1),
        "Root representative must be removed from representatives map");
    assertTrue(
        hufs.representatives.containsKey(2),
        "Middle representative must be added to representatives map");
    assertFalse(
        hufs.representatives.containsKey(3),
        "Leaf representative must not be added to representatives map");

    assertTrue(
        firstRep.disjointSetValues.isEmpty(), "Root representative must not refer to any values");
    assertNull(
        firstRep.disjointSetParent,
        "Root representative must not refer to any parent representative");
    assertTrue(
        firstRep.disjointSetChildren.isEmpty(),
        "Root representative must not refer to any child representatives");

    assertEquals(
        Set.of(2),
        secondRep.disjointSetValues,
        "Middle representative must keep reference to its value");
    assertNull(
        secondRep.disjointSetParent,
        "Middle representative must not refer to any parent representative");
    assertTrue(
        secondRep.disjointSetChildren.containsKey(thirdRep.value),
        "Middle representative must keep reference to its children");

    assertEquals(
        Set.of(3),
        thirdRep.disjointSetValues,
        "Leaf representative must keep reference to its value");
    assertEquals(
        secondRep,
        thirdRep.disjointSetParent,
        "Leaf representative must keep reference to its parent representative");
    assertTrue(
        thirdRep.disjointSetChildren.isEmpty(),
        "Leaf representative must not refer to any child representatives");
  }

  @Test
  void remove_middleRepresentative() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    // Model a case, where we have a representative linear tree as [1 -> 2 -> 3]
    HashUnionFindSet.Representative<Integer, Integer> firstRep =
        new HashUnionFindSet.Representative<>(1);
    HashUnionFindSet.Representative<Integer, Integer> secondRep =
        new HashUnionFindSet.Representative<>(2);
    HashUnionFindSet.Representative<Integer, Integer> thirdRep =
        new HashUnionFindSet.Representative<>(3);

    firstRep.disjointSetValues.add(1);
    firstRep.disjointSetParent = null;
    firstRep.disjointSetChildren.put(secondRep.value, secondRep);

    secondRep.disjointSetValues.add(2);
    secondRep.disjointSetParent = firstRep;
    secondRep.disjointSetChildren.put(thirdRep.value, thirdRep);

    thirdRep.disjointSetValues.add(3);
    thirdRep.disjointSetParent = secondRep;

    // Add values to the set
    hufs.representatives.put(1, firstRep);

    hufs.map.put(1, firstRep);
    hufs.map.put(2, secondRep);
    hufs.map.put(3, thirdRep);

    // Remove root
    assertTrue(assertDoesNotThrow(() -> hufs.remove(2)));

    // Ensure that the representative tree hierarchy shifted
    assertTrue(
        hufs.representatives.containsKey(1),
        "Root representative must stay in representatives map");
    assertFalse(
        hufs.representatives.containsKey(2),
        "Middle representative must not be added to representatives map");
    assertFalse(
        hufs.representatives.containsKey(3),
        "Leaf representative must not be added to representatives map");

    assertEquals(
        Set.of(1),
        firstRep.disjointSetValues,
        "Root representative must keep reference to its value");
    assertNull(
        firstRep.disjointSetParent,
        "Root representative must not refer to any parent representative");
    assertTrue(
        firstRep.disjointSetChildren.containsKey(thirdRep.value),
        "Root representative must refer to leaf representative instead of middle");

    assertTrue(
        secondRep.disjointSetValues.isEmpty(), "Middle representative must not refer to any value");
    assertNull(
        secondRep.disjointSetParent,
        "Middle representative must not refer to any parent representative");
    assertTrue(
        secondRep.disjointSetChildren.isEmpty(),
        "Middle representative must not refer to any child representatives");

    assertEquals(
        Set.of(3),
        thirdRep.disjointSetValues,
        "Leaf representative must keep reference to its value");
    assertEquals(
        firstRep,
        thirdRep.disjointSetParent,
        "Leaf representative must shift its parent reference to root representative instead of middle");
    assertTrue(
        thirdRep.disjointSetChildren.isEmpty(),
        "Leaf representative must not refer to any child representatives");
  }

  @Test
  void remove_leafRepresentative() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    // Model a case, where we have a representative linear tree as [1 -> 2 -> 3]
    HashUnionFindSet.Representative<Integer, Integer> firstRep =
        new HashUnionFindSet.Representative<>(1);
    HashUnionFindSet.Representative<Integer, Integer> secondRep =
        new HashUnionFindSet.Representative<>(2);
    HashUnionFindSet.Representative<Integer, Integer> thirdRep =
        new HashUnionFindSet.Representative<>(3);

    firstRep.disjointSetValues.add(1);
    firstRep.disjointSetParent = null;
    firstRep.disjointSetChildren.put(secondRep.value, secondRep);

    secondRep.disjointSetValues.add(2);
    secondRep.disjointSetParent = firstRep;
    secondRep.disjointSetChildren.put(thirdRep.value, thirdRep);

    thirdRep.disjointSetValues.add(3);
    thirdRep.disjointSetParent = secondRep;

    // Add values to the set
    hufs.representatives.put(1, firstRep);

    hufs.map.put(1, firstRep);
    hufs.map.put(2, secondRep);
    hufs.map.put(3, thirdRep);

    // Remove root
    assertTrue(assertDoesNotThrow(() -> hufs.remove(3)));

    // Ensure that the representative tree hierarchy shifted
    assertTrue(
        hufs.representatives.containsKey(1),
        "Root representative must stay in representatives map");
    assertFalse(
        hufs.representatives.containsKey(2),
        "Middle representative must not be added to representatives map");
    assertFalse(
        hufs.representatives.containsKey(3),
        "Leaf representative must not be added to representatives map");

    assertEquals(
        Set.of(1),
        firstRep.disjointSetValues,
        "Root representative must keep reference to its value");
    assertNull(
        firstRep.disjointSetParent,
        "Root representative must not refer to any parent representative");
    assertTrue(
        firstRep.disjointSetChildren.containsKey(secondRep.value),
        "Root representative must keep reference to middle representative");

    assertEquals(
        Set.of(2),
        secondRep.disjointSetValues,
        "Middle representative must keep reference to its value");
    assertEquals(
        firstRep,
        secondRep.disjointSetParent,
        "Middle representative must keep reference to the root representative");
    assertTrue(
        secondRep.disjointSetChildren.isEmpty(),
        "Middle representative must not refer to any child representatives");

    assertTrue(
        thirdRep.disjointSetValues.isEmpty(), "Leaf representative must not refer to any value");
    assertNull(
        thirdRep.disjointSetParent,
        "Leaf representative must not refer to any parent representative");
    assertTrue(
        thirdRep.disjointSetChildren.isEmpty(),
        "Leaf representative must not refer to any child representatives");
  }

  @Test
  void removeAll() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(
        3,
        hufs.representatives.size(),
        "After adding three values there must be three disjoint sets");
    assertThrows(
        NullPointerException.class,
        () -> hufs.removeAll(null),
        "Null argument must result in an exception");
    assertFalse(
        assertDoesNotThrow(
            () -> hufs.removeAll(Set.of(Integer.MAX_VALUE)),
            "Removal of non-existing value must not throw an exception"),
        "Removal of non-existing value must return false");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.removeAll(Set.of(1, 2)),
            "Removal of existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertEquals(1, hufs.representatives.size(), "Amount of representatives must reduce by two");
    assertTrue(hufs.map.containsKey(3), "There must be only one element");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.removeAll(Set.of(3, 4)),
            "Removal of existing and non-existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertTrue(hufs.representatives.isEmpty(), "All representatives must be removed");

    assertFalse(
        assertDoesNotThrow(
            () -> hufs.removeAll(Set.of(1, 2)),
            "Removal of non-existing values must not throw an exception"),
        "Removal of existing value must return false");

    assertTrue(hufs.isEmpty(), "At the end there must be no elements in the set");
    assertTrue(
        hufs.representatives.isEmpty(), "At the end there must be no representatives in the set");
  }

  @Test
  void retainAll() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(
        3,
        hufs.representatives.size(),
        "After adding three values there must be three disjoint sets");
    assertThrows(
        NullPointerException.class,
        () -> hufs.retainAll(null),
        "Null argument must result in an exception");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.retainAll(Set.of(1, 2)),
            "Removal of existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertEquals(2, hufs.representatives.size(), "Amount of representatives must reduce by one");
    assertTrue(hufs.map.containsKey(1), "There must element 1");
    assertTrue(hufs.map.containsKey(2), "There must element 2");
    assertFalse(hufs.map.containsKey(3), "There must be no element 3");

    assertTrue(
        assertDoesNotThrow(
            () -> hufs.retainAll(Set.of(3, 4)),
            "Removal of existing and non-existing value must not throw an exception"),
        "Removal of existing value must return true");
    assertTrue(hufs.representatives.isEmpty(), "All representatives must be removed");

    assertFalse(
        assertDoesNotThrow(
            () -> hufs.retainAll(Set.of(1, 2)),
            "Removal of non-existing values must not throw an exception"),
        "Removal of existing value must return false");

    assertTrue(hufs.isEmpty(), "At the end there must be no elements in the set");
    assertTrue(
        hufs.representatives.isEmpty(), "At the end there must be no representatives in the set");
  }

  @Test
  void clear() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(3, hufs.map.size(), "Initial map must contain 3 elements");
    assertEquals(
        3, hufs.representatives.size(), "Initial representatives map must contain 3 elements");

    assertDoesNotThrow(hufs::clear, "Clear must not throw an exception");

    assertEquals(0, hufs.map.size(), "After clear map must contain 0 elements");
    assertEquals(
        0, hufs.representatives.size(), "After clear representatives map must contain 0 elements");
  }

  @Test
  @SuppressWarnings("unchecked")
  void clone_() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    HashUnionFindSet<Integer, Integer> clonedHufs =
        (HashUnionFindSet<Integer, Integer>)
            assertDoesNotThrow(hufs::clone, "Clear must not throw an exception");

    assertEquals(
        hufs.representativeRetriever,
        clonedHufs.representativeRetriever,
        "After clone function must remain the same");
    assertEquals(hufs.map, clonedHufs.map, "After clone maps must be equal");
    assertEquals(
        hufs.representatives,
        clonedHufs.representatives,
        "After clone representative maps must be equal");
  }

  @Test
  void spliterator() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    int expectedSum = 0;
    for (int i = 0; i < 10_000; i++) {
      expectedSum += i;
      hufs.add(i);
    }

    assertNotNull(hufs.spliterator(), "Spliterator must not be null");

    int actualSum = hufs.stream().mapToInt(value -> value).sum();
    int actualParallelSum = hufs.parallelStream().mapToInt(value -> value).sum();

    assertEquals(expectedSum, actualSum, "Sums are not equal");
    assertEquals(expectedSum, actualParallelSum, "Sums are not equal");
  }

  @Test
  void toArray() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertArrayEquals(new Object[] {1, 2, 3}, hufs.toArray(), "toArray should work as expected");
  }

  @Test
  void toArray_typed() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertArrayEquals(
        new Integer[] {1, 2, 3},
        hufs.toArray(new Integer[0]),
        "Typed toArray should work as expected");
  }

  @Test
  void union_standaloneElement() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(3, hufs.representatives.size(), "There must be 3 distinct disjoint sets");

    assertThrows(
        NoSuchElementException.class,
        () -> hufs.union(Integer.MAX_VALUE, Integer.MAX_VALUE),
        "Union must throw NoSuchElementException for non-existing elements");

    assertDoesNotThrow(
        () -> hufs.union(1, 2), "Union between existing elements must not throw an exception");
    assertEquals(
        2, hufs.representatives.size(), "There must be 2 distinct disjoint sets after union");
    assertEquals(
        hufs.find(1), hufs.find(2), "Find operation for both arguments must return same value");

    assertDoesNotThrow(
        () -> hufs.union(2, 1),
        "Union between already joined elements in reverse order must not throw an exception");
    assertEquals(
        2, hufs.representatives.size(), "There must be 2 distinct disjoint sets after union");
    assertEquals(
        hufs.find(1),
        hufs.find(2),
        "Find operation for both arguments must return same value again");

    Integer representative = hufs.find(1);
    assertDoesNotThrow(
        () -> hufs.union(1, 3), "Union between existing elements must not throw an exception");
    assertEquals(
        1, hufs.representatives.size(), "There must be 1 distinct disjoint sets after union");
    assertEquals(
        representative,
        hufs.find(3),
        "Smaller disjoint set must have been joined to a larger disjoint set");
  }

  @Test
  void union_standaloneElement_evenDisjointSetSizes() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);
    hufs.add(4);

    assertEquals(4, hufs.representatives.size(), "There must be 4 distinct disjoint sets");

    assertThrows(
        NoSuchElementException.class,
        () -> hufs.union(Integer.MAX_VALUE, Integer.MAX_VALUE),
        "Union must throw NoSuchElementException for non-existing elements");

    assertDoesNotThrow(
        () -> hufs.union(1, 2), "Union between existing elements must not throw an exception");
    assertEquals(
        3, hufs.representatives.size(), "There must be 3 distinct disjoint sets after union");
    assertEquals(
        hufs.find(1), hufs.find(2), "Find operation for both arguments must return same value");

    assertDoesNotThrow(
        () -> hufs.union(3, 4), "Union between existing elements must not throw an exception");
    assertEquals(
        2, hufs.representatives.size(), "There must be 2 distinct disjoint sets after union");
    assertEquals(
        hufs.find(3),
        hufs.find(4),
        "Find operation for both arguments must return same value again");

    Integer representative1 = hufs.find(1);
    assertDoesNotThrow(
        () -> hufs.union(1, 3), "Union between existing elements must not throw an exception");
    assertEquals(
        1, hufs.representatives.size(), "There must be 1 distinct disjoint sets after union");
    assertEquals(
        representative1, hufs.find(3), "When sets have equal sizes, first root typically wins");
  }

  @Test
  void find_standaloneElement() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertThrows(
        NoSuchElementException.class,
        () -> hufs.find(Integer.MAX_VALUE),
        "Find must throw NoSuchElementException for non-existing elements");
    assertEquals(1, hufs.find(1), "Find must return respective disjoint set representative");
    assertEquals(2, hufs.find(2), "Find must return respective disjoint set representative");
    assertEquals(3, hufs.find(3), "Find must return respective disjoint set representative");
  }

  @Test
  void find_rootRepresentativeWithChildren_compactionSkipped() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    // Model a case, where we have a representative linear tree as [1 -> 2 -> 3]
    HashUnionFindSet.Representative<Integer, Integer> firstRep =
        new HashUnionFindSet.Representative<>(1);
    HashUnionFindSet.Representative<Integer, Integer> secondRep =
        new HashUnionFindSet.Representative<>(2);
    HashUnionFindSet.Representative<Integer, Integer> thirdRep =
        new HashUnionFindSet.Representative<>(3);

    firstRep.disjointSetValues.add(1);
    firstRep.disjointSetParent = null;
    firstRep.disjointSetChildren.put(secondRep.value, secondRep);

    secondRep.disjointSetValues.add(2);
    secondRep.disjointSetParent = firstRep;
    secondRep.disjointSetChildren.put(thirdRep.value, thirdRep);

    thirdRep.disjointSetValues.add(3);
    thirdRep.disjointSetParent = secondRep;

    // Add values to the set
    hufs.representatives.put(1, firstRep);

    hufs.map.put(1, firstRep);
    hufs.map.put(2, secondRep);
    hufs.map.put(3, thirdRep);

    // Remove root
    assertEquals(
        1,
        assertDoesNotThrow(
            () -> hufs.find(1), "Find must not throw exception for existing element"),
        "Find for root element must return appropriate value");

    // Ensure that the representative tree hierarchy shifted
    assertTrue(
        hufs.representatives.containsKey(1),
        "Root representative must stay in representatives map");
    assertFalse(
        hufs.representatives.containsKey(2),
        "Middle representative must not be added to representatives map");
    assertFalse(
        hufs.representatives.containsKey(3),
        "Leaf representative must not be added to representatives map");

    assertEquals(
        Set.of(1),
        firstRep.disjointSetValues,
        "Root representative must keep reference to its value");
    assertNull(
        firstRep.disjointSetParent,
        "Root representative must not refer to any parent representative");
    assertTrue(
        firstRep.disjointSetChildren.containsKey(secondRep.value),
        "Root representative must keep reference to middle representative");

    assertEquals(
        Set.of(2),
        secondRep.disjointSetValues,
        "Middle representative must keep reference to its value");
    assertEquals(
        firstRep,
        secondRep.disjointSetParent,
        "Middle representative must keep reference to root representative");
    assertTrue(
        secondRep.disjointSetChildren.containsKey(thirdRep.value),
        "Middle representative must keep reference to leaf representative");

    assertEquals(
        Set.of(3),
        thirdRep.disjointSetValues,
        "Leaf representative must keep reference to its value");
    assertEquals(
        secondRep,
        thirdRep.disjointSetParent,
        "Leaf representative must not refer to any parent representative");
    assertTrue(
        thirdRep.disjointSetChildren.isEmpty(),
        "Leaf representative must not refer to any child representatives");
  }

  @Test
  void find_middleRepresentative_compaction() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    // Model a case, where we have a representative linear tree as [1 -> 2 -> 3]
    HashUnionFindSet.Representative<Integer, Integer> firstRep =
        new HashUnionFindSet.Representative<>(1);
    HashUnionFindSet.Representative<Integer, Integer> secondRep =
        new HashUnionFindSet.Representative<>(2);
    HashUnionFindSet.Representative<Integer, Integer> thirdRep =
        new HashUnionFindSet.Representative<>(3);

    firstRep.disjointSetValues.add(1);
    firstRep.disjointSetParent = null;
    firstRep.disjointSetChildren.put(secondRep.value, secondRep);

    secondRep.disjointSetValues.add(2);
    secondRep.disjointSetParent = firstRep;
    secondRep.disjointSetChildren.put(thirdRep.value, thirdRep);

    thirdRep.disjointSetValues.add(3);
    thirdRep.disjointSetParent = secondRep;

    // Add values to the set
    hufs.representatives.put(1, firstRep);

    hufs.map.put(1, firstRep);
    hufs.map.put(2, secondRep);
    hufs.map.put(3, thirdRep);

    // Remove root
    assertEquals(
        1,
        assertDoesNotThrow(
            () -> hufs.find(2), "Find must not throw exception for existing element"),
        "Find for middle element must return root value");

    // Ensure that the representative tree hierarchy shifted
    assertTrue(
        hufs.representatives.containsKey(1),
        "Root representative must stay in representatives map");
    assertFalse(
        hufs.representatives.containsKey(2),
        "Middle representative must not be added to representatives map");
    assertFalse(
        hufs.representatives.containsKey(3),
        "Leaf representative must not be added to representatives map");

    assertEquals(
        Set.of(1, 2),
        firstRep.disjointSetValues,
        "Root representative must keep reference to its value + middle value");
    assertNull(
        firstRep.disjointSetParent,
        "Root representative must not refer to any parent representative");
    assertTrue(
        firstRep.disjointSetChildren.containsKey(secondRep.value),
        "Root representative must keep reference to middle representative");

    assertTrue(
        secondRep.disjointSetValues.isEmpty(),
        "Middle representative must not refer to its value after compaction");
    assertNull(
        secondRep.disjointSetParent, "Middle representative must not refer to root representative");
    assertTrue(
        secondRep.disjointSetChildren.isEmpty(),
        "Middle representative must not refer to leaf representative");

    assertEquals(
        Set.of(3),
        thirdRep.disjointSetValues,
        "Leaf representative must keep reference to its value");
    assertEquals(
        firstRep,
        thirdRep.disjointSetParent,
        "Leaf representative must refer to root representative, bypassing middle reference");
    assertTrue(
        thirdRep.disjointSetChildren.isEmpty(),
        "Leaf representative must not refer to any child representatives");
  }

  @Test
  void find_leafRepresentative_compaction() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());

    // Model a case, where we have a representative linear tree as [1 -> 2 -> 3]
    HashUnionFindSet.Representative<Integer, Integer> firstRep =
        new HashUnionFindSet.Representative<>(1);
    HashUnionFindSet.Representative<Integer, Integer> secondRep =
        new HashUnionFindSet.Representative<>(2);
    HashUnionFindSet.Representative<Integer, Integer> thirdRep =
        new HashUnionFindSet.Representative<>(3);

    firstRep.disjointSetValues.add(1);
    firstRep.disjointSetParent = null;
    firstRep.disjointSetChildren.put(secondRep.value, secondRep);

    secondRep.disjointSetValues.add(2);
    secondRep.disjointSetParent = firstRep;
    secondRep.disjointSetChildren.put(thirdRep.value, thirdRep);

    thirdRep.disjointSetValues.add(3);
    thirdRep.disjointSetParent = secondRep;

    // Add values to the set
    hufs.representatives.put(1, firstRep);

    hufs.map.put(1, firstRep);
    hufs.map.put(2, secondRep);
    hufs.map.put(3, thirdRep);

    // Remove root
    assertEquals(
        1,
        assertDoesNotThrow(
            () -> hufs.find(3), "Find must not throw exception for existing element"),
        "Find for leaf element must return root value");

    // Ensure that the representative tree hierarchy shifted
    assertTrue(
        hufs.representatives.containsKey(1),
        "Root representative must stay in representatives map");
    assertFalse(
        hufs.representatives.containsKey(2),
        "Middle representative must not be added to representatives map");
    assertFalse(
        hufs.representatives.containsKey(3),
        "Leaf representative must not be added to representatives map");

    assertEquals(
        Set.of(1, 3),
        firstRep.disjointSetValues,
        "Root representative must keep reference to its value + leaf value");
    assertNull(
        firstRep.disjointSetParent,
        "Root representative must not refer to any parent representative");
    assertTrue(
        firstRep.disjointSetChildren.containsKey(secondRep.value),
        "Root representative must keep reference to middle representative");

    assertEquals(
        Set.of(2),
        secondRep.disjointSetValues,
        "Middle representative must keep reference to its value");
    assertEquals(
        firstRep,
        secondRep.disjointSetParent,
        "Middle representative must keep reference to root representative");
    assertTrue(
        secondRep.disjointSetChildren.isEmpty(),
        "Middle representative must not refer to leaf representative");

    assertTrue(
        thirdRep.disjointSetValues.isEmpty(), "Leaf representative must not refer to its value");
    assertNull(
        thirdRep.disjointSetParent,
        "Leaf representative must not refer to any parent representative");
    assertTrue(
        thirdRep.disjointSetChildren.isEmpty(),
        "Leaf representative must not refer to any child representatives");
  }

  @Test
  void numberOfSets() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(3, hufs.numberOfSets(), "Without union there must be 3 disjoint sets");

    hufs.union(1, 2);
    assertEquals(2, hufs.numberOfSets(), "After single union there must be 2 disjoint sets");

    hufs.union(1, 3);
    assertEquals(
        1, hufs.numberOfSets(), "After all elements were joined there must be 1 disjoint set");
  }

  @Test
  void representativeSetSize() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    assertThrows(
        NoSuchElementException.class,
        () -> hufs.representativeSetSize(Integer.MAX_VALUE),
        "For absent elements, representativeSetSize must throws NoSuchElementException");
  }

  @Test
  void representativeSet() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    assertThrows(
        NoSuchElementException.class,
        () -> hufs.representativeSet(Integer.MAX_VALUE),
        "For absent elements, representativeSet must throws NoSuchElementException");
  }

  @Test
  void elementSetSize() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(
        1, hufs.elementSetSize(1), "Without union there must be 3 disjoint sets of size 1");
    assertEquals(
        1, hufs.elementSetSize(2), "Without union there must be 3 disjoint sets of size 1");
    assertEquals(
        1, hufs.elementSetSize(3), "Without union there must be 3 disjoint sets of size 1");

    hufs.union(1, 2);
    assertEquals(
        2,
        hufs.elementSetSize(1),
        "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertEquals(
        2,
        hufs.elementSetSize(2),
        "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertEquals(
        1,
        hufs.elementSetSize(3),
        "After single union there must be 2 disjoint sets of sizes 1 and 2");

    hufs.union(1, 3);
    assertEquals(
        3,
        hufs.elementSetSize(1),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertEquals(
        3,
        hufs.elementSetSize(2),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertEquals(
        3,
        hufs.elementSetSize(3),
        "After all elements were joined there must be 1 disjoint set of size 3");
  }

  @Test
  void elementSet() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertEquals(
        Set.of(1), hufs.elementSet(1), "Without union there must be 3 disjoint sets of size 1");
    assertEquals(
        Set.of(2), hufs.elementSet(2), "Without union there must be 3 disjoint sets of size 1");
    assertEquals(
        Set.of(3), hufs.elementSet(3), "Without union there must be 3 disjoint sets of size 1");

    hufs.union(1, 2);
    assertEquals(
        Set.of(1, 2),
        hufs.elementSet(1),
        "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertEquals(
        Set.of(1, 2),
        hufs.elementSet(2),
        "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertEquals(
        Set.of(3),
        hufs.elementSet(3),
        "After single union there must be 2 disjoint sets of sizes 1 and 2");

    hufs.union(1, 3);
    assertEquals(
        Set.of(1, 2, 3),
        hufs.elementSet(1),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertEquals(
        Set.of(1, 2, 3),
        hufs.elementSet(2),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertEquals(
        Set.of(1, 2, 3),
        hufs.elementSet(3),
        "After all elements were joined there must be 1 disjoint set of size 3");
  }

  @Test
  @SuppressWarnings("squid:S5961")
  void connected() {
    HashUnionFindSet<Integer, Integer> hufs = new HashUnionFindSet<>(Function.identity());
    hufs.add(1);
    hufs.add(2);
    hufs.add(3);

    assertTrue(hufs.connected(1, 1), "Without union there must be 3 disjoint sets of size 1");
    assertFalse(hufs.connected(1, 2), "Without union there must be 3 disjoint sets of size 1");
    assertFalse(hufs.connected(1, 3), "Without union there must be 3 disjoint sets of size 1");
    assertFalse(hufs.connected(2, 1), "Without union there must be 3 disjoint sets of size 1");
    assertTrue(hufs.connected(2, 2), "Without union there must be 3 disjoint sets of size 1");
    assertFalse(hufs.connected(2, 3), "Without union there must be 3 disjoint sets of size 1");
    assertFalse(hufs.connected(3, 1), "Without union there must be 3 disjoint sets of size 1");
    assertFalse(hufs.connected(3, 2), "Without union there must be 3 disjoint sets of size 1");
    assertTrue(hufs.connected(3, 3), "Without union there must be 3 disjoint sets of size 1");

    hufs.union(1, 2);
    assertTrue(
        hufs.connected(1, 1), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertTrue(
        hufs.connected(1, 2), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertFalse(
        hufs.connected(1, 3), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertTrue(
        hufs.connected(2, 1), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertTrue(
        hufs.connected(2, 2), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertFalse(
        hufs.connected(2, 3), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertFalse(
        hufs.connected(3, 1), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertFalse(
        hufs.connected(3, 2), "After single union there must be 2 disjoint sets of sizes 1 and 2");
    assertTrue(
        hufs.connected(3, 3), "After single union there must be 2 disjoint sets of sizes 1 and 2");

    hufs.union(1, 3);
    assertTrue(
        hufs.connected(1, 1),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(1, 2),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(1, 3),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(2, 1),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(2, 2),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(2, 3),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(3, 1),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(3, 2),
        "After all elements were joined there must be 1 disjoint set of size 3");
    assertTrue(
        hufs.connected(3, 3),
        "After all elements were joined there must be 1 disjoint set of size 3");
  }
}
