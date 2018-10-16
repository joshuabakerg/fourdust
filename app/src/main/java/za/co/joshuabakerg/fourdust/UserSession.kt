package za.co.joshuabakerg.fourdust

public class UserSession private constructor() {
    init {  }

    private object Holder { val INSTANCE = UserSession() }

    companion object {
        val instance: UserSession by lazy { Holder.INSTANCE }
    }

    var sessionID: String? = null
    var user: Map<String, Object>? = null
}