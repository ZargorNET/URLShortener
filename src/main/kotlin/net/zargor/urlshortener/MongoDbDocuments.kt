package net.zargor.urlshortener

import org.bson.codecs.pojo.annotations.BsonId

data class ShortenedUrl(@BsonId var id : String = "", var url : String = "", var createdTime : Long = 0)