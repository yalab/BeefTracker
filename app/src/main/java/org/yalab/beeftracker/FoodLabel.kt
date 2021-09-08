package org.yalab.beeftracker

import android.content.Context
import android.graphics.BitmapFactory
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.InputStream

class FoodLabel constructor(_context: Context) {
    val context: Context
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
    }
    fun detect(inputStream: InputStream) : String {
        val baseApi = TessBaseAPI()
        baseApi.init(context.filesDir.toString(), "eng")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        baseApi.setImage(bitmap)
        val recognizedText = baseApi.utF8Text as String
        baseApi.end()
        return recognizedText.replace(Regex("[\\s”]"),"").replace("I", "1", true)
    }
}
