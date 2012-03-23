package geotrellis.operation.applicative

import geotrellis.operation._
import geotrellis.process._

/**
 * Some implicit operators to add some syntactic sugar. Example:
 *
 * import geotrellis.operation._
 * import geotrellis.operation.applicative.Implicits._
 *
 * val f = (a:Int) => (b:Int) => (c:Int) => a + b * c
 * val op = f <@> 1 <*> 2 <*> 3
 *
 * server.run(op) // returns 7
 */
object Implicits {
  implicit def applyOperator[A, Z:Manifest](lhs:Op[A => Z]) = new {
    def <*>(rhs:Op[A]) = Apply(rhs)(lhs)
  }

  // This coorrespond's to Haskell's <$> which isn't legal in Scala.
  implicit def fmapOperator[A, Z:Manifest](lhs:Function1[A, Z]) = new {
    def <@>(rhs:Op[A]) = Fmap(rhs)(lhs)
  }
}
