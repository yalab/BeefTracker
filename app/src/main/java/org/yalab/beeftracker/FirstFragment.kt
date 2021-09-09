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
import org.opencv.imgproc.Imgproc.circle
import org.yalab.beeftracker.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private lateinit var foodLabel : FoodLabel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodLabel = FoodLabel(context as Context)
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            val context = context as Context
            val image = context.assets.open("image.png")
            binding.textviewFirst.text = foodLabel.detect(image)

            val image2 = context.assets.open("image.jpg")
            var bmp = BitmapFactory.decodeStream(image2)
            OpenCVLoader.initDebug();
            var src = Mat()
            Utils.bitmapToMat(bmp, src)

            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY)
            val hierarchy = Mat.zeros(Size(5.0, 5.0), CvType.CV_8UC1)
            val invsrc = src.clone()
            var contours = ArrayList<MatOfPoint>()
            Imgproc.findContours(invsrc, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1)
            val dst=Mat.zeros(Size(src.width().toDouble(), src.height().toDouble()), CvType.CV_8UC3)
            var color = Scalar(255.0, 255.0, 255.0)
            Imgproc.drawContours(dst, contours, -1, color,1)

            val j = contours.size - 1
            for(i in 0..j) {
                val ptmat= contours.get(i)
                color = Scalar(255.0, 0.0, 0.0)
                val ptmat2 : MatOfPoint2f = MatOfPoint2f(*ptmat.toArray())
                val bbox = Imgproc.minAreaRect(ptmat2)
                val box = bbox.boundingRect()
                Imgproc.circle(dst, bbox.center, 5, color, -1)
                color = Scalar(0.0, 255.0, 0.0)
                Imgproc.rectangle(dst, box.tl(), box.br(), color, 2)
            }


            var output = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(dst, output)
//            Utils.matToBitmap(src, output)
            binding.imageView.setImageBitmap(output)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}