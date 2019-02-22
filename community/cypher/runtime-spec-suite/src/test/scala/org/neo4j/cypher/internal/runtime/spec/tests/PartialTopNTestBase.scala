/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.runtime.spec.tests

import org.neo4j.cypher.internal.runtime.spec._
import org.neo4j.cypher.internal.v4_0.logical.plans.{Ascending, Descending}
import org.neo4j.cypher.internal.{CypherRuntime, RuntimeContext}

abstract class PartialTopNTestBase[CONTEXT <: RuntimeContext](
                                                               edition: Edition[CONTEXT],
                                                               runtime: CypherRuntime[CONTEXT],
                                                               sizeHint: Int
                                                             ) extends RuntimeTestSuite[CONTEXT](edition, runtime) {

  test("empty input gives empty output") {
    // when
    val input = inputValues()

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 5)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y").withNoRows()
  }

  test("simple top n if sorted column only has one value") {
    // when
    val input = inputValues(
      Array("A", 2),
      Array("A", 1),
      Array("A", 3),
      Array("A", 0),
      Array("A", 4),
      Array("A", 2)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 2)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow("A", 0)
      .withRow("A", 1)
  }

  test("simple top n for chunk size 1") {
    // when
    val input = inputValues(
      Array("A", 2),
      Array("B", 1),
      Array("C", 3),
      Array("D", 0),
      Array("E", 4),
      Array("F", 2)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 4)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow("A", 2)
      .withRow("B", 1)
      .withRow("C", 3)
      .withRow("D", 0)
  }

  test("simple top n if sorted column has more values - return subset of first block") {
    // when
    val input = inputValues(
      Array("A", 2),
      Array("A", 1),
      Array("A", 3),
      Array("B", 0)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 2)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow("A", 1)
      .withRow("A", 2)
  }

  test("simple top n if sorted column has more values - return whole first block") {
    // when
    val input = inputValues(
      Array("A", 2),
      Array("A", 1),
      Array("A", 3),
      Array("B", 0)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 3)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow("A", 1)
      .withRow("A", 2)
      .withRow("A", 3)
  }

  test("simple top n if results from two blocks must be returned - one from second block") {
    // when
    val input = inputValues(
      Array(1, 5),
      Array(1, 5),
      Array(1, 2),
      Array(1, 2),
      Array(2, 4),
      Array(2, 3),
      Array(2, 5),
      Array(3, 1),
      Array(3, 0)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 5)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow(1, 2)
      .withRow(1, 2)
      .withRow(1, 5)
      .withRow(1, 5)
      .withRow(2, 3)
  }

  test("simple top n if results from two blocks must be returned - two from second block") {
    // when
    val input = inputValues(
      Array(1, 5),
      Array(1, 5),
      Array(1, 2),
      Array(1, 2),
      Array(2, 4),
      Array(2, 3),
      Array(2, 5),
      Array(3, 1),
      Array(3, 0)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 6)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow(1, 2)
      .withRow(1, 2)
      .withRow(1, 5)
      .withRow(1, 5)
      .withRow(2, 3)
      .withRow(2, 4)
  }

  test("Top n with limit > Integer.Max works") {
    // when
    val input = inputValues(
      Array(1, 5),
      Array(1, 5),
      Array(1, 2),
      Array(1, 2),
      Array(2, 4),
      Array(2, 3),
      Array(2, 5),
      Array(3, 1),
      Array(3, 0)
    )

    val logicalQuery = new LogicalQueryBuilder(this)
      .produceResults("x", "y")
      .partialTop(Seq(Ascending("x")), Seq(Ascending("y")), 1L + Int.MaxValue)
      .input("x", "y")
      .build()

    val runtimeResult = execute(logicalQuery, runtime, input)

    // then
    runtimeResult should beColumns("x", "y")
      .withRow(1, 2)
      .withRow(1, 2)
      .withRow(1, 5)
      .withRow(1, 5)
      .withRow(2, 3)
      .withRow(2, 4)
      .withRow(2, 5)
      .withRow(3, 0)
      .withRow(3, 1)
  }

}