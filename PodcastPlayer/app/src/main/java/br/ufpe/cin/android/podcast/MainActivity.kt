package br.ufpe.cin.android.podcast

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val ON_CREATE_REQUEST = 710
    internal var ListItems : ArrayList<ItemFeed>? = null
    private val TAG : String = "MainActivity"
    private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val STORAGE_REQUEST = ON_CREATE_REQUEST + 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Lista de itemfeed's que guardam os episódios com as informações pedidas.
        ListItems = arrayListOf() //atribuindo isso pra caso ele não ser null
        my_recyclerview.layoutManager = LinearLayoutManager(this) //usando linearlayout
        //chamar a função pra dar parse sem usar a uiThread
        parseAsync()


    }



    fun parseAsync(){
        // string do link que ira se transformar em objeto URL pra conexão

        val link = "https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml"
        val url = URL(link)
        //pegar instancia do banco de dados

        var episodes : Array<Episode> = arrayOf()
        val db = EpisodesDB.getDatabase(applicationContext)
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
                    my_recyclerview.adapter = CustomAdapter(ListItems!!, this@MainActivity)
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
                        ListItems!!.add(ItemFeed(episode.title,"",episode.date,"","",""))

                    }
                    my_recyclerview.adapter = CustomAdapter(ListItems!!, this@MainActivity)
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
                        val episode = Episode(itemfeed.title,itemfeed.pubDate,"")
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
