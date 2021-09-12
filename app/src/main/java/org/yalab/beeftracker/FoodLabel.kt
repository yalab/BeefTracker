package org.yalab.beeftracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import java.io.File
import java.io.InputStream
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters

class FoodLabel constructor(_context: Context, inputStream: InputStream) {
    val bitmap: Bitmap
    val texts: List<String>

    private val context: Context
    private val dnnNet: Net
    private val baseApi: TessBaseAPI
    init{
        context = _context
        val filename = "tessdata/eng.traineddata"
        val dataFile = File(context.filesDir, filename)
        val dir = dataFile.parentFile!!
        if(!dir.exists()) { dir.mkdir() }
        if(!dataFile.exists()){
            val src = context.assets.open(filename)
            dataFile.createNewFile()
            dataFile.outputStream().use { it.write(src.readBytes()) }
        }
        baseApi = TessBaseAPI()
        baseApi.init(context.filesDir.toString(), "eng")

        // https://www.dropbox.com/s/r2ingd0l3zt8hxs/frozen_east_text_detection.tar.gz?dl=1

        val pbFileName = "frozen_east_text_detection.pb"
        val pbFile = File(context.filesDir, pbFileName)
        if(!pbFile.exists()) {
            val src = context.assets.open(pbFileName)
            pbFile.createNewFile()
            pbFile.outputStream().use { it.write(src.readBytes()) }
        }
        OpenCVLoader.initDebug();
        dnnNet = Dnn.readNetFromTensorflow(pbFile.path)
        val mat = Mat()
        Utils.bitmapToMat(BitmapFactory.decodeStream(inputStream), mat)
        val rectangles = textRectangles(mat)
        val green = Scalar(0.0, 255.0, 0.0)
        rectangles.forEach({vertices ->
            for (j in 0..3) {
                Imgproc.line(mat, vertices[j], vertices[(j + 1) % 4], green, 10)
            }
        })
        texts = recognize(mat, rectangles)
        bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
    }

    protected fun finalize() {
        baseApi.end()
    }

    private fun recognize(mat: Mat, rectangles: List<Array<Point?>>): List<String> {
        val output = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, output)
        baseApi.setImage(output)
        val strings = ArrayList<String>(rectangles.size)
        rectangles.forEach({rectangle ->
            val origin = rectangle[1] as Point
            val againstVertix = rectangle[3] as Point
            val x = origin.x
            val y = origin.y
            val width = againstVertix.x - origin.x
            val height = againstVertix.y - origin.y
            baseApi.setRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())
            val recognizedText = baseApi.utF8Text as String
            strings.add(recognizedText)
        })
        return strings
    }

    private fun textRectangles(mat: Mat) : List<Array<Point?>> {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);

        val scoreThresh = 0.5f
        val nmsThresh = 0.4f
        val siz = Size(224.0, 224.0)
        val w = (siz.width / 4).toInt()
        val h = (siz.height / 4).toInt()

        val blob = Dnn.blobFromImage(mat, 1.0, siz, Scalar(123.68, 116.78, 103.94), true, false)
        dnnNet.setInput(blob)

        val outs: List<Mat> = ArrayList()
        val outNames: MutableList<String> = ArrayList(2)
        outNames.add("feature_fusion/Conv_7/Sigmoid")
        outNames.add("feature_fusion/concat_3")
        dnnNet.forward(outs, outNames)

        val scores = outs[0].reshape(1, h)
        val geometry = outs[1].reshape(1, 5 * h)

        val (boxesList, confidencesList) = decodeRectangles(scores, geometry, scoreThresh)

        val confidences = MatOfFloat(Converters.vector_float_to_Mat(confidencesList))
        val boxesArray = boxesList.toTypedArray()
        val boxes = MatOfRotatedRect(*boxesArray)
        val indices = MatOfInt()
        Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices)

        val ratio = Point(mat.cols().toFloat() / siz.width, mat.rows().toFloat() / siz.height)
        val indexes = indices.toArray()
        val rectangles = ArrayList<Array<Point?>>(indexes.size - 1)
        for (i in indexes.indices) {
            val rot = boxesArray[indexes[i]]
            val vertices: Array<Point?> = arrayOfNulls<Point>(4)
            rot.points(vertices)
            for (j in 0..3) {
                vertices[j]!!.x *= ratio.x
                vertices[j]!!.y *= ratio.y
            }
            rectangles.add(vertices)
        }
        return rectangles
    }

    private fun decodeRectangles(scores: Mat, geometry: Mat, scoreThresh: Float): Pair<List<RotatedRect>, List<Float>> {
        val confidences = ArrayList<Float>()
        // size of 1 geometry plane
        // size of 1 geometry plane
        val W = geometry.cols()
        val H = geometry.rows() / 5
        //System.out.println(geometry);
        //System.out.println(scores);

        //System.out.println(geometry);
        //System.out.println(scores);
        val detections: MutableList<RotatedRect> = ArrayList()
        for (y in 0 until H) {
            val scoresData = scores.row(y)
            val x0Data = geometry.submat(0, H, 0, W).row(y)
            val x1Data = geometry.submat(H, 2 * H, 0, W).row(y)
            val x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y)
            val x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y)
            val anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y)
            for (x in 0 until W) {
                val score = scoresData[0, x][0]
                if (score >= scoreThresh) {
                    val offsetX = x * 4.0
                    val offsetY = y * 4.0
                    val angle = anglesData[0, x][0]
                    val cosA = Math.cos(angle)
                    val sinA = Math.sin(angle)
                    val x0 = x0Data[0, x][0]
                    val x1 = x1Data[0, x][0]
                    val x2 = x2Data[0, x][0]
                    val x3 = x3Data[0, x][0]
                    val h = x0 + x2
                    val w = x1 + x3
                    val offset =
                        Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2)
                    val p1 = Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y)
                    val p3 = Point(
                        -1 * cosA * w + offset.x,
                        sinA * w + offset.y
                    ) // original trouble here !
                    val r = RotatedRect(
                        Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)),
                        Size(w, h),
                        -1 * angle * 180 / Math.PI
                    )
                    detections.add(r)
                    confidences += score.toFloat()
                }
            }
        }
        return detections to confidences
    }
}
