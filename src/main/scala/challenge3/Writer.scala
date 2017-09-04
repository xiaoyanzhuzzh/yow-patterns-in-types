package challenge3

import core._
import Syntax._
import com.sun.xml.internal.ws.policy.sourcemodel.ModelNode

/*
 * A writer data type that represents the pair of some
 * writer content with the production of a value.
 */
case class Writer[W, A](log: W, value: A) {

  def run: (W, A) =
    (log, value)

  /*
   * Exercise 3.1:
   *
   * Implement map for Writer[W, A].
   *
   * The following laws must hold:
   *  1) r.map(z => z) == r
   *  2) r.map(z => f(g(z))) == r.map(g).map(f)
   *
   */
  def map[B](f: A => B): Writer[W, B] =
    Writer(log, f(value))

  /*
   * Exercise 3.2:
   *
   * Implement flatMap (a.k.a. bind, a.k.a. >>=).
   *
   * The following law must hold:
   *   r.flatMap(f).flatMap(g) == r.flatMap(z => f(z).flatMap(g))
   *
   */
  def flatMap[B](f: A => Writer[W, B])(implicit M: Monoid[W]): Writer[W, B] = {
    val nextWriter = f(value)
    Writer(Monoid[W].append(log, nextWriter.log), nextWriter.value)
  }

}

object Writer {
  /*
   * Exercise 3.3:
   *
   * Implement value  (a.k.a. return, point, pure) given a
   * Monoid for W.
   *
   * Hint: Try using Writer constructor.
   */
  def value[W: Monoid, A](a: A): Writer[W, A] = Writer(Monoid[W].zero, a)

  /*
   * Exercise 3.4:
   *
   * Implement tell.
   *
   * Tell appends the writer content w and produces no value.
   *
   * Hint: Try using Writer constructor.
   */
  def tell[W](w: W): Writer[W, Unit] = Writer(w, ())

  /*
   * Exercise 3.5:
   *
   * Sequence, a list of Readers, to a Reader of Lists.
   */
  def sequence[W: Monoid, A](writers: List[Writer[W, A]]): Writer[W, List[A]] =
    writers.foldRight(Writer(Monoid[W].zero, List[A]()))((next, acc) => {
      Writer(Monoid[W].append(next.log, acc.log), next.value :: acc.value)
    })

  class Writer_[W] {
    type l[a] = Writer[W, a]
  }

  implicit def WriterMonad[W: Monoid]: Monad[Writer_[W]#l] =
    new Monad[Writer_[W]#l] {
      def point[A](a: => A) = value[W, A](a)
      def bind[A, B](a: Writer[W, A])(f: A => Writer[W, B]) = a flatMap f
    }

  implicit def WriterEqual[W: Equal, A: Equal] =
    Equal.from[Writer[W, A]]((a, b) => (a.log -> a.value) === (b.log -> b.value))

  implicit def WriterMoniod[W: Monoid, A: Monoid]: Monoid[Writer[W, A]] =
    new Monoid[Writer[W, A]] {
      def zero = Writer.value[W, A](Monoid[A].zero)
      def append(l: Writer[W, A], r: => Writer[W, A]) =
        Writer(Monoid[W].append(l.log, r.log), Monoid[A].append(l.value, r.value))
    }
}

/*
 * *Challenge* Exercise 3.6: Stocks + Stats.
 *
 * We have some stock prices over time, and we make a simple
 * adjustment:
 *  Map across each ticker price and do an adjustment by adding
 *  1000 cents to every value under 10000 and 10 cents to every
 *  value equal to or over 10000.
 *
 * However, while we compute this answer we also want to caculate
 * summary statistics for our data, specifically, min, max, total,
 * and count.
 *
 * Use the Writer data type to compute stats whilst we calculate
 * our adjustments.
 *
 * Complete the implementation, some of the methods are provided
 * fill in the remainder, to complete the spec.
 */
object Example {
  case class Stats(min: Int, max: Int, total: Int, count: Int)
  case class Stock(ticker: String, date: String, cents: Int)

  /**
   * Implement our algorthim.
   *
   * Hint: Writer(W, A) and Writer.sequence will be useful here.
   */
  def stocks(data: List[Stock]): (Stats, List[Stock]) =
    Writer.sequence(data.map(stock => {
      val newCents = stock.cents + (if (stock.cents > 10000) 10 else 1000)
      //Writer[Stats, Stock](
      //  Stats(newCents, newCents, newCents, 1),
      //  Stock(stock.ticker, stock.date, newCents)
      //)
//      Writer.tell(Stats(newCents, newCents, newCents, 1)).map(_ => Stock(stock.ticker, stock.date, newCents))
      for {
        _ <- Writer.tell(Stats(newCents, newCents, newCents, 1))
      } yield Stock(stock.ticker, stock.date, newCents)
    })).run

  /**
   * A monoid for Stats.
   */
  implicit def StatsMonoid: Monoid[Stats] =
    new Monoid[Stats] {
      def zero = Stats(0, 0, 0, 0)
      def append(l: Stats, r: => Stats) =
        Stats(math.min(l.min, r.min), math.max(l.max, r.max), l.total + r.total, l.count + r.count)
    }

  def exampledata = List(
    Stock("FAKE", "2012-01-01", 10000)
  , Stock("FAKE", "2012-01-02", 10020)
  , Stock("FAKE", "2012-01-03", 10022)
  , Stock("FAKE", "2012-01-04", 10005)
  , Stock("FAKE", "2012-01-05",  9911)
  , Stock("FAKE", "2012-01-06",  6023)
  , Stock("FAKE", "2012-01-07",  7019)
  , Stock("CAKE", "2012-01-01",     1)
  , Stock("CAKE", "2012-01-02",     2)
  , Stock("CAKE", "2012-01-03",     3)
  , Stock("CAKE", "2012-01-04",     4)
  , Stock("CAKE", "2012-01-05",     5)
  , Stock("CAKE", "2012-01-06",     6)
  , Stock("CAKE", "2012-01-07",     7)
  , Stock("BAKE", "2012-01-01", 99999)
  , Stock("BAKE", "2012-01-02", 99999)
  , Stock("BAKE", "2012-01-03", 99999)
  , Stock("BAKE", "2012-01-04", 99999)
  , Stock("BAKE", "2012-01-05", 99999)
  , Stock("BAKE", "2012-01-06", 99999)
  , Stock("BAKE", "2012-01-07", 99999)
  , Stock("LAKE", "2012-01-01", 10012)
  , Stock("LAKE", "2012-01-02",  7000)
  , Stock("LAKE", "2012-01-03",  1234)
  , Stock("LAKE", "2012-01-04",    10)
  , Stock("LAKE", "2012-01-05",  6000)
  , Stock("LAKE", "2012-01-06",  6099)
  , Stock("LAKE", "2012-01-07",  5999)
  )
}
