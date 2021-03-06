package org.shapelogic.sc.polygon

import scala.collection.mutable.Map

/**
 *
 * @author Sami Badawi
 *
 */
trait PointReplacable[Result] {
  def replacePointsInMap(
    pointReplacementMap: Map[IPoint2D, IPoint2D],
    annotatedShape: AnnotatedShapeImplementation): Result

}
