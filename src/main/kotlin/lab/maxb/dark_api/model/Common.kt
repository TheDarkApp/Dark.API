package lab.maxb.dark_api.model


import java.security.MessageDigest
import java.util.*
import kotlin.text.Charsets.UTF_8

inline val randomUUID get(): UUID = UUID.randomUUID()

fun getTimestampNow() = System.currentTimeMillis() / 1000L

fun String.toSHA256()
    = MessageDigest.getInstance("SHA-256")
                   .digest(toByteArray(UTF_8))
                   .fold("") { str, it ->
                        str + "%02x".format(it)
                   }