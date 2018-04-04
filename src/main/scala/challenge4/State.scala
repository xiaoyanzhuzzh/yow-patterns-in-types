package challenge4

import core._

/*
 * A state data type that represents the threading
 * of some state value through computations.
 */
case class State[S, A](run: S => (S, A)) {

  /*
   * Exercise 4.1:
   *
   * Implement map for State[S, A].
   *
   * The following laws must hold:
   *  1) r.map(z => z) == r
   *  2) r.map(z => f(g(z))) == r.map(g).map(f)
   *
   */
  def map[B](f: A => B): State[S, B] =
    State(s => {
      val (ss, a) = run(s)
      (ss, f(a))
    })

  /*
   * Exercise 4.2:
   *
   * Implement flatMap (a.k.a. bind, a.k.a. >>=).
   *
   * The following law must hold:
   *   r.flatMap(f).flatMap(g) == r.flatMap(z => f(z).flatMap(g))
   *
   */
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State(s => {
      val (ss, a) = run(s)
      f(a).run(ss)
    })
}

object State {
  /*
   * Exercise 4.3:
   *
   * Implement value  (a.k.a. return, point, pure).
   *
   * Hint: Try using State constructor.
   */
  def value[S, A](a: => A): State[S, A] =
    State(s => (s, a))

  /*
   * Exercise 4.4:
   *
   * Implement get.
   *
   * Get provides access to the current state (S).
   *
   * Hint: Try using State constructor.
   */
  def get[S]: State[S, S] =
    State(s => (s, s))

  /*
   * Exercise 4.5:
   *
   * Implement gets.
   *
   * Gets provides access to a view of the current state (S).
   *
   * Hint: Try building on get.
   */
  def gets[S, A](f: S => A): State[S, A] =
    get.map(f)

  /*
   * Exercise 4.6:
   *
   * Implement modify.
   *
   * Update the current state and produce no value.
   *
   * Hint: Try using State constructor.
   */
  def modify[S](f: S => S): State[S, Unit] =
    State(s => (f(s), ()))

  /*
   * Exercise 4.7:
   *
   * Implement put.
   *
   * Clobber the current state and produce no value.
   *
   * Hint: Try building on modify.
   */
  def put[S](s: S): State[S, Unit] =
    modify[S](_ => s)

  class State_[S] {
    type l[a] = State[S, a]
  }

  implicit def StateMonad[S]: Monad[State_[S]#l] =
    new Monad[State_[S]#l] {
      def point[A](a: => A) = value(a)
      def bind[A, B](a: State[S, A])(f: A => State[S, B]) = a flatMap f
    }
}
