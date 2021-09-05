package org.yalab.beeftracker

class FoodLabel {
    val TRAIND_DATA_PATH = "eng.traineddata"
    fun detect() : String {
        val path = this.javaClass.classLoader.getResource(TRAIND_DATA_PATH)
        return "1234"
    }
}
