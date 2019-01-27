package net.zargor.urlshortener

import com.mongodb.client.model.Filters
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.system.exitProcess

const val ID_LENGTH = 5;
fun main(args : Array<String>) {
    val configFile = File("config.toml")
    var config = Config.getConfig(configFile)
    if (config == null) {
        config = Config("127.0.0.1", 8080, "your_auth_key", "https://127.0.0.1:8080/", Config.MongoDb("example.tld", 27017, "your_username", "your_password", "your_db", "your_auth_db", "your_collection"))
        config.save(configFile)
        println("New config created! Go check it out")
        exitProcess(-1)
    }
    val mongodb = MongoDb(config.mongoDb)
    val mongoDbColl = mongodb.mongoClient.getDatabase(config.mongoDb.db).getCollection(config.mongoDb.collection, ShortenedUrl::class.java)
    val server = embeddedServer(Netty, host = config.bindHost, port = config.bindPort) {
        install(StatusPages) {
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                cause.printStackTrace()
            }
        }

        routing {
            static {
                resource("/", "index.html")
                resources()
            }
            post(path = "/shorten") {
                val post = call.receiveParameters()
                val url = post["url"]
                var auth = post["auth"]

                if (url.isNullOrBlank()) {
                    call.respondText("No URL specified", status = HttpStatusCode.BadRequest)
                    return@post
                }
                if (auth.isNullOrBlank()) {
                    call.respondText("Authkey is not specified", status = HttpStatusCode.BadRequest)
                    return@post
                }
                auth = URLDecoder.decode(auth, "UTF-8")

                if (config.authKey != auth) {
                    call.respondText("Wrong authkey!", status = HttpStatusCode.BadRequest)
                    return@post
                }

                if (!url.matches(Regex("(http:\\/\\/|https:\\/\\/).+\\..+"))) {
                    call.respondText("Invalid URL", status = HttpStatusCode.BadRequest)
                    return@post
                }
                //CHECK IF THE URL HAS BEEN PARSED BEFORE
                val oldDoc = mongoDbColl.find(Filters.eq("url", url)).firstOrNull()
                if (oldDoc != null) {
                    call.respondText(config.url + oldDoc.id, status = HttpStatusCode.OK)
                    return@post
                }
                ////
                var id : String
                do
                    id = RandomStringUtils.random(ID_LENGTH, true, true)
                while (mongoDbColl.find(Filters.eq("_id", id)).any())

                GlobalScope.launch { mongoDbColl.insertOne(ShortenedUrl(id, url, Date().time)) }
                println("Shortened new url! ID: $id; URL: $url")
                call.respondText(config.url + id, status = HttpStatusCode.OK)
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: return@get
                if (id.startsWith("static"))
                    return@get
                val doc = mongoDbColl.find(Filters.eq("_id", id)).firstOrNull()

                if (doc == null) {
                    call.respondText("url not found", status = HttpStatusCode.NotFound)
                    return@get
                }

                call.respondRedirect(doc.url, false)
            }
        }
    }
    println("Started!")
    server.start(true)
}
