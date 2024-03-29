package br.ufpe.cin.android.podcast


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="episodes")
data class Episode (
    @PrimaryKey var title:String,
    var date:String,
    var path:String,
    var link:String,
    var pubDate:String,
    var description:String,
    var downloadLink:String,
    var duration : Int

) {
    override fun toString(): String {
        return title
    }
}