/*
 * Copyright 2023 Hossein Naderi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hnaderi.example.accounts

import edomata.core._
import edomata.syntax.all._
import cats.implicits._
import cats.data.ValidatedNec

sealed trait Event
object Event {
  final case object Opened extends Event
  final case class Deposited(amount: BigDecimal) extends Event
  final case class Withdrawn(amount: BigDecimal) extends Event
  final case object Closed extends Event
}

sealed trait Rejection
object Rejection {
  final case object ExistingAccount extends Rejection
  final case object NoSuchAccount extends Rejection
  final case object InsufficientBalance extends Rejection
  final case object NotSettled extends Rejection
  final case object AlreadyClosed extends Rejection
  final case object BadRequest extends Rejection
}

sealed trait Account {
  def open: Decision[Rejection, Event, Account.Open] = this.decide {
      case Account.New => Decision.accept(Event.Opened)
      case _   => Decision.reject(Rejection.ExistingAccount)
    }
    .validate(_.mustBeOpen)

  def close: Decision[Rejection, Event, Account] =
    this.perform(mustBeOpen.toDecision.flatMap { account =>
      if (account.balance == 0) Event.Closed.accept
      else Decision.reject(Rejection.NotSettled)
    })

  def withdraw(amount: BigDecimal): Decision[Rejection, Event, Account.Open] = this
    .perform(mustBeOpen.toDecision.flatMap { account =>
      if (account.balance >= amount && amount > 0) Decision.accept(Event.Withdrawn(amount))
      else Decision.reject(Rejection.InsufficientBalance)
    // We can model rejections to have values, which helps a lot for showing error messages, but it's out of scope for this document
    })
    .validate(_.mustBeOpen)

  def deposit(amount: BigDecimal): Decision[Rejection, Event, Account.Open] = this
    .perform(mustBeOpen.toDecision.flatMap { account =>
      if (amount > 0) Decision.accept(Event.Deposited(amount))
      else Decision.reject(Rejection.BadRequest)
    })
    .validate(_.mustBeOpen)

  private def mustBeOpen: ValidatedNec[Rejection, Account.Open] = this match {
    case o @ Account.Open(_) => o.validNec
    case Account.New         => Rejection.NoSuchAccount.invalidNec
    case Account.Close       => Rejection.AlreadyClosed.invalidNec
  }
}

object Account extends DomainModel[Account, Event, Rejection] {
  final case object New extends Account
  final case class Open(balance: BigDecimal) extends Account
  final case object Close extends Account

  def initial = New
  def transition = {
    case Event.Opened => _ => Open(0).validNec
    case Event.Withdrawn(b) =>
      _.mustBeOpen.map(s => s.copy(balance = s.balance - b))
    case Event.Deposited(b) =>
      _.mustBeOpen.map(s => s.copy(balance = s.balance + b))
    case Event.Closed => _ => Close.validNec
  }
}
