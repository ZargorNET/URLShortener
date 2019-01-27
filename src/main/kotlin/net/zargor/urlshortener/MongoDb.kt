package net.zargor.urlshortener

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import java.util.*

class MongoDb(val cfg : Config.MongoDb) {
    val mongoClient : MongoClient

    init {
        val registry : CodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))
        this.mongoClient = MongoClients.create(MongoClientSettings.builder()
                .codecRegistry(registry)
                .applyToClusterSettings {
                    it.hosts(Arrays.asList(ServerAddress(this.cfg.host, this.cfg.port)))
                }.credential(MongoCredential.createCredential(this.cfg.username, this.cfg.authDb, this.cfg.password.toCharArray())).build())
    }
}