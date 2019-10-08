package br.ufpe.cin.android.podcast

import android.Manifest
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class DownloadService : IntentService("DownloadService") {



    override fun onCreate() {
        super.onCreate()

    }
    public override fun onHandleIntent(i: Intent?) {
        try {
            Log.d("Service", "Service Rodando")
            //checar se tem permissao... Android 6.0+

            val root = getExternalFilesDir(DIRECTORY_DOWNLOADS)
            if (root != null) {
                Log.d("DownloadService", "" + root.path.toString())
            }

            root?.mkdirs()
            val output = File(root, i!!.data!!.lastPathSegment)
            if (output.exists()) {
                output.delete()
            }

            val url = URL(i.data!!.toString())

            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                while (len >= 0) {
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)
                }
                out.flush()
            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            Log.d("DownloadService", "download completo" + output.absoluteFile.toString())
            val titulo = i.getStringExtra("titulo")
            doAsync {
                val db = EpisodesDB.getDatabase(applicationContext)
                val updepisodes = db.episodesDAO().buscaEpisodiopelotitulo(titulo!!)
                for(episodes in updepisodes){
                    episodes.path = output.absoluteFile.toString()
                    Log.d("DownloadService", "episodios que bateram com o titulo " + episodes.path)
                    db.episodesDAO().atualizarEpisodios(episodes)
                }
                uiThread { LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(DOWNLOAD_COMPLETE)) }
            }


        } catch (e2: IOException) {
            Log.e(javaClass.getName(), "Exception durante download", e2)
        }
        //onDestroy()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    companion object {
        val DOWNLOAD_COMPLETE = "br.ufpe.cin.podcast.action.DOWNLOAD_COMPLETE"
    }
}
