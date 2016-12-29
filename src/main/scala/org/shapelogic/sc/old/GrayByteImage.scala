package org.shapelogic.sc.old

import org.shapelogic.sc.image.ImageBase
import org.shapelogic.sc.image._

@deprecated("BufferImage is more general, use that","2016-07-11")
 class GrayByteImage(
    val width: Int,
    val height: Int,
    bufferInput: Array[Byte]) extends ImageBase[Byte] {
  def frozen: Boolean = false

  def numBands: Int = 1

  lazy val bufferLenght = height * width

  val data: Array[Byte] = if (bufferInput == null) new Array[Byte](bufferLenght) else bufferInput

  def getIndex(x: Int, y: Int): Int = {
    width * y + x
  }

  def getChannel(x: Int, y: Int, ch: Int): Byte = {
    data(width * y + x)
  }

  def getPixel(x: Int, y: Int): Array[Byte] = {
    Array[Byte](getChannel(x, y, ch = 0))
  }

  def setChannel(x: Int, y: Int, ch: Int, value: Byte): Unit = {
    if (!frozen)
      data(width * y + x) = value
  }

  def setPixel(x: Int, y: Int, value: Array[Byte]): Unit = {
    if (!frozen)
      data(width * y + x) = value(0)
  }

  def fill(value: Byte): Unit = {
    var i = 0
    while (i < bufferLenght) {
      data(i) = value
      i += 1
    }
  }

  def rgbOffsetsOpt: Option[RGBOffsets] = Some(grayRGBOffsets)

  def isInBounds(x: Int, y: Int): Boolean = {
    0 <= x && x < width && 0 <= y && y < height
  }
}