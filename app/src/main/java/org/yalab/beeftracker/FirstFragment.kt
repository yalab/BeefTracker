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
import org.opencv.imgproc.Imgproc
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
            binding.textviewFirst.text = foodLabel.detect(image)

            OpenCVLoader.initDebug();
            var src = Mat()
            val image2 = context.assets.open("image.jpg")
            Utils.bitmapToMat(BitmapFactory.decodeStream(image2), src)
            val dst = Mat.zeros(Size(src.width().toDouble(), src.height().toDouble()), CvType.CV_8UC3)
            src.copyTo(dst)
            var label = "next"
            if(step >= 0) { Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY); label = "gray scale" }
            if(step >= 1) { Imgproc.GaussianBlur(dst, dst, Size(5.0, 5.0), 5.0); label = "grassian" }
            if(step >= 2) { Imgproc.Sobel(dst, dst, -1, 1, 0); label = "sobel" }
            if(step >= 3) { Imgproc.threshold(dst, dst, 100.0, 255.0, Imgproc.THRESH_BINARY); label = "threshhold"}
            if(step >= 4) { Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, Mat(3, 3, CvType.CV_8UC1), Point(-1.0, -1.0), 3); label = "morphology"}
            if(step >= 5) {
                val hierarchy = Mat()
                val contours = ArrayList<MatOfPoint>()
                Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
                src.copyTo(dst)
                val green = Scalar(0.0, 255.0, 0.0)
                val red = Scalar(255.0, 0.0, 0.0)
                contours.forEach({ ptmat ->
                    val ptmat2 = MatOfPoint2f(*ptmat.toArray())
                    val bbox = Imgproc.minAreaRect(ptmat2)
                    val box = bbox.boundingRect()
                    Imgproc.rectangle(dst, box.tl(), box.br(), green, 10)
                })
                Imgproc.drawContours(dst, contours, -1, red, 10)
                label = "contours"
            }
            if(step >= 6) { src.copyTo(dst); step = -1; label = "next" }
            step ++
            binding.buttonFirst.text = label
            var output = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(dst, output)
            binding.imageView.setImageBitmap(output)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}