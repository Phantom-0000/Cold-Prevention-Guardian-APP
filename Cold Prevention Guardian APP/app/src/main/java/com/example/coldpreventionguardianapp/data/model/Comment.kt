package com.example.coldpreventionguardianapp.data.model

data class Comment(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "author" to author,
            "content" to content,
            "date" to date,
            "timestamp" to timestamp,
            "likes" to likes,
            "likedBy" to likedBy
        )
    }

    companion object {
        fun fromSnapshot(id: String, map: Map<String, Any?>): Comment {
            return Comment(
                id = id,
                author = map["author"] as? String ?: "",
                content = map["content"] as? String ?: "",
                date = map["date"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                likes = (map["likes"] as? Number)?.toInt() ?: 0,
                likedBy = (map["likedBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}