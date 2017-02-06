package org.shapelogic.sc.polygon

import scala.collection.mutable.Set
import scala.collection.mutable.Map
import scala.collection.mutable.ArrayBuffer
import org.shapelogic.sc.calculation.CalcInvoke

import spire.implicits._
import scala.collection.mutable.HashSet

object Polygon {
  val MAX_DISTANCE_BETWEEN_CLUSTER_POINTS: Double = 2;

}
class Polygon(annotatedShape: AnnotatedShapeImplementation) extends BaseAnnotatedShape(annotatedShape)
    with IPolygon2D with CalcInvoke[Polygon] with Cloneable with PointReplacable[Polygon] {
  import Polygon._
  var _bBox = new BBox()
  var _lines: Set[CLine] = Set()
  var _points: Set[IPoint2D] = Set()
  protected var _dirty = true;
  protected var _aspectRatio: Double = 0
  protected var _closed: Boolean = false
  protected var _endPointCount: Int = -1;
  protected var _pointsCountMap = Map[IPoint2D, Integer]()
  protected val _pointsToLineMap = Map[IPoint2D, Set[CLine]]()
  protected var _version: Int = 0
  var _currentMultiLine: MultiLine = new MultiLine(this.getAnnotatedShape())
  protected var _endPointsClusters = ArrayBuffer[Set[IPoint2D]]()
  //I could make this lazy
  override val _annotatedShape: AnnotatedShapeImplementation = annotatedShape
  var _polygonImprovers: ArrayBuffer[Improver[Polygon]] = new ArrayBuffer() //XXX takes more imports
  protected var _perimeter: Double = 0

  def this() {
    this(null);
  }

  /**
   * Was constructor
   */
  def init() {
    //  		super(annotatedShape)
    setup();
    //    internalFactory();
  }

  //  /** All the objects that needs special version should be created here. */
  //  protected def internalFactory() = {
  //    _polygonImprovers = new ArrayBuffer < Improver < Polygon > >();
  //    _polygonImprovers.add(new FilterPolygonForSmallLines());
  //    _polygonImprovers.add(new PolygonAnnotator());
  //  }

  override def getBBox(): BBox = {
    getValue();
    return _bBox;
  }

  override def getLines(): Set[CLine] = {
    return _lines;
  }

  override def getPoints(): Set[IPoint2D] = {
    return _points;
  }

  override def getAspectRatio(): Double = {
    getValue();
    if (_bBox.minVal != null) {
      val lenX: Double = _bBox.maxVal.getX() - _bBox.minVal.getX();
      val lenY: Double = _bBox.maxVal.getY() - _bBox.minVal.getY();
      if (lenX > 0)
        _aspectRatio = lenY / lenX;
      else
        _aspectRatio = Double.PositiveInfinity;
    }
    return _aspectRatio;
  }

  /** Does this make sense for a polygon or only for multi line */
  override def isClosed(): Boolean = {
    return _closed;
  }

  override def isDirty(): Boolean = {
    return _dirty;
  }
  //
  //	@Override
  //	public void setup() {
  //		_lines = new TreeSet<CLine>();
  //		_points = new TreeSet<IPoint2D>();
  //		_bBox = new BBox();
  //	}
  //
  def containsPoint(point: IPoint2D): Boolean = {
    return _points.contains(point);
  }

  def containsLine(line: ILine2D): Boolean = {
    return _lines.contains(line.asInstanceOf[CLine]) //XXX 
  }

  def addPoint(point: IPoint2D): Unit = {
    if (!containsPoint(point))
      _points.add(point);
  }

  /** this should not be used use addIndependentLine() instead */
  def addLine(point1: IPoint2D, point2: IPoint2D): CLine = {
    val line: CLine = CLine.makeUnordered(point1, point2)
    if (!containsLine(line))
      _lines.add(line);
    addPoint(point1);
    addPoint(point2);
    return line;
  }

  /**
   * this should not be used use addIndependentLine() instead
   *
   */
  @deprecated("Not sure what is bad about this", "2017-02-02")
  def addLine(line: CLine): CLine = {
    if (!containsLine(line)) {
      _lines.add(line);
      addPoint(line.getStart());
      addPoint(line.getEnd());
    }
    return line;
  }

  override def invoke(): Polygon = {
    _bBox = findBbox();
    findPointCount();
    _dirty = false;
    return this;
  }

  def findBbox(): BBox = {
    if (_bBox == null)
      _bBox = new BBox();
    _points.foreach { (pointInPolygon: IPoint2D) =>
      _bBox.addPoint(pointInPolygon)
    }
    return _bBox;
  }
  //    
  //    /** Return a cleaned up polygon 
  //     * 
  //     * @param onlyInt Change all coordinates to integers
  //     * @param procentage of diagonal of b box that should be considered as same point
  //     */
  //    public Polygon cleanUp(Boolean onlyInt, Double procentage) {
  //        findBbox();
  //        Double threshold = _bBox.getDiameter() * procentage;
  //        List<IPoint2D> roundedPoints = new ArrayBuffer<IPoint2D>();
  //        Map<IPoint2D, IPoint2D> pointMap = new HashMap<IPoint2D, IPoint2D>();
  //        for (IPoint2D point : _points) {
  //        	IPoint2D roundedPoint = point;
  //        	if (onlyInt) {
  //        		roundedPoint = point.copy().round();
  //        		if (roundedPoint.equals(point) )
  //        			roundedPoint = point; //uses the same point, do not create new 
  //        	}
  //        	Iterator<IPoint2D> roundPointIterator = roundedPoints.iterator();
  //        	Boolean moreRoundedPoints = roundPointIterator.hasNext();
  //        	IPoint2D foundPoint = null;
  //        	while (moreRoundedPoints) {
  //        		IPoint2D point2 = roundPointIterator.next(); 
  //        		pointMap.put(point, point);
  //        		if (roundedPoint.distance(point2) < threshold) {
  //        			foundPoint = point2;
  //        			moreRoundedPoints = false;
  //        		}
  //        		else
  //        			moreRoundedPoints = roundPointIterator.hasNext();
  //        	}
  //        	if (foundPoint == null) {
  //        		roundedPoints.add(roundedPoint);
  //        		pointMap.put(point, roundedPoint);
  //        	}
  //        	else { 
  //        		pointMap.put(point, foundPoint);
  //        	}
  //          
  //        }
  //        return replacePointsInMap(pointMap,null);
  //    }
  //
  /** register a list of improvers and call them here */
  def improve(): Polygon = {
    if (_polygonImprovers == null)
      return this;
    var result: Polygon = this;
    _polygonImprovers.foreach { (improver: Improver[Polygon]) =>
      improver.setInput(result)
      result = improver.getValue()
    }
    return result;
  }

  override def getValue(): Polygon = {
    if (isDirty())
      invoke();
    return this;
  }

  def getVerticalLines() = { //List < CLine > 
    _lines.filter(_.isVertical)
  }

  def getHorizontalLines() = {
    _lines.filter(_.isHorizontal)
  }

  /** Find how many lines each point is part of by making a map */
  def getPointsCountMap(): Map[IPoint2D, Integer] = {
    if (_pointsCountMap == null) {
      _pointsCountMap = Map[IPoint2D, Integer]()
      _lines.foreach { (line: CLine) =>
        {
          var startCount: Integer = _pointsCountMap.getOrElse(line.getStart(), null)
          if (startCount == null)
            startCount = 1;
          else
            startCount = startCount + 1
          _pointsCountMap.put(line.getStart(), startCount);
          if (!line.isPoint()) {
            var endCount: Integer = _pointsCountMap.getOrElse(line.getEnd(), null)
            if (endCount == null)
              endCount = 1;
            else
              endCount = endCount + 1
            _pointsCountMap.put(line.getEnd(), endCount);
          }
        }
      }
    }
    return _pointsCountMap;
  }

  //	/** Find how many lines each point is part of by making a map */
  //	public Map<IPoint2D,Set<CLine>> getPointsToLineMap() {
  //		if (_pointsToLineMap == null) {
  //			_pointsToLineMap = new TreeMap<IPoint2D,Set<CLine>>();
  //			for (CLine line: _lines) {
  //				Set<CLine> lineSetForStartPoint = _pointsToLineMap.get(line.getStart());
  //				if (lineSetForStartPoint == null) {
  //					lineSetForStartPoint = new TreeSet<CLine>();
  //					_pointsToLineMap.put(line.getStart(),lineSetForStartPoint);
  //				}
  //				lineSetForStartPoint.add(line);
  //				Set<CLine> lineSetForEndPoint = _pointsToLineMap.get(line.getEnd());
  //				if (lineSetForEndPoint == null) {
  //					lineSetForEndPoint = new TreeSet<CLine>();
  //					_pointsToLineMap.put(line.getEnd(),lineSetForEndPoint);
  //				}
  //				lineSetForEndPoint.add(line);
  //			}
  //		}
  //		return _pointsToLineMap;
  //	}
  //	
  def findPointCount(): Int = {
    getPointsCountMap();
    val ONE: Integer = 1;
    _endPointCount = 0;
    _pointsCountMap.keys.toSeq.foreach { point =>
      {
        if (ONE.equals(_pointsCountMap.get(point)))
          _endPointCount = _endPointCount + 1
      }
    }
    return _endPointCount;
  }

  def getEndPointCount(): Int = {
    getValue();
    return _endPointCount;
  }

  def getLinesForPoint(point: IPoint2D): Set[CLine] = {
    val result = new HashSet[CLine]();
    if (point == null)
      return result
    getValue();
    if (!_points.contains(point))
      return result;
    _lines.foreach { (line: CLine) =>
      if (line.getStart().equals(point) || line.getEnd().equals(point))
        result.add(line);
    }
    return result
  }

  def getVersion(): Int = {
    return _version;
  }

  def setVersion(version: Int) = {
    _version = version;
  }

  def startMultiLine(): Unit = {
    if (_currentMultiLine == null)
      _currentMultiLine = new MultiLine(this.getAnnotatedShape());
  }

  def addBeforeStart(newPoint: IPoint2D): Unit = {
    _currentMultiLine.addBeforeStart(newPoint);
  }

  def addAfterEnd(newPoint: IPoint2D): Unit = {
    _currentMultiLine.addAfterEnd(newPoint);
  }

  /** Add all the lines segments in the multi line to _lines */
  def endMultiLine(): Unit = {
    if (_currentMultiLine != null && _currentMultiLine.getPoints().size > 0)
      addMultiLine(_currentMultiLine)
    _currentMultiLine = null;
  }

  def addMultiLine(multiLine: MultiLine): Unit = {
    var lastPoint: IPoint2D = null;
    var linesAdded: Int = 0;
    multiLine.getPoints().foreach {
      (point: IPoint2D) =>
        if (lastPoint == null) {
          lastPoint = point;
        } else {
          addLine(lastPoint, point);
          linesAdded += 1
        }
        lastPoint = point;
    }
    if (linesAdded == 0 && lastPoint != null)
      addLine(lastPoint, lastPoint)
  }

  def getCurrentMultiLine(): MultiLine = {
    _currentMultiLine;
  }

  def getEndPointsClusters(): ArrayBuffer[Set[IPoint2D]] = {
    if (_endPointsClusters == null) {
      _endPointsClusters = new ArrayBuffer[Set[IPoint2D]]()
      getPoints().foreach { (point: IPoint2D) =>
        //  				inner_loop: 
        var stopInner = false
        _endPointsClusters.foreach { (cluster: Set[IPoint2D]) =>
          if (!stopInner && point.distance(cluster.iterator.next) <= MAX_DISTANCE_BETWEEN_CLUSTER_POINTS) {
            cluster.add(point);
            stopInner = true
          }
        }
        val cluster = new HashSet[IPoint2D]()
        cluster.add(point)
        _endPointsClusters.append(cluster)
      }
    }
    return _endPointsClusters;
  }

  def getEndPointsMultiClusters(): ArrayBuffer[Set[IPoint2D]] = {
    val result: ArrayBuffer[Set[IPoint2D]] = new ArrayBuffer[Set[IPoint2D]]()
    getEndPointsClusters().foreach { (cluster: Set[IPoint2D]) =>
      if (cluster.size > 1)
        result.append(cluster)
    }
    return result;
  }

  override def clone(): Object = {
    try {
      return super.clone();
    } catch {
      case e: CloneNotSupportedException => {
        e.printStackTrace();
        return null;
      }
    }
  }

  /** No filtering in first version */

  override def replacePointsInMap(pointReplacementMap: Map[IPoint2D, IPoint2D],
    annotatedShape: AnnotatedShapeImplementation): Polygon = {
    val replacedPolygon: Polygon = new Polygon(annotatedShape);
    replacedPolygon.setup();
    _lines.foreach {
      (line: CLine) =>
        {
          val newLine = line.replacePointsInMap(pointReplacementMap, annotatedShape);
          if (!newLine.isPoint()) {
            replacedPolygon.addIndependentLine(newLine);
          }
        }
    }
    var annotationForOldPolygon: Set[Object] = null;
    if (annotatedShape != null)
      annotationForOldPolygon = annotatedShape.getAnnotationForShapes(this);
    if (annotationForOldPolygon != null) {
      annotatedShape.putAllAnnotation(replacedPolygon, annotationForOldPolygon);
    }
    return replacedPolygon;
  }

  override def getCenter(): IPoint2D = {
    return _bBox.getCenter();
  }

  override def getDiameter(): Double = {
    return getBBox().getDiameter();
  }

  def setPolygonImprovers(improvers: ArrayBuffer[Improver[Polygon]]) {
    _polygonImprovers = improvers;
  }

  /** To have the same interface as MultiLinePolygon */
  def addIndependentLine(point1: IPoint2D, point2: IPoint2D): CLine = {
    return addLine(point1, point2);
  }

  /**
   * Most of the time this should not be used use the version taking input
   * points instead
   */
  def addIndependentLine(line: CLine): CLine = {
    return addLine(line)
  }

  /** To have the same interface as MultiLinePolygon */
  def getIndependentLines(): Set[CLine] = {
    return getLines();
  }

  /**
   * To have the same interface as MultiLinePolygon
   * returns null
   * since this and the independent lines are supposed to be all the lines
   */
  def getMultiLines(): ArrayBuffer[MultiLine] = {
    return null;
  }
  //	
  //	public <Element> Collection<Element> filter(IFilter<Polygon, Element> filterObject) {
  //		filterObject.setParent(this);
  //		return filterObject.filter();
  //	}

  //	public <Element> Collection<Element> filter(String inputExpression) {
  //		IFilter<Polygon, Element> filterObject = FilterFactory.makeTreeFilter(inputExpression);
  //		filterObject.setParent(this);
  //		return filterObject.filter();
  //	}

  def getHoleCount(): Int = {
    return getLines().size + 1 - getPoints().size
  }

  //	@Override
  //	public String toString() {
  //		StringBuffer result = new StringBuffer();
  //        internalInfo(result);
  //        printAnnotation(result);
  //		return result.toString();
  //	}

  //    public String internalInfo(StringBuffer sb) {
  //        sb.append("\n\n=====Class: ").append(getClass().getSimpleName()).append("=====\n");
  //        if (null != _bBox)
  //            sb.append(_bBox.toString());
  //        if (null != _currentMultiLine)
  //            _currentMultiLine.internalInfo(sb);
  //        else {
  //            sb.append("Lines:\n");
  //            for (CLine line: getLines()) {
  //                sb.append(line);
  //            }
  //            sb.append("\nPoints:\n");
  //            for (IPoint2D point: getPoints()) {
  //                sb.append(point);
  //            }
  //        }
  //        return sb.toString();
  //    }

  //    public String printAnnotation(StringBuffer result) {
  //		result.append("\nAnnotations:\n");
  //		Map<Object, Set<GeometricShape2D>> map = getAnnotatedShape().getMap();
  //		for (Entry<Object, Set<GeometricShape2D>> entry: map.entrySet())
  //			result.append(entry.getKey() +":\n" + entry.getValue() + "\n");
  //		result.append("\naspectRatio: " + getBBox().getAspectRatio());
  //		return result.toString();
  //    }

  def getPerimeter(): Double = {
    return _perimeter;
  }

  def setPerimeter(perimeter: Double) {
    _perimeter = perimeter;
  }

}