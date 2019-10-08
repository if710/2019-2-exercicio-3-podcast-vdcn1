package br.ufpe.cin.android.podcast

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.DownloadService
import kotlinx.android.synthetic.main.itemlista.view.*
import android.Manifest
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class CustomAdapter (private val Episodes : ArrayList<ItemFeed>, private val c : Context, private val isBound : Boolean, private val musicPlayerService: MusicPlayerService?) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    private var TAG : String = "AdapterLog"
    private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val db = EpisodesDB.getDatabase(c.applicationContext)




    override fun getItemCount() =
        Episodes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(c).inflate(R.layout.itemlista,parent,false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val episode = Episodes[position]



        //Log.d("SendingPath", "CurrentPath is " + episode.path)


        holder.title?.text = episode.title
        holder.date?.text = episode.pubDate
        var play_pause : Boolean = true
        // se o episodio já foi baixado, sem botao de download e com botao de play
        if(episode.path != ""){
            holder.play.isEnabled = true
            holder.button.isEnabled=false
        }

        //botao de download
        holder.button.setOnClickListener { view: View? ->
            holder.button.isEnabled = false
            Log.d("Botao", "Botao apertado")
            val downloadService = Intent(c, DownloadService::class.java)
            downloadService.data = Uri.parse(episode.link)
            downloadService.putExtra("titulo", episode.title)
            c.startService(downloadService)

        }
        //click listener do play
        holder.play.setOnClickListener { view: View? ->
            Log.d("OnPlay", "Current position " + position)
            Log.d("CustomAdapter", "duration do episodio " + episode.duration)
            Log.d("CustomAdapter", "" + play_pause)

            //evitar mesmo audio de ser tocado
            if (musicPlayerService != null) {
                if(episode.duration == 0 && play_pause){
                    musicPlayerService.playFromFile(episode.path,episode.duration)
                }
            }
            //alternar entre pausar e dar play
            if(play_pause) {
                if (isBound) {
                    if (musicPlayerService != null) {
                        Log.d("CustomAdapter", "" + musicPlayerService)
                        Log.d("CustomAdapter", "" + isBound)
                        musicPlayerService.playMusic(episode.duration)
                        play_pause = !play_pause
                    }
                }
            }
            else{
                if (isBound) {
                    if (musicPlayerService != null) {
                        Log.d("CustomAdapter", "" + musicPlayerService)
                        Log.d("CustomAdapter", "" + isBound)
                        musicPlayerService.pauseMusic()
                        episode.duration = musicPlayerService.duration()
                        //pego a duração do podcast e coloco no bd
                        doAsync {
                            val db = EpisodesDB.getDatabase(c.applicationContext)
                            val _episodes = db.episodesDAO().buscaEpisodiopelotitulo(episode.title)
                            for(episode in _episodes){
                                db.episodesDAO().atualizarEpisodios(episode)
                            }
                        }
                        play_pause = !play_pause
                    }
                }
            }
        }
    }



    inner class MyViewHolder(v : View) : RecyclerView.ViewHolder(v), View.OnClickListener{
        val title = v.item_title
        val date = v.item_date
        val button = v.item_action
        val play = v.item_play

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            val position = adapterPosition
            val episode = Episodes[position]
            val intent = Intent(c,EpisodeDetailActivity::class.java)

            intent.putExtra("description",episode.description)
            intent.putExtra("link",episode.link)
            c.startActivity(intent)
        }
    }




}