package geotrellis.raster.op.global

import geotrellis._
import geotrellis.source._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

import geotrellis.testkit._
import geotrellis.raster.BitConstant

/**
 * Created by jchien on 4/24/14.
 */
class ViewshedSpec extends FunSpec
                            with ShouldMatchers
                            with TestServer
                            with RasterBuilders {
  describe("Viewshed") {
    it("computes the viewshed of a flat int plane") {
      val r = createRaster(Array.fill(7*8)(1), 7, 8)
      assertEqual(Raster(BitConstant(true, 7, 8), r.rasterExtent), Viewshed.computeViewable(4, 3, r))
    }

    it("computes the viewshed of a flat double plane") {
      val r = createRaster(Array.fill(7*8)(1.5), 7, 8)
      assertEqual(Raster(BitConstant(true, 7, 8), r.rasterExtent), Viewshed.computeViewable(4, 3, r))
    }

    it("computes the viewshed of a double line") {
      val rasterData = Array (
        300.0, 1.0, 99.0, 0.0, 10.0, 200.0, 137.0
      )
      val viewable = Array (
        1, 0, 1, 1, 1, 1, 0
      )
      val r = createRaster(rasterData, 7, 1)
      val viewRaster = createRaster(viewable, 7, 1)
      assertEqual(viewRaster, Viewshed.computeViewable(3, 0, r))
    }

    it("computes the viewshed of a double plane") {
      val rasterData = Array (
        999.0, 1.0,   1.0,   1.0,   1.0,   1.0,   999.0,
        1.0,   1.0,   1.0,   1.0,   1.0,   499.0, 1.0,
        1.0,   1.0,   1.0,   1.0,   99.0,  1.0,   1.0,
        1.0,   1.0,   999.0, 1.0,   1.0,   1.0,   1.0,
        1.0,   1.0,   1.0,   1.0,   100.0, 1.0,   1.0,
        1.0,   1.0,   1.0,   1.0,   1.0,   101.0, 1.0,
        1.0,   1.0,   1.0,   1.0,   1.0,   1.0,   102.0
      )
      val viewable = Array (
          1,     1,     1,     1,     0,     0,     1,
          0,     1,     1,     1,     0,     1,     0,
          0,     0,     1,     1,     1,     0,     0,
          0,     0,     1,     1,     1,     1,     1,
          0,     0,     1,     1,     1,     0,     0,
          0,     1,     1,     1,     0,     0,     0,
          1,     1,     1,     1,     0,     0,     0
      )
      val r = createRaster(rasterData, 7, 7)
      val viewRaster = createRaster(viewable, 7, 7)
      assertEqual(viewRaster, Viewshed.computeViewable(3, 3, r))
    }

    it("computes the viewshed of a int plane") {
      val rasterData = Array (
        999, 1,   1,   1,   1,   499, 999,
        1,   1,   1,   1,   1,   499, 250,
        1,   1,   1,   1,   99,  1,   1,
        1,   999, 1,   1,   1,   1,   1,
        1,   1,   1,   1,   1,   1,   1,
        1,   1,   1,   0,   1,   1,   1,
        1,   1,   1,   1,   1,   1,   1
      )
      val viewable = Array (
        1,     1,     1,     1,     0,     1,     1,
        1,     1,     1,     1,     0,     1,     1,
        0,     1,     1,     1,     1,     0,     0,
        0,     1,     1,     1,     1,     1,     1,
        0,     1,     1,     1,     1,     1,     1,
        1,     1,     1,     0,     1,     1,     1,
        1,     1,     1,     1,     1,     1,     1
      )
      val r = createRaster(rasterData, 7, 7)
      val viewRaster = createRaster(viewable, 7, 7)
      assertEqual(viewRaster, Viewshed.computeViewable(3, 3, r))
    }

    it("ignores NoData values and indicates they're unviewable"){
      val rasterData = Array (
        300.0, 1.0, 99.0, 0.0, Double.NaN, 200.0, 137.0
      )
      val viewable = Array (
        1, 0, 1, 1, 0, 1, 0
      )
      val r = createRaster(rasterData, 7, 1)
      val viewRaster = createRaster(viewable, 7, 1)
      assertEqual(viewRaster, Viewshed.computeViewable(3, 0, r))
    }

    it("should match veiwshed generated by ArgGIS") {
      val elevation = RasterSource("viewshed-elevation").get
      val expected = RasterSource("viewshed-expected").get

      val (x,y) = (-93.63300872055451407, 30.54649743277299123) // create overload
      val (col, row) = elevation.rasterExtent.mapToGrid(x, y)
      val actual = Viewshed.computeViewable(col, row, elevation)

      def countDiff(a:Raster, b:Raster):Int = {
        var ans = 0
        for(col <- 0 until 256) {
          for(row <- 0 until 256) {
            if (a.get(col, row) != b.get(col,row)) ans += 1;
          }
        }
        ans
      }
//      System.out.println(col.toString + "," + row.toString)
      val diff = (countDiff(expected,actual) / (256*256).toDouble) * 100
      val allowable = 5.0
      System.out.println(s"${diff} / ${256*256} = ${diff / (256*256).toDouble}")
      withClue(s"Percent difference from ArgGIS raster is more than $allowable%:") {
        diff should be < allowable
      }

//      System.out.println("======")
//      System.out.println(elevation.asciiDraw())
//      System.out.println("======")
//      System.out.println(Viewshed.computeHeightRequired(row,col,elevation).asciiDraw())

//      new geotrellis.data.arg.ArgWriter(TypeFloat).write("/Users/jchien/Documents/viewshed-actual3.arg", actual, "viewshed-actual3")
//      assertEqual(Viewshed.computeViewable(row, col, elevation), expected)
    }
  }
}
