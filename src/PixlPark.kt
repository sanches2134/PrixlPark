import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.internal.parser.JSONParser
import jdk.nashorn.internal.parser.Parser
import jdk.nashorn.internal.runtime.JSONFunctions
import jdk.nashorn.internal.runtime.JSONListAdapter

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
fun JSONConvert(requestString:String,fieldName:String):String
{
    val parser = com.beust.klaxon.Parser()
    val stringBuilder: StringBuilder = StringBuilder(requestString)
    val json: JsonObject = parser.parse(stringBuilder)as JsonObject
    val str= json.string(fieldName).toString()
    return str
}
fun GetREquestToken():String {
    val url = URL("http://api.pixlpark.com/oauth/requesttoken")
    var str :String=""
    with(url.openConnection() as HttpURLConnection) {
        requestMethod = "GET"  // optional default is GET
        inputStream.bufferedReader().use {
            it.lines().forEach { line ->
               line.let{ str=JSONConvert(line,"RequestToken") } ?: println("Something happend")

            }
        }
    }
    return str
}

private fun GetPassword(privateKey:String,requestToken:String)
:String{
    var password=requestToken+privateKey;

    val bytes = MessageDigest.getInstance("SHA-1").digest(password.toByteArray())
    return bytes.joinToString("") {
        "%02x".format(it)
    }
    }
fun GetAccessToken(requestToken: String, publicKey:String, password:String):String {

    var reqParam = URLEncoder.encode("oauth_token", "UTF-8") + "=" + URLEncoder.encode(requestToken, "UTF-8")
    reqParam += "&" + URLEncoder.encode("grant_type", "UTF-8") + "=" + URLEncoder.encode("api", "UTF-8")
    reqParam += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(publicKey, "UTF-8")
    reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")

    val mURL = URL("http://api.pixlpark.com/oauth/accesstoken?"+reqParam)
    var resp=""
    with(mURL.openConnection() as HttpURLConnection) {
        requestMethod = "GET"
        BufferedReader(InputStreamReader(inputStream)).use {
            val response = StringBuffer()

            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            response.let{ resp=JSONConvert(response.toString(),"AccessToken") } ?: println("Something happend")

        }
    }
    return resp
}
fun GetOrders(accessToken: String) {

    var reqParam = URLEncoder.encode("oauth_token", "UTF-8") + "=" + URLEncoder.encode(accessToken, "UTF-8")
    var resp=""
    val mURL = URL("http://api.pixlpark.com/orders?"+reqParam)

    with(mURL.openConnection() as HttpURLConnection) {
        requestMethod = "GET"

        println("URL : $url")

        BufferedReader(InputStreamReader(inputStream)).use {
            val response = StringBuffer()
            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()

           println(response)
        }
    }
}

fun main (args: Array<String>)
{
    val publicKey="38cd79b5f2b2486d86f562e3c43034f8"
    val privateKey="8e49ff607b1f46e1a5e8f6ad5d312a80"
    val requestToken=GetREquestToken()
    GetOrders(GetAccessToken(requestToken,publicKey,GetPassword(privateKey,requestToken)))
}




