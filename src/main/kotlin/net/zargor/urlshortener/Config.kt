package net.zargor.urlshortener

import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import java.io.File

data class Config(val bindHost : String, val bindPort : Int, val authKey : String, val url : String, val mongoDb : MongoDb) {
    data class MongoDb(val host : String, val port : Int, val username : String, val password : String, val db : String, val authDb : String, val collection : String)

    fun save(f : File) {
        if (!f.exists())
            f.createNewFile()
        TomlWriter().write(this, f)
    }

    companion object {
        fun getConfig(f : File) : Config? {
            if (!f.exists())
                return null
            return Toml().read(f).to(Config::class.java)
        }
    }
}