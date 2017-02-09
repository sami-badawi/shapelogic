package org.shapelogic.sc.io

import java.awt._
import java.awt.event._
import java.awt.image._
import java.io._
import javax.imageio._
import scala.util.Try
import org.shapelogic.sc.image.WrappedRGBIntBufferedImage
import java.awt.image.Raster
import java.awt.image.DataBufferByte
import org.shapelogic.sc.image.ReadImage
import org.shapelogic.sc.image.BufferImage
import org.shapelogic.sc.image._
import java.io.File
import scala.util.Success
import scala.util.Failure

/**
 * Loading of images via AWT BufferedImage
 * Current version is using javax.imageio
 * This creates a dependency on Oracle JDK and will not run with OpenJDK
 * This will probably be replaced
 */
object LoadImage {

  def loadAWTBufferedImage(filename: String): Try[BufferedImage] = {
    println(s"loadAWTBufferedImage for $filename")
    Try {
      val img: BufferedImage = ImageIO.read(new File(filename))
      val colorModel = img.getColorModel
      println(s"colorModel: $colorModel")
      val height = img.getHeight
      println(s"filename: $filename had height: $height")
      img
    }
  }

  def loadBufferImage(filename: String): Option[BufferImage[Byte]] = {
    try {
      val imageTry = loadAWTBufferedImage(filename)
      imageTry match {
        case Success(image) => BufferedImageConverter.awtBufferedImage2BufferImage(image)
        case Failure(ex) => { throw ex }
      }
    } catch {
      case ex: Throwable => {
        println(s"loadBufferImage($filename) error: ${ex.getMessage}")
        ex.printStackTrace()
        None
      }
    }
  }

  def saveAWTBufferedImage(image: BufferedImage, format: String, filename: String): Boolean = {
    println(s"saveAWTBufferedImage to $filename")
    try {
      val outputfile = new File(filename)
      ImageIO.write(image, format, outputfile)
      true
    } catch {
      case ex: Throwable => {
        false
      }
    }
  }
}