package com.example.coldpreventionguardianapp.data.repository

import com.example.coldpreventionguardianapp.data.model.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CommunityRepository {
    private val database = FirebaseDatabase.getInstance()
    private val commentsRef = database.getReference("comments")

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Observe all comments in real-time.
     * Emits a list sorted by timestamp descending (newest first).
     */
    fun observeComments(): Flow<List<Comment>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val id = child.key ?: continue
                        @Suppress("UNCHECKED_CAST")
                        val map = child.value as? Map<String, Any?> ?: continue
                        comments.add(Comment.fromSnapshot(id, map))
                    }
                    comments.sortByDescending { it.timestamp }
                }
                trySend(comments)
            }

            override fun onCancelled(error: DatabaseError) {
                // Silently ignore
            }
        }
        commentsRef.addValueEventListener(listener)

        awaitClose {
            commentsRef.removeEventListener(listener)
        }
    }

    /**
     * Post a new comment. Generates date/timestamp automatically.
     */
    suspend fun postComment(author: String, content: String) {
        val now = Date()
        val date = dateFormatter.format(now)
        val timestamp = now.time

        val newRef = commentsRef.push()
        val comment = mapOf(
            "author" to author,
            "content" to content,
            "date" to date,
            "timestamp" to timestamp,
            "likes" to 0,
            "likedBy" to emptyList<String>()
        )
        newRef.setValue(comment).await()
    }

    /**
     * Toggle like for a comment by the given username.
     * Reads latest snapshot, toggles like, writes back.
     */
    suspend fun toggleLike(commentId: String, currentUsername: String) {
        val ref = commentsRef.child(commentId)
        val snapshot = ref.get().await()
        if (!snapshot.exists()) return

        @Suppress("UNCHECKED_CAST")
        val map = snapshot.value as? Map<String, Any?> ?: return
        val comment = Comment.fromSnapshot(commentId, map)

        val updatedLikedBy = comment.likedBy.toMutableList()
        val isLiked = currentUsername in updatedLikedBy

        if (isLiked) {
            updatedLikedBy.remove(currentUsername)
        } else {
            updatedLikedBy.add(currentUsername)
        }

        val updateMap = mapOf(
            "likes" to updatedLikedBy.size,
            "likedBy" to updatedLikedBy
        )
        ref.updateChildren(updateMap).await()
    }
}