package org.shapelogic.sc.operation

import org.shapelogic.sc.image.BufferImage
import org.shapelogic.sc.image.HasBufferImage
import org.shapelogic.sc.pixel.PixelOperation
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import org.shapelogic.sc.pixel.PixelHandler1ByteResult
import org.shapelogic.sc.pixel.PixelHandler1ByteResult

/**
 * This idea is that you can run over an image
 *
 * Many input channels one output channel possibly an alpha output channel
 * Input and output type are the same
 * BaseOperationToByte has no knowledge of the internals of the numbers
 * It is just a runner
 * If it was not for demands by BufferImage all it needed was
 * context bounds for:
 * ClassTag and the transform: T => T parameter
 *
 * Example of use: edge detector resulting in one band
 */
class BaseOperationByteResult[ //
@specialized(Byte, Short, Int, Long, Float, Double) T: ClassTag, //
@specialized(Byte, Short, Int, Long, Float, Double) C: ClassTag //
](
    inputImage: BufferImage[T])(
        pixelHandler: PixelHandler1ByteResult.Aux[T, C] //
        ) extends HasBufferImage[Byte] {

  lazy val pixelOperation: PixelOperation[T] = new PixelOperation[T](inputImage)

  val outputImage: BufferImage[Byte] = makeOutputImage()
  lazy val outBuffer = outputImage.data
  lazy val rgbOffsets = inputImage.getRGBOffsetsDefaults
  lazy val alphaChannel = if (rgbOffsets.hasAlpha) rgbOffsets.alpha else -1
  val verboseLogging = false

  def handleIndex(index: Int, indexOut: Int): Unit = {
    try {
      outBuffer(indexOut) = pixelHandler.calc(index)
    } catch {
      case ex: Throwable => print(",")
    }
  }

  def makeOutputImage(): BufferImage[Byte] = {
    BufferImage[Byte](
      width = inputImage.width,
      height = inputImage.height,
      numBands = 1,
      bufferInput = new Array[Byte](inputImage.pixelCount),
      rgbOffsetsOpt = None)
  }

  /**
   * Run over input and output
   * Should I do by line?
   */
  def calc(): BufferImage[Byte] = {
    val pointCount = inputImage.width * inputImage.height
    pixelOperation.reset()
    var count = 0
    var indexOut: Int = -1
    var index: Int = pixelOperation.index
    while (pixelOperation.hasNext) {
      index = pixelOperation.next()
      indexOut += 1
      handleIndex(index, indexOut)
    }
    if (verboseLogging)
      println(s"count: $indexOut, index: $index")
    outputImage
  }

  lazy val result: BufferImage[Byte] = calc()
}