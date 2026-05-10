package com.example.coldpreventionguardianapp.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val language: String = "zh-CN",
    val ageGroup: String = "成年人",
    val registeredAt: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "language" to language,
            "ageGroup" to ageGroup,
            "registeredAt" to registeredAt
        )
    }

    companion object {
        fun fromMap(uid: String, map: Map<String, Any?>): User {
            return User(
                uid = uid,
                username = map["username"] as? String ?: "",
                email = map["email"] as? String ?: "",
                language = map["language"] as? String ?: "zh-CN",
                ageGroup = map["ageGroup"] as? String ?: "成年人",
                registeredAt = map["registeredAt"] as? String ?: ""
            )
        }
    }
}