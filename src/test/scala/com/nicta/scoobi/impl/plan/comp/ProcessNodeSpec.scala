/**
 * Copyright 2011,2012 National ICT Australia Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nicta.scoobi
package impl
package plan
package comp

import testing.UnitSpecification
import org.specs2.specification.Grouped
import core.WireFormat._
import Scoobi._

class ProcessNodeSpec extends UnitSpecification with Grouped { def is =

  "A ProcessNode node represent the current state of a DList or DObject computation"               ^
                                                                                                   p^
  "Sinks can be added to ParallelDo, GroupByKey and Combine nodes"                                 ^
    "it is possible to add a Sink to a node"                                                       ! g1.e1^
    "it is possible to create a BridgeStore for a node"                                            ! g1.e2^
    "it is possible to update all the sinks to add compression for example"                        ! g1.e3^
                                                                                                   end

  "sinks" - new g1 {
    def parallelDo = ParallelDo.create(Return.unit)(wireFormat[String])

    e1 := parallelDo.addSink(StringSink()).nodeSinks === Seq(StringSink())
    e2 := parallelDo.bridgeStore must not(beNull)
    e3 := parallelDo.addSink(StringSink()).updateSinks(s => s.map(_.compress)).nodeSinks.map(_.isCompressed) === Seq(true)
  }

}
