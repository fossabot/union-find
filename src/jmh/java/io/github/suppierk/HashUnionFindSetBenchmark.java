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

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@SuppressWarnings("unused")
public class HashUnionFindSetBenchmark {
  static final int LIST_SIZE = 1_000_000;

  public HashUnionFindSet<Integer, Integer> hufs;

  public int firstValue;
  public int secondValue;

  @Setup(Level.Iteration)
  public void setupIteration() {
    hufs = new HashUnionFindSet<>(Function.identity());

    for (int i = 0; i < LIST_SIZE; i++) {
      hufs.add(i);
    }
  }

  @Setup(Level.Invocation)
  public void setupInvocation() {
    firstValue = ThreadLocalRandom.current().nextInt(0, LIST_SIZE);
    secondValue = ThreadLocalRandom.current().nextInt(0, LIST_SIZE);
  }

  @Benchmark
  public void unionBenchmark() {
    // This leverages find operation underneath
    hufs.union(firstValue, secondValue);
  }

  @Benchmark
  public void findBenchmark(Blackhole blackhole) {
    blackhole.consume(hufs.find(firstValue));
  }

  public static void main(String[] args) throws Exception {
    Main.main(args);
  }
}
