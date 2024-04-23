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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * HashUnionFindSet is an implementation of the UnionFind interface that uses a hash map as the
 * underlying data structure.
 *
 * <p>This class provides efficient operations for creating and manipulating disjoint sets using a
 * hash map to store the elements. It supports operations such as union, find, and determining the
 * size of a set.
 *
 * <p>The hash map is used to store representatives for each element in the set. Each element is
 * associated with a representative, which is a value of type R. The representatives form a
 * tree-like structure, where each node represents a set and points to its parent node. The root
 * node of each tree represents the representative of the set.
 *
 * <p>The class has multiple constructors to create HashUnionFindSet objects with different initial
 * capacities and load factors for the hash map. It also supports initializing the set with elements
 * from a collection.
 *
 * @param <E> the type of the elements stored in the set
 * @param <R> the type of the representative values
 * @see UnionFind
 * @see Representative
 */
@SuppressWarnings("squid:S2160")
public class HashUnionFindSet<R, E> extends AbstractSet<E>
    implements UnionFind<R, E>, Set<E>, Cloneable {
  Function<E, R> representativeRetriever;
  HashMap<R, Representative<R, E>> representatives;
  HashMap<E, Representative<R, E>> map;

  /**
   * The number of times this HashUnionFindSet has been structurally modified Structural
   * modifications are those that change the number of mappings in the HashMap or otherwise modify
   * its internal structure (e.g., rehash). This field is used to make iterators on Collection-views
   * of the HashUnionFindSet fail-fast. (See ConcurrentModificationException).
   */
  int modCount;

  /**
   * Constructs a new, empty set; the backing {@code HashMap} instance has default initial capacity
   * (16) and load factor (0.75).
   */
  public HashUnionFindSet(Function<E, R> representativeRetriever) {
    this(16, representativeRetriever);
  }

  /**
   * Constructs a new set containing the elements in the specified collection. The {@code HashMap}
   * is created with a default load factor (0.75) and an initial capacity sufficient to contain the
   * elements in the specified collection.
   *
   * @param c the collection whose elements are to be placed into this set
   * @throws NullPointerException if the specified collection is null
   */
  public HashUnionFindSet(Collection<? extends E> c, Function<E, R> representativeRetriever) {
    this(
        Math.max((int) (requireNotNull(c, "Collection cannot be null").size() / .75f) + 1, 16),
        representativeRetriever);
    addAll(c);
  }

  /**
   * Constructs a new, empty set; the backing {@code HashMap} instance has the specified initial
   * capacity and default load factor (0.75).
   *
   * @param initialCapacity the initial capacity of the hash table
   * @throws IllegalArgumentException if the initial capacity is less than zero
   */
  public HashUnionFindSet(int initialCapacity, Function<E, R> representativeRetriever) {
    this(initialCapacity, .75f, representativeRetriever);
  }

  /**
   * Constructs a new, empty set; the backing {@code HashMap} instance has the specified initial
   * capacity and the specified load factor.
   *
   * @param initialCapacity the initial capacity of the hash map
   * @param loadFactor the load factor of the hash map
   * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load
   *     factor is non-positive
   */
  public HashUnionFindSet(
      int initialCapacity, float loadFactor, Function<E, R> representativeRetriever) {
    this.representativeRetriever =
        requireNotNull(representativeRetriever, "Representative retrieval function cannot be null");
    this.representatives = new HashMap<>(initialCapacity, loadFactor);
    this.map = new HashMap<>(initialCapacity, loadFactor);

    this.modCount = 0;
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<E> iterator() {
    return new HUFSIterator(map.keySet().iterator(), modCount);
  }

  /** {@inheritDoc} */
  @Override
  public int size() {
    return map.size();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(E e) {
    if (map.containsKey(requireNotNull(e, "Null elements are not supported"))) {
      return false;
    }

    R representativeValue =
        requireNotNull(
            representativeRetriever.apply(e),
            "Representative retrieval function cannot return null");
    Representative<R, E> representative =
        representatives.computeIfAbsent(representativeValue, Representative::new);
    representative.disjointSetValues.add(e);
    map.put(e, representative);
    incrementModCount();
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean remove(Object o) {
    Representative<R, E> representative = map.remove(o);

    if (representative == null) {
      return false;
    }

    representative.disjointSetValues.remove(o);
    tryRemoveRepresentative(representative);
    incrementModCount();
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    representatives.clear();
    map.clear();
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings({"unchecked", "squid:S2975"})
  public Object clone() {
    try {
      HashUnionFindSet<R, E> newSet = (HashUnionFindSet<R, E>) super.clone();
      newSet.representativeRetriever = representativeRetriever;
      newSet.representatives = (HashMap<R, Representative<R, E>>) representatives.clone();
      newSet.map = (HashMap<E, Representative<R, E>>) map.clone();
      return newSet;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Spliterator<E> spliterator() {
    return new HUFSSpliterator<>(map.keySet().spliterator(), modCount);
  }

  /** {@inheritDoc} */
  @Override
  public Object[] toArray() {
    return map.keySet().toArray();
  }

  /** {@inheritDoc} */
  @Override
  public <T> T[] toArray(T[] a) {
    return map.keySet().toArray(a);
  }

  /** {@inheritDoc} */
  @Override
  public void union(E e1, E e2) {
    Representative<R, E> largerRep = findRepresentative(e1);
    Representative<R, E> smallerRep = findRepresentative(e2);

    if (largerRep.value.equals(smallerRep.value) || Objects.equals(e1, e2)) {
      // Already joined or same element
      return;
    }

    if (largerRep.disjointSetValues.size() < smallerRep.disjointSetValues.size()) {
      Representative<R, E> tmp = largerRep;
      largerRep = smallerRep;
      smallerRep = tmp;
    }

    smallerRep.disjointSetParent = largerRep;
    representatives.remove(smallerRep.value);
    largerRep.disjointSetChildren.put(smallerRep.value, smallerRep);
    incrementModCount();
  }

  /** {@inheritDoc} */
  @Override
  public R find(E e) {
    return findRepresentative(e).value;
  }

  /** {@inheritDoc} */
  @Override
  public int numberOfSets() {
    int count = 0;
    for (Representative<R, E> rep : representatives.values()) {
      if (rep.disjointSetParent == null) {
        count++;
      }
    }
    return count;
  }

  /** {@inheritDoc} */
  @Override
  public int representativeSetSize(R r) {
    if (!representatives.containsKey(r)) {
      throw new NoSuchElementException("No such representative with value '%s'".formatted(r));
    }

    Representative<R, E> curr = representatives.get(r);
    int result = curr.disjointSetValues.size();
    for (Representative<R, E> child : curr.disjointSetChildren.values()) {
      result += child.disjointSetValues.size();
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Set<E> representativeSet(R r) {
    if (!representatives.containsKey(r)) {
      throw new NoSuchElementException("No such representative with value '%s'".formatted(r));
    }

    Representative<R, E> curr = representatives.get(r);
    Set<E> result = new HashSet<>(curr.disjointSetValues);
    for (Representative<R, E> child : curr.disjointSetChildren.values()) {
      result.addAll(child.disjointSetValues);
    }
    return Collections.unmodifiableSet(result);
  }

  /**
   * Finds the representative for the given element in the HashUnionFindSet data structure,
   * compacting references and attempting to remove obsolete representatives along the way.
   *
   * @param e the element whose representative is to be found.
   * @return the representative for the given element.
   * @throws NoSuchElementException if the representative for the given element does not exist.
   */
  private Representative<R, E> findRepresentative(E e) {
    Representative<R, E> representative = map.get(e);

    if (representative == null) {
      throw new NoSuchElementException(
          "Cannot find representative of the disjoint set for element");
    }

    while (representative.disjointSetParent != null) {
      map.put(e, representative.disjointSetParent);
      representative.disjointSetValues.remove(e);
      representative.disjointSetParent.disjointSetValues.add(e);

      final Representative<R, E> defensivePointer = representative;
      representative = representative.disjointSetParent;
      tryRemoveRepresentative(defensivePointer);
    }

    return representative;
  }

  /**
   * Tries to remove a representative from the map in the HashUnionFindSet data structure.
   *
   * @param representative the representative to be removed
   */
  private void tryRemoveRepresentative(Representative<R, E> representative) {
    if (!representative.disjointSetValues.isEmpty()) {
      return;
    }

    // The ultimate goal now is to remove current representative from the map
    representatives.remove(representative.value);

    // If there are no more values for this representative, there are two ways to go about it:
    // 1. Leave it, assuming the presence of virtual representatives.
    // 2. Remove it, prettify the state - my choice.
    final Representative<R, E> disjointSetParent = representative.disjointSetParent;

    if (disjointSetParent == null && !representative.disjointSetChildren.isEmpty()) {
      // A representative was a potential root, and it had other children - make first available
      // children a new root
      Representative<R, E> newRoot = representative.disjointSetChildren.values().iterator().next();
      newRoot.disjointSetParent = null;

      for (Representative<R, E> child : representative.disjointSetChildren.values()) {
        if (!newRoot.value.equals(child.value)) {
          child.disjointSetParent = newRoot;
        }
      }

      // Adding new root to the representative map
      representatives.put(newRoot.value, newRoot);

      // Cleaning up current representative
      representative.disjointSetChildren.clear();
    } else if (disjointSetParent != null && representative.disjointSetChildren.isEmpty()) {
      // A representative was NOT a potential root, and it had no other children - can be safely
      // removed, keeping parent in mind
      representative.disjointSetParent.disjointSetChildren.remove(representative.value);
      representative.disjointSetParent = null;
    } else if (disjointSetParent != null) {
      // A representative was NOT a potential root, and it had other children - move those children
      // under current representative parent
      disjointSetParent.disjointSetChildren.putAll(representative.disjointSetChildren);

      for (Representative<R, E> child : representative.disjointSetChildren.values()) {
        child.disjointSetParent = disjointSetParent;
      }

      // Cleaning up current representative
      representative.disjointSetParent = null;
      representative.disjointSetChildren.clear();
    }

    incrementModCount();
  }

  /**
   * Increments the mod count of the HashUnionFindSet data structure. The mod count is used to track
   * the number of structural modifications made to the set. When the mod count reaches
   * Integer.MAX_VALUE, it is reset to 0 to prevent overflow.
   */
  private void incrementModCount() {
    if (modCount == Integer.MAX_VALUE) {
      modCount = 0;
    } else {
      modCount++;
    }
  }

  /**
   * A holder for a representative value of the disjoint set in the HashUnionFindSet data structure.
   *
   * @param <E> the type of the element stored in the representative's node.
   * @param <R> the type of the value stored in the representative.
   */
  static class Representative<R, E> {
    final R value;
    Representative<R, E> disjointSetParent;
    final Map<R, Representative<R, E>> disjointSetChildren;
    final Set<E> disjointSetValues;

    Representative(R value) {
      this.value = value;
      this.disjointSetParent = null;
      this.disjointSetChildren = new HashMap<>();
      this.disjointSetValues = new HashSet<>();
    }
  }

  /**
   * HUFSIterator is an iterator implementation that wraps an original iterator and provides
   * additional checks for concurrent modifications.
   */
  class HUFSIterator implements Iterator<E> {
    final Iterator<E> originalIterator;
    E current;
    int expectedModCount;

    private HUFSIterator(Iterator<E> originalIterator, int expectedModCount) {
      this.originalIterator = originalIterator;
      this.current = null;
      this.expectedModCount = expectedModCount;
    }

    @Override
    public boolean hasNext() {
      checkForComodification();
      return originalIterator.hasNext();
    }

    @Override
    public E next() {
      checkForComodification();
      current = originalIterator.next();
      return current;
    }

    @Override
    public void remove() {
      checkForComodification();

      Representative<R, E> representative = map.get(current);
      representative.disjointSetValues.remove(current);
      tryRemoveRepresentative(representative);

      originalIterator.remove();
      current = null;

      expectedModCount = modCount;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
      Objects.requireNonNull(action);
      while (modCount == expectedModCount && originalIterator.hasNext()) {
        action.accept(originalIterator.next());
      }
      checkForComodification();
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /**
   * This class is an implementation of the Spliterator interface that wraps another Spliterator and
   * performs additional checks for concurrent modification.
   *
   * @param <T> the type of elements returned by this Spliterator
   */
  class HUFSSpliterator<T> implements Spliterator<T> {
    final Spliterator<T> originalSpliterator;
    int expectedModCount;

    private HUFSSpliterator(Spliterator<T> originalSpliterator, int expectedModCount) {
      this.originalSpliterator = originalSpliterator;
      this.expectedModCount = expectedModCount;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      return originalSpliterator.tryAdvance(
          t -> {
            checkForComodification();
            action.accept(t);
          });
    }

    @Override
    public Spliterator<T> trySplit() {
      Spliterator<T> tSpliterator = originalSpliterator.trySplit();
      return tSpliterator == null ? null : new HUFSSpliterator<>(tSpliterator, expectedModCount);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
      originalSpliterator.forEachRemaining(
          t -> {
            checkForComodification();
            action.accept(t);
          });
    }

    @Override
    public long estimateSize() {
      return originalSpliterator.estimateSize();
    }

    @Override
    public int characteristics() {
      return Spliterator.SIZED | Spliterator.DISTINCT;
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /**
   * Returns the specified object if it is not null, otherwise throws an IllegalArgumentException
   * with the specified message.
   *
   * @param object the object to check for null
   * @param message the exception message to be used if the object is null
   * @param <E> the type of the object to check
   * @return the specified object if it is not null
   * @throws IllegalArgumentException if the object is null
   */
  private static <E> E requireNotNull(E object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    } else {
      return object;
    }
  }
}
