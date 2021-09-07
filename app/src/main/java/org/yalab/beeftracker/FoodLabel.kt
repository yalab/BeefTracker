package org.yalab.beeftracker

import android.content.Context
import android.graphics.BitmapFactory
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.InputStream

class FoodLabel {
    fun detect(context: Context, inputStream: InputStream) : String {

        val dir = File(context.filesDir, "tessdata")
//        if(!dir.exists()){
            dir.mkdir()
            val filename = "tessdata/eng.traineddata"
            val src = context.assets.open(filename)
            val dst = File(dir.toString() + "/eng.traineddata")
            dst.delete()
            dst.createNewFile()
            dst.outputStream().use {
                it.write(src.readBytes())
            }
//        }

        val baseApi = TessBaseAPI()
        baseApi.init(context.filesDir.toString(), "eng")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        baseApi.setImage(bitmap)
        val recognizedText = baseApi.utF8Text
        baseApi.end()
        return recognizedText
    }
}
