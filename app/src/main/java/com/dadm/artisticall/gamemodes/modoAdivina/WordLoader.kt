package com.dadm.artisticall.gamemodes.modoAdivina

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

fun loadWordsFromAssets(context: Context): List<String> {
    val assetManager = context.assets
    val inputStream = assetManager.open("words.json")

    val reader = InputStreamReader(inputStream)
    val words: List<String> = Gson().fromJson(reader, Array<String>::class.java).toList()

    return words
}

fun getRandomWord(words: List<String>): String {
    return words.random()
}
