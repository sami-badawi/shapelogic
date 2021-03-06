package org.shapelogic.sc.morphology

import spire.implicits._
import spire.math._
import org.shapelogic.sc.image.BufferImage
import org.shapelogic.sc.image.HasBufferImage

/**
 * Take specialized binary gray scale byte image
 *
 * Code is a port from ImageJ from
 * https://raw.githubusercontent.com/imagej/imagej1/master/ij/process/BinaryProcessor.java
 *
 * Found this reference explaining Hilditch's Algorithm
 *
 * http://cgm.cs.mcgill.ca/~godfried/teaching/projects97/azar/skeleton.html
 *
 * Skeletonize does not modify the input image
 *
 * @param image input image need to be gray scale
 * @param inverted true if black should be background
 */
class Skeletonize(
    image: BufferImage[Byte],
    inverted: Boolean,
    findOutline: Boolean = false) extends HasBufferImage[Byte] {

  var verboseLogging = false
  val margin = if (findOutline) 2 else 1
  val maxPass = 100

  lazy val xMin: Int = image.xMin + margin
  lazy val xMax: Int = image.xMax - margin
  lazy val yMin: Int = image.yMin + margin
  lazy val yMax: Int = image.yMax - margin
  lazy val width: Int = image.width
  lazy val height: Int = image.height

  var inputPixels: Array[Byte] = image.data
  //  val outputImage: BufferImage[Byte] = image.copy()
  var outputPixels: Array[Byte] = image.data.clone()

  val OUTLINE: Int = 0

  /**
   * pixelType: int BYTE=0, SHORT=1, FLOAT=2, RGB=3
   */
  def process(pixelType: Int, count: Int): Unit = {
    var p1: Byte = 0
    var p2: Byte = 0
    var p3: Byte = 0
    var p4: Byte = 0
    var p5: Byte = 0
    var p6: Byte = 0
    var p7: Byte = 0
    var p8: Byte = 0
    var p9: Byte = 0
    val bgColor: Byte = if (inverted)
      0
    else
      -1 //255

    //    val inputPixels = image.data.clone
    var offset: Int = 0
    var v: Byte = 0
    var sum: Int = 0
    var rowOffset: Int = image.stride
    cfor(yMin)(_ <= yMax, _ + 1) { y =>
      offset = xMin + y * width
      p2 = inputPixels(offset - rowOffset - 1)
      p3 = inputPixels(offset - rowOffset)
      p5 = inputPixels(offset - 1)
      p6 = inputPixels(offset)
      p8 = inputPixels(offset + rowOffset - 1)
      p9 = inputPixels(offset + rowOffset)

      cfor(image.xMin)(_ <= xMax, _ + 1) { x =>
        p1 = p2
        p2 = p3
        p3 = inputPixels(offset - rowOffset + 1)
        p4 = p5
        p5 = p6
        p6 = inputPixels(offset + 1)
        p7 = p8
        p8 = p9
        p9 = inputPixels(offset + rowOffset + 1)

        pixelType match {
          case OUTLINE => {
            v = p5
            if (v != bgColor) {
              if (!(p1 == bgColor || p2 == bgColor || p3 == bgColor || p4 == bgColor
                || p6 == bgColor || p7 == bgColor || p8 == bgColor || p9 == bgColor))
                v = bgColor
            }
          }
          case _ => {}
        }
        outputPixels(offset) = v
        offset += 1
      }
    }
    inputPixels = outputPixels.clone()
  }

  val table: Array[Int] = Array(
    0, 0, 0, 0, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 3, 0, 3, 3,
    0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0,
    0, 0, 3, 1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
    3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 3, 1, 3, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 1, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0)

  val table2: Array[Int] = Array(
    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 2, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  /**
   * Uses a lookup table to repeatably removes outputPixels from the
   * edges of objects in a binary image, reducing them to single
   * pixel wide skeletons. There is an entry in the table for each
   * of the 256 possible 3x3 neighborhood configurations. An entry
   * of '1' means delete pixel on first pass, '2' means delete pixel on
   * second pass, and '3' means delete on either pass. Pixels are
   * removed from the right and bottom edges of objects on the first
   * pass and from the left and top edges on the second pass. A
   * graphical representation of the 256 neighborhoods indexed by
   * the table is available at
   * "http://imagej.nih.gov/ij/images/skeletonize-table.gif".
   */
  def skeletonize(): Unit = {
    var pass: Int = 0
    var pixelsRemoved: Int = 0
    do {
      pixelsRemoved = thin(pass, table)
      pass += 1
      pixelsRemoved += thin(pass, table)
      pass += 1
      if (verboseLogging)
        println(s"table: pass: $pass, pixelsRemoved: $pixelsRemoved")
    } while (pixelsRemoved > 0 && pass <= maxPass)
    do { // use a second table to remove "stuck" outputPixels
      pixelsRemoved = thin(pass, table2)
      pass += 1
      pixelsRemoved += thin(pass, table2)
      pass += 1
      if (verboseLogging)
        println(s"table2: pass: $pass, pixelsRemoved: $pixelsRemoved")
    } while (pixelsRemoved > 0 && pass <= maxPass)
  }

  /**
   * The active area need to be 1 smaller than the whole image
   * This can fill the missing.
   * First line drawing has to be implemented
   */
  def drawEdge(): Unit = {
    //    resetRoi()
    //    setColor(Color.white)
    //    moveTo(0, 0); lineTo(0, height - 1);
    //    moveTo(0, 0); lineTo(width - 1, 0);
    //    moveTo(width - 1, 0); lineTo(width - 1, height - 1);
    //    moveTo(0, height - 1); lineTo(width /*-1*/ , height - 1);
  }

  def thin(pass: Int, table: Array[Int]): Int = {
    var p1: Byte = 0
    var p2: Byte = 0
    var p3: Byte = 0
    var p4: Byte = 0
    var p5: Byte = 0
    var p6: Byte = 0
    var p7: Byte = 0
    var p8: Byte = 0
    var p9: Byte = 0
    val bgColor: Byte = if (inverted)
      0
    else
      -1 //255

    var v: Byte = 0
    var index: Int = 0
    var code: Int = 0
    var offset: Int = 0
    var rowOffset = image.width
    var pixelsRemoved: Int = 0
    var count: Int = 100
    cfor(yMin)(_ <= yMax, _ + 1) { y =>
      offset = xMin + y * width
      cfor(xMin)(_ <= xMax, _ + 1) { x =>
        p5 = inputPixels(offset)
        v = p5
        if (v != bgColor) {
          p1 = inputPixels(offset - rowOffset - 1)
          p2 = inputPixels(offset - rowOffset)
          p3 = inputPixels(offset - rowOffset + 1)
          p4 = inputPixels(offset - 1)
          p6 = inputPixels(offset + 1)
          p7 = inputPixels(offset + rowOffset - 1)
          p8 = inputPixels(offset + rowOffset)
          p9 = inputPixels(offset + rowOffset + 1)
          index = 0
          if (p1 != bgColor) index |= 1
          if (p2 != bgColor) index |= 2
          if (p3 != bgColor) index |= 4
          if (p6 != bgColor) index |= 8
          if (p9 != bgColor) index |= 16
          if (p8 != bgColor) index |= 32
          if (p7 != bgColor) index |= 64
          if (p4 != bgColor) index |= 128
          code = table(index)
          if ((pass & 1) == 1) { //odd pass
            if (code == 2 || code == 3) {
              v = bgColor
              pixelsRemoved += 1
            }
          } else { //even pass
            if (code == 1 || code == 3) {
              v = bgColor
              pixelsRemoved += 1
            }
          }
        }
        outputPixels(offset) = v
        offset += 1
      }
    }
    inputPixels = outputPixels.clone()
    return pixelsRemoved
  }

  /**
   * Outline
   */
  def outline(): BufferImage[Byte] = {
    process(OUTLINE, 0)
    BufferImage.copyWithNewBuffer(image, outputPixels)
  }

  /**
   * Skeletonize
   */
  def calc(): BufferImage[Byte] = {
    //    process(1, 0)
    if (findOutline)
      outline()
    else
      skeletonize()
    BufferImage.copyWithNewBuffer(image, outputPixels)
  }

  lazy val result = calc()
}

object Skeletonize {

  // ================ Outline ================

  def outline(image: BufferImage[Byte]): BufferImage[Byte] = {
    if (image.numBands != 1) {
      println(s"Can only handle images with 1 channel run treshold first")
      return image
    }
    val skeletonize = new Skeletonize(
      image,
      inverted = false,
      findOutline = true)
    skeletonize.result
  }

  // ================ Skeletonize ================

  def transform(image: BufferImage[Byte]): BufferImage[Byte] = {
    if (image.numBands != 1) {
      println(s"Can only handle images with 1 channel run treshold first")
      return image
    }
    val skeletonize = new Skeletonize(
      image,
      inverted = false)
    skeletonize.calc()
  }

  def transformWhiteBackground(image: BufferImage[Byte]): BufferImage[Byte] = {
    if (image.numBands != 1) {
      println(s"Can only handle images with 1 channel run treshold first")
      return image
    }
    val skeletonize = new Skeletonize(
      image,
      inverted = true)
    skeletonize.result
  }
}