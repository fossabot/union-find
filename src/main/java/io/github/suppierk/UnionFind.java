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

import java.util.Objects;
import java.util.Set;

/**
 * UnionFind interface represents a disjoint set data structure.
 *
 * <p>In computer science, a disjoint-set data structure, also called a union–find data structure or
 * merge–find set, is a data structure that stores a collection of the disjoint (non-overlapping)
 * sets.
 *
 * <p>Equivalently, it stores a partition of a set into disjoint subsets.
 *
 * <p>It provides operations for adding new sets, merging sets (replacing them by their union), and
 * finding a representative member of a set.
 *
 * <p>The last operation makes it possible to find out efficiently if any two elements are in the
 * same or different sets.
 *
 * <p>This data structure can be frequently seen in such real-life use cases as:
 *
 * <ul>
 *   <li>Image processing (image segmentation).
 *   <li>Social network analysis (grouping people by certain characteristics).
 *   <li>Computer networks (tracking nodes connectivity).
 *   <li>Circuit analysis (detecting cycles, same as for Kruskal's graph connectivity algorithm).
 *   <li>Computational geometry (Voronoi diagrams, Delaunay triangulation, etc.).
 * </ul>
 *
 * @param <R> the type of the value used to represent sets
 * @param <E> the type of the elements stored in the sets
 * @see <a href="https://en.wikipedia.org/wiki/Disjoint-set_data_structure">Disjoint-set data
 *     structure</a>
 */
public interface UnionFind<R, E> {
  /**
   * Performs the union operation between two elements in the disjoint set.
   *
   * <p>The elements e1 and e2 will be placed in the same set after the union operation is complete.
   *
   * <p>If e1 and e2 are already in the same set, no action will be taken.
   *
   * @param e1 the first element
   * @param e2 the second element
   */
  void union(E e1, E e2);

  /**
   * Finds the representative value of the set that contains the given element.
   *
   * @param e the element whose representative value is to be found
   * @return the representative value of the set that contains the element {@code e}
   */
  R find(E e);

  /**
   * @return the number of sets in the disjoint set data structure
   */
  int numberOfSets();

  /**
   * @param e the element whose set size is to be found
   * @return the size of the set that contains the element {@code e}
   */
  default int elementSetSize(E e) {
    return representativeSetSize(find(e));
  }

  /**
   * @param r the representative value of a set
   * @return the size of the set represented by the given representative value {@code r}
   */
  int representativeSetSize(R r);

  /**
   * @param e the element whose set is to be retrieved
   * @return a set of elements in the set that contains the specified element {@code e}
   */
  default Set<E> elementSet(E e) {
    return representativeSet(find(e));
  }

  /**
   * @param r the representative value of a set
   * @return a set of elements in the set represented by the given representative value {@code r}
   */
  Set<E> representativeSet(R r);

  /**
   * Returns true if the two elements are connected, meaning they are in the same set.
   *
   * @param e1 the first element
   * @param e2 the second element
   * @return true if the two elements are connected, false otherwise
   */
  default boolean connected(E e1, E e2) {
    return Objects.equals(find(e1), find(e2));
  }
}
