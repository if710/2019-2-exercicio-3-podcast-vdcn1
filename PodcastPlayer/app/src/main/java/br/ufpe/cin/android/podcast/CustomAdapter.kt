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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.jetbrains.anko.doAsync


class CustomAdapter (private val Episodes : ArrayList<ItemFeed>, private val c : Context) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    private var TAG : String = "AdapterLog"
    private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun getItemCount() =
        Episodes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(c).inflate(R.layout.itemlista,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val episode = Episodes[position]
        holder.title?.text = episode.title
        holder.date?.text = episode.pubDate
        holder.play.isEnabled = true
        doAsync {
            val db = EpisodesDB.getDatabase(c.applicationContext)
            val listEpisode = db.episodesDAO().buscaEpisodiopelotitulo(episode.title)
            Log.d("CustomAdapter", "path do episodio" + episode.path)
            for(episodes in listEpisode){
                if(episodes.path != ""){
                    episode.path = episodes.path
                    Log.d("CustomAdapter", "path da query no adapter" + episode.path)
                    holder.play.isEnabled = true
                }
            }
        }

        holder.button.setOnClickListener { view: View? ->
            holder.button.isEnabled = false
            Log.d("Botao","Botao apertado")
            val downloadService = Intent(c, DownloadService::class.java)
            downloadService.data = Uri.parse(episode.link)
            downloadService.putExtra("titulo", episode.title)
            c.startService(downloadService)
        }

        holder.play.setOnClickListener { view: View? ->
            val playerService = Intent(c, MusicPlayerService::class.java)
            Log.d("SendingPath", "CurrentPath is " + episode.path)
            playerService.putExtra("path",episode.path)
            c.startService(playerService)
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