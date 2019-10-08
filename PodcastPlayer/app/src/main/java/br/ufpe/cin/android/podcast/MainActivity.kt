package br.ufpe.cin.android.podcast

import android.Manifest
import android.app.LauncherActivity
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.itemlista.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val ON_CREATE_REQUEST = 710
    var ListItems : ArrayList<ItemFeed>? = null
    private val TAG : String = "MainActivity"
    private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val STORAGE_REQUEST = ON_CREATE_REQUEST + 4
    internal var musicPlayerService: MusicPlayerService? = null
    internal var isBound = false
    //broadcast reciever que toda vez que termina o download atualiza o adapter
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            doAsync {
                val db = EpisodesDB.getDatabase(applicationContext)
                var episodeList = db.episodesDAO().todosEpisodios()
                Log.d("BroadcastReceiver","Lista dos episodios " + episodeList[0].path)
                uiThread { dbToAdapter(episodeList) }
            }
        }
    }
    private val intentFilter = IntentFilter(DownloadService.DOWNLOAD_COMPLETE)

    private fun dbToAdapter(episodeList : Array<Episode>) {
        ListItems?.clear()
        for(episode in episodeList){
            ListItems!!.add(ItemFeed(episode.title,episode.link,episode.date,episode.description,episode.downloadLink,episode.path,episode.duration))
        }
        my_recyclerview.layoutManager = LinearLayoutManager(this) //usando linearlayout
        my_recyclerview.adapter = CustomAdapter(ListItems!!, this@MainActivity, isBound, musicPlayerService)
        my_recyclerview.addItemDecoration(
            DividerItemDecoration(
                this@MainActivity,
                LinearLayoutManager.VERTICAL
            )
        )
        Log.d("dbToAdapter", "END")
    }
    //serviço com binding
    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            musicPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as MusicPlayerService.MusicBinder
            musicPlayerService = binder.service

            isBound = true

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val playerService = Intent(applicationContext, MusicPlayerService::class.java)
        startService(playerService)
        val myintent = intent
        //Log.d("MainActivityIntent" , "intent do id " + intent.getStringExtra("id"))
        //Lista de itemfeed's que guardam os episódios com as informações pedidas.
        ListItems = arrayListOf() //atribuindo isso pra caso ele não ser null
        my_recyclerview.layoutManager = LinearLayoutManager(this) //usando linearlayout
        //chamar a função pra dar parse sem usar a uiThread
        parseAsync()
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
        unbindService(sConn)
        super.onStop()
    }

    override fun onStart()  {
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver,intentFilter)
        super.onStart()
        if (!isBound) {
            Toast.makeText(applicationContext, "Fazendo o Binding...", Toast.LENGTH_SHORT).show()
            val bindIntent = Intent(this, MusicPlayerService::class.java)
            isBound = bindService(bindIntent,sConn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver,intentFilter)

    }

    fun parseAsync(){
        // string do link que ira se transformar em objeto URL pra conexão

        val link = "https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml"
        val url = URL(link)
        //pegar instancia do banco de dados
        val db = EpisodesDB.getDatabase(applicationContext)
        var episodes : Array<Episode> = arrayOf()
        doAsync{
            //caso eu tenha internet, busco os episódios pelo link
            if(isNetworkAvailable()){
                Log.d(TAG,"Parsing...")
                val cc = url.openConnection() //abrindo conexão
                cc.doInput = true

                //trâmites de pegar informações...
                var inputs : InputStream = cc.getInputStream()
                val isr = InputStreamReader(inputs)
                val reader = BufferedReader(isr)
                val respostaHTTP = StringBuilder()
                var line : String? = ""
                //caso a linha não seja null.... pegar cada linha e jogar pra uma string grande e aí assim jogar pro parser
                while(line != null){
                    line = reader.readLine()
                    //Log.d(TAG,"Line: " + line)
                    respostaHTTP.append(line)
                }
                Log.d(TAG,"InputStream: " + respostaHTTP.toString())
                ListItems = Parser.parse(respostaHTTP.toString())
            }
            //se não tenho conectividade com a internet... pego os episódios no db
            else {
                episodes = db.episodesDAO().todosEpisodios()
            }
            uiThread{
                // ui thread que irá determinar se eu uso os o objeto listitem e coloco o adapter nele (caso tenha conectividade)
                // caso contrário eu pego os episódios do db, jogo no listitem e assim coloco o adapter.
                Log.d(TAG,"ItemsList: " + ListItems)
                if(isNetworkAvailable()) {
                    Log.d("MainActivity", " " + musicPlayerService)
                    my_recyclerview.adapter = CustomAdapter(ListItems!!, this@MainActivity, isBound, musicPlayerService)
                    my_recyclerview.addItemDecoration(
                        DividerItemDecoration(
                            this@MainActivity,
                            LinearLayoutManager.VERTICAL
                        )
                    )
                    Log.d(TAG, "Parsing Done!")
                }
                else{
                    for(episode in episodes){
                        ListItems!!.add(ItemFeed(episode.title,episode.link,episode.date,episode.description,episode.downloadLink,episode.path,episode.duration))

                    }
                    Log.d("MainActivity", " " + musicPlayerService)
                    my_recyclerview.adapter = CustomAdapter(ListItems!!, this@MainActivity, isBound, musicPlayerService)
                    my_recyclerview.addItemDecoration(
                        DividerItemDecoration(
                            this@MainActivity,
                            LinearLayoutManager.VERTICAL
                        )
                    )
                }
                //codigo que insere assincronamente os episódios que tem seu XML já baixado e colocando no DB
                doAsync {
                    for (itemfeed in ListItems!!){
                        val episode = Episode(itemfeed.title,itemfeed.pubDate,itemfeed.path,itemfeed.link,itemfeed.pubDate,itemfeed.description,itemfeed.downloadLink,itemfeed.duration)
                        db.episodesDAO().inserirEpisodios(episode)
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    private fun podeStorage() : Boolean {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this@MainActivity, perm)
    }



}
