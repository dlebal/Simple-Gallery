package com.simplemobiletools.gallery.asynctasks

import android.content.Context
import android.os.AsyncTask
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_TAKEN
import com.simplemobiletools.gallery.extensions.config
import com.simplemobiletools.gallery.extensions.galleryDB
import com.simplemobiletools.gallery.extensions.getFavoritePaths
import com.simplemobiletools.gallery.helpers.FAVORITES
import com.simplemobiletools.gallery.helpers.MediaFetcher
import com.simplemobiletools.gallery.models.Medium
import java.util.*

class GetMediaAsynctask(val context: Context, val mPath: String, val isPickImage: Boolean = false, val isPickVideo: Boolean = false,
                        val showAll: Boolean, val callback: (media: ArrayList<Medium>) -> Unit) :
        AsyncTask<Void, Void, ArrayList<Medium>>() {
    private val mediaFetcher = MediaFetcher(context)

    override fun doInBackground(vararg params: Void): ArrayList<Medium> {
        val getProperDateTaken = context.config.getFileSorting(mPath) and SORT_BY_DATE_TAKEN != 0
        val favoritePaths = context.getFavoritePaths()
        return if (showAll) {
            val foldersToScan = mediaFetcher.getFoldersToScan()
            val media = ArrayList<Medium>()
            foldersToScan.forEach {
                val newMedia = mediaFetcher.getFilesFrom(it, isPickImage, isPickVideo, getProperDateTaken, favoritePaths)
                media.addAll(newMedia)
            }

            MediaFetcher(context).sortMedia(media, context.config.getFileSorting(""))
            media
        } else {
            if (mPath == FAVORITES) {
                context.galleryDB.MediumDao().getFavorites() as ArrayList<Medium>
            } else {
                mediaFetcher.getFilesFrom(mPath, isPickImage, isPickVideo, getProperDateTaken, favoritePaths)
            }
        }
    }

    override fun onPostExecute(media: ArrayList<Medium>) {
        super.onPostExecute(media)
        callback(media)
    }

    fun stopFetching() {
        mediaFetcher.shouldStop = true
        cancel(true)
    }
}
