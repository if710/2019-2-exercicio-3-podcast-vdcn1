package br.ufpe.cin.android.podcast

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(entities= arrayOf(Episode::class), version=1)
abstract class EpisodesDB : RoomDatabase(){
    abstract fun episodesDAO() : EpisodesDAO
    companion object {
        private var INSTANCE : EpisodesDB? = null
        fun getDatabase(ctx : Context) : EpisodesDB {
            if (INSTANCE == null) {
                synchronized(EpisodesDB::class) {
                    INSTANCE = databaseBuilder(
                        ctx.applicationContext,
                        EpisodesDB::class.java,
                        "episodes.db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}