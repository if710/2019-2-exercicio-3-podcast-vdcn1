package br.ufpe.cin.android.podcast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.Intent.getIntent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.InputStream

class MusicPlayerService : Service() {
    private val TAG = "MusicPlayerService"
    private var mPlayer: MediaPlayer? = null
    private val mStartID: Int = 0
    private var path : String? = ""
    private val mBinder = MusicBinder()

    override fun onCreate() {
        super.onCreate()
        // configurar media player
        /*Log.d("MusicPlayer", "Playing music from...  " + path)
        mPlayer = MediaPlayer.create(this, Uri.parse(path))

        //fica em loop
        mPlayer?.isLooping = true

        createChannel()
        // cria notificacao na area de notificacoes para usuario voltar p/ Activity
        val notificationIntent = Intent(applicationContext, MusicPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(
            applicationContext,"1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).setContentTitle("Music Service rodando")
            .setContentText("Clique para acessar o player!")
            .setContentIntent(pendingIntent).build()

        // inicia em estado foreground, para ter prioridade na memoria
        // evita que seja facilmente eliminado pelo sistema
        startForeground(NOTIFICATION_ID, notification)*/
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        path = intent.getStringExtra("path")
        Log.d("OnStartCommand", "on start called")
        //path
        mPlayer = MediaPlayer.create(this,Uri.fromFile(File(path!!)))

        //fica em loop
        mPlayer?.isLooping = true

        createChannel()
        // cria notificacao na area de notificacoes para usuario voltar p/ Activity
        val notificationIntent = Intent(this, MusicPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(
            applicationContext,"1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).setContentTitle("Music Service rodando")
            .setContentText("Clique para acessar o player!")
            .setContentIntent(pendingIntent).build()

        // inicia em estado foreground, para ter prioridade na memoria
        // evita que seja facilmente eliminado pelo sistema
        startForeground(NOTIFICATION_ID, notification)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }

    fun playMusic() {
        if (!mPlayer!!.isPlaying) {
            mPlayer?.start()
        }
    }

    fun pauseMusic() {
        if (mPlayer!!.isPlaying) {
            mPlayer?.pause()
        }
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicPlayerService
            get() = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel("1", "Canal de Notificacoes", NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.description = "Descricao"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
    companion object {
        private val NOTIFICATION_ID = 2
    }

}