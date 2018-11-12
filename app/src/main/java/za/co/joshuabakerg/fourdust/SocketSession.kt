package za.co.joshuabakerg.fourdust

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

private var socket: MessageSocket = MessageSocket()

fun getMessageSocket(): MessageSocket {
    return socket
}

class MessageSocket(private val sessionId: String? = UserSession.instance.sessionID) {

    private val objectMapper = ObjectMapper()

    private var socket: Socket

    private val callbacks = HashMap<String, MutableList<(Any) -> Unit>>()

    init {
        socket = IO.socket("http://test.joshuabakerg.co.za")
        socket.on(Socket.EVENT_CONNECT) {
            Log.i("SocketSession", "Connected to socket")
            socket.emit("message", """
                {
                    "type" : "register-session",
                    "sessionId" : "$sessionId"
                }
            """.trimIndent())
            socket.emit("message", """
                {
                    "type" : "join-chat",
                    "convId" : "conv1",
                    "sessionId" : "$sessionId"
                }
            """.trimIndent())
        }.on("message") {
            val jsonObject = it[0] as JSONObject
            val string = jsonObject.getString("type")
            val data = jsonObject.get("data")
        }.on(Socket.EVENT_DISCONNECT) {
            Log.i("SocketSession", "Disconnected from socket")
        }
        socket.connect()
    }

    fun emit(type: String, data: Any) {
        val convertValue = objectMapper.convertValue(data, LinkedHashMap::class.java) as LinkedHashMap<String, Any>
        convertValue["type"] = type
        convertValue["sessionId"] = sessionId!!
        val json = objectMapper.writeValueAsString(convertValue)
        socket.emit("message", json)
    }

    fun on(type: String, callback: (Any) -> Unit) {
        var list = callbacks[type]
        if (list == null) {
            list = ArrayList()
            callbacks[type] = list
        }
        list.add(callback)
    }

}
