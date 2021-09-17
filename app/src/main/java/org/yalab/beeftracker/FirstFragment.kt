package org.yalab.beeftracker

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import org.yalab.beeftracker.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    var step = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val image2 = (context as Context).assets.open("image.jpg")
        var bmp = BitmapFactory.decodeStream(image2)
        binding.imageView.setImageBitmap(bmp)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            renderCattleInfo()
        }
    }

    fun renderCattleInfo() = runBlocking {
        val context = context as Context
        val image2 = context.assets.open("image.jpg")
        val foodLabel = FoodLabel(context, image2)
        binding.imageView.setImageBitmap(foodLabel.bitmap)
        launch {
            val nlbc = NLBC()
            val deferred = async(Dispatchers.IO) {
                nlbc.fetch(foodLabel.beefTrackingNumber())
            }
            deferred.await()
            val cattle = nlbc.cattle
            binding.trackingNumber.text = cattle.trackingNumber
            binding.birthDay.text = cattle.birthDay
            binding.gender.text = cattle.gender
            binding.motherTrackingNumber.text = cattle.motherTrackingNumber
            binding.breed.text = cattle.breed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}