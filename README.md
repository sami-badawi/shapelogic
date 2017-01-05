![Logo](https://github.com/sami-badawi/shapelogic/blob/master/docs/image/shapelogicsmallgradient.png)

# Shapelogic Scala #

Shapelogic Scala is a simple generic image processing / computer vision library in Scala. 

The main goal is to make it simple to write generic image processing / computer vision code in Scala. While shielding the user from the advanced type machinery that is needed to makes this possible. 
See [Spire](https://github.com/non/spire) for example of generic code.

A central idea is to have only one generic image class [BufferImage](https://github.com/sami-badawi/shapelogic-scala/blob/master/src/main/scala/org/shapelogic/sc/image/BufferImage.scala) and a [few traits and helpers](https://github.com/sami-badawi/shapelogic-scala/wiki/Image-Classes-and-Traits).

## Background ##

Doing image processing in Java is harder than it should be.
Java Abstract Window Toolkit (AWT) have had image functionality since Java 1.0.
This feels dated and has many problems:
* Java does not have the unsigned integer that are prevalent in image processing.
* AWT was made for the purpose of making GUIs and 2D graphics.
* AWT has many layers of encapsulation and a lot of dependencies.
* It feels clumsy and dated.

[BoofCV](http://boofcv.org) and [ImageJ](https://imagej.nih.gov/ij/features.html)
are a good new image processing libraries for Java, but they don't use Scala's advanced language features.

## Goals ##

* Make minimal uniform classes for images in ideomatic Scala
* Use Scala's advanced type system to make image processing algorithms uniform
* Make loaders and savers for images
* Make system for combining image operations
* Port some algorithms from Shapelogic Java 
  * Vectorization 
  * Feature extraction
* Combine with machine learning to do some image classification

## Shapelogic History ##

[Shapelogic Java](http://shapelogic.org) was started in 2007 as a Java image processing library.
The primary purpose was add functional programming techniques to Java.
Functional programming ideas have made it into Java 8 and Scala.
Shapelogic Java is now bit rotted. 

Shapelogic Scala was started in 2016. 

## Status ##

* Version 0.1.1
* The api is not stable yet
* Pre alpha

## Features finished ##

* Image classes created
* Image loaders
* First command line script for image inspection
* Image writers
* [Image operations](https://github.com/sami-badawi/shapelogic-scala/wiki/Image-Operations)

## Planned features ##

* Better image load and save
* Better image operations
* Image operation pipelines
* Vectorize skeletonized lines (Shapelogic Java port)
* Compine vectorized lines (Shapelogic Java port)
* Use machine learning for classification

## Image IO and Dependencies ##

The goal is to keep library dependencies for Shapelogic low.
Currently the images loaders are using javax.imageio. They are only part of Oracle JDK not on OpenJDK.

* Stardard Git and SBT Scala project
* Currently no configuration
* Dependencies on Spire, Simulacrum, javax.imageio
* No database is used
* Currently no GUI all command line

There is a branch attempting to rewritten image IO using [imglib2](https://github.com/imglib/imglib2) 
the base library of [ImageJ2](https://github.com/imagej/imagej).
This could open the door for better integration with ImageJ.

Another option is using Apache commons-imaging.

## Getting Started ##

```
clone git https://github.com/sami-badawi/shapelogic-scala.git
cd shapelogic-scala
sbt compile
sbt test
sbt 'run-main org.shapelogic.sc.script.ColorExtractor -i "image/rgbbmwpng.png" -x 2 -y 0'
```

Example of running command line script:
```
Threshold:
sbt 'run-main org.shapelogic.sc.script.Threshold -i "image/3black_dots.png" -t 128 -o "image/out.png"'

ColorExtractor:
sbt 'run-main org.shapelogic.sc.script.ColorExtractor -i "image/rgbbmwpng.png" -x 2 -y 0'
```
ColorExtractor will just extract the pixel value at x y coordinates. Output:

```
alpha: 255, blue: 255, green: 38, red: 0
```

### Who do I talk to? ###

* Repo owner: [Sami Badawi](http://blog.samibadawi.com/) / [@Sami_Badawi](https://twitter.com/Sami_Badawi)
