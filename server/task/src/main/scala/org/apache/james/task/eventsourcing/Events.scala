/** **************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                 *
 * *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 * ***************************************************************/
package org.apache.james.task.eventsourcing

import java.net.{InetAddress, UnknownHostException}

import org.apache.james.eventsourcing.{Event, EventId}
import org.apache.james.task.Task
import org.apache.james.task.Task.Result

sealed abstract class TaskEvent(aggregateId: TaskAggregateId, val eventId: EventId) extends Event {
  override def getAggregateId: TaskAggregateId = aggregateId
}

sealed abstract class TerminalTaskEvent(aggregateId: TaskAggregateId, override val eventId: EventId) extends TaskEvent(aggregateId, eventId)

case class Hostname(private val value: String) {
  def asString: String = value
}

object Hostname {
  def fromLocalHostname = try new Hostname(InetAddress.getLocalHost.getHostName)
  catch {
    case e: UnknownHostException =>
      throw new UnconfigurableHostnameException("Hostname can not be retrieved", e)
  }
}

private class UnconfigurableHostnameException(val message: String, val originException: Exception) extends RuntimeException(message, originException) {
}


case class Created(aggregateId: TaskAggregateId, override val eventId: EventId, task: Task, hostname: Hostname) extends TaskEvent(aggregateId, eventId)

case class Started(aggregateId: TaskAggregateId, override val eventId: EventId, hostname: Hostname) extends TaskEvent(aggregateId, eventId)

case class CancelRequested(aggregateId: TaskAggregateId, override val eventId: EventId, hostname: Hostname) extends TaskEvent(aggregateId, eventId)

case class Completed(aggregateId: TaskAggregateId, override val eventId: EventId, result: Result) extends TerminalTaskEvent(aggregateId, eventId)

case class Failed(aggregateId: TaskAggregateId, override val eventId: EventId) extends TerminalTaskEvent(aggregateId, eventId)

case class Cancelled(aggregateId: TaskAggregateId, override val eventId: EventId) extends TerminalTaskEvent(aggregateId, eventId)
