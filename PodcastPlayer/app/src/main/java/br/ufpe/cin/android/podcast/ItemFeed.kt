package br.ufpe.cin.android.podcast

data class ItemFeed(val title: String, val link: String, val pubDate: String, val description: String, val downloadLink: String,
                    var path : String, var duration: Int) {

    override fun toString(): String {
        return title
    }

}
