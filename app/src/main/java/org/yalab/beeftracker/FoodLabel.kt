package org.yalab.beeftracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.InputStream

class FoodLabel constructor(_context: Context) {
    val context: Context
    companion object {
        lateinit var PB_FILE: String
    }
    init{
        context = _context
        val filename = "tessdata/eng.traineddata"
        val dataFile = File(context.filesDir, filename)
        val dir = dataFile.parentFile
        if(!dir.exists()) { dir.mkdir() }
        if(!dataFile.exists()){
            val src = context.assets.open(filename)
            dataFile.createNewFile()
            dataFile.outputStream().use { it.write(src.readBytes()) }
        }
        // https://www.dropbox.com/s/r2ingd0l3zt8hxs/frozen_east_text_detection.tar.gz?dl=1
        val pbFileName = "frozen_east_text_detection.pb"
        val pbFile = File(context.filesDir, pbFileName)
        PB_FILE = pbFile.path
        if(!pbFile.exists()) {
            val src = context.assets.open(pbFileName)
            pbFile.createNewFile()
            pbFile.outputStream().use { it.write(src.readBytes()) }
        }
    }

    fun recognize(inputStream: InputStream) : String {
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return recognize(bitmap)
    }

    fun recognize(bitmap: Bitmap) : String {
        val baseApi = TessBaseAPI()
        baseApi.init(context.filesDir.toString(), "eng")
        baseApi.setImage(bitmap)
        val recognizedText = baseApi.utF8Text as String
        baseApi.end()
        return recognizedText.replace(Regex("[\\s”]"),"").replace("I", "1", true)
    }
}
