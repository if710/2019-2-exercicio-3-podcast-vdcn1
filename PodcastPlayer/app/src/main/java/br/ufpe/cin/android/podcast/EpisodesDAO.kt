package br.ufpe.cin.android.podcast

import androidx.room.*

@Dao
interface EpisodesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserirEpisodios(vararg episodios: Episode)
    @Update
    fun atualizarEpisodios(vararg episodios:Episode)
    @Delete
    fun removerEpisodios(vararg episodios:Episode)
    @Query("SELECT * FROM episodes")
    fun todosEpisodios() : Array<Episode>
    @Query("SELECT * FROM episodes WHERE date LIKE :q")
    fun buscaEpisodiopeladata(q : String) : List<Episode>
    @Query("SELECT * FROM episodes WHERE title LIKE :q")
    fun buscaEpisodiopelotitulo(q : String) : List<Episode>
}