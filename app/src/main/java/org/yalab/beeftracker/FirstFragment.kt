package org.yalab.beeftracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import org.yalab.beeftracker.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private lateinit var foodLabel : FoodLabel
    var step = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodLabel = FoodLabel(context as Context)
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val image2 = (context as Context).assets.open("image.jpg")
        var bmp = BitmapFactory.decodeStream(image2)
        binding.imageView.setImageBitmap(bmp)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            val context = context as Context
            val image = context.assets.open("image.png")
            binding.textviewFirst.text = foodLabel.recognize(image)

            OpenCVLoader.initDebug();
            var src = Mat()
            val image2 = context.assets.open("image.jpg")
            Utils.bitmapToMat(BitmapFactory.decodeStream(image2), src)
//            val dst = Mat.zeros(Size(src.width().toDouble(), src.height().toDouble()), CvType.CV_8UC3)
            val dst = Mat()
            src.copyTo(dst)
            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGBA2RGB);
//            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY)

            val net = Dnn.readNetFromTensorflow(FoodLabel.PB_FILE)
            val scoreThresh = 0.5f
            val nmsThresh = 0.4f
            val siz = Size(224.0, 224.0)
            val w = (siz.width / 4).toInt() // width of the output geometry  / score maps
            val h = (siz.height / 4).toInt() // height of those. the geometry has 4, vertically stacked maps, the score one 1

            val blob =
                Dnn.blobFromImage(dst, 1.0, siz, Scalar(123.68, 116.78, 103.94), true, false)
            net.setInput(blob)

            val outs: List<Mat> = ArrayList()
            val outNames: MutableList<String> = ArrayList()
            outNames.add("feature_fusion/Conv_7/Sigmoid")
            outNames.add("feature_fusion/concat_3")
            net.forward(outs, outNames)

            // Decode predicted bounding boxes.
            // Decode predicted bounding boxes.
            val scores = outs[0].reshape(1, h)
            // My lord and savior : http://answers.opencv.org/question/175676/javaandroid-access-4-dim-mat-planes/
            // My lord and savior : http://answers.opencv.org/question/175676/javaandroid-access-4-dim-mat-planes/
            val geometry = outs[1].reshape(1, 5 * h) // don't hardcode it !

            val (boxesList, confidencesList) = decode(scores, geometry, scoreThresh)

            // Apply non-maximum suppression procedure.

            // Apply non-maximum suppression procedure.
            val confidences = MatOfFloat(Converters.vector_float_to_Mat(confidencesList))
            val boxesArray = boxesList.toTypedArray()
            val boxes = MatOfRotatedRect(*boxesArray)
            val indices = MatOfInt()
            Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices)

            // Render detections

            // Render detections
            val ratio = Point(dst.cols().toFloat() / siz.width, dst.rows().toFloat() / siz.height)
            val indexes = indices.toArray()
            for (i in indexes.indices) {
                val rot = boxesArray[indexes[i]]
                val vertices: Array<Point?> = arrayOfNulls<Point>(4)
                rot.points(vertices)
                for (j in 0..3) {
                    vertices[j]!!.x *= ratio.x
                    vertices[j]!!.y *= ratio.y
                }
                for (j in 0..3) {
                    Imgproc.line(dst, vertices[j], vertices[(j + 1) % 4], Scalar(0.0, 255.0, 0.0), 10)
                }
            }

            var output = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(dst, output)
            binding.imageView.setImageBitmap(output)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun decode(scores: Mat, geometry: Mat, scoreThresh: Float): Pair<List<RotatedRect>, List<Float>> {
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