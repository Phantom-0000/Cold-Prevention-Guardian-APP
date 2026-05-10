package com.example.coldpreventionguardianapp.data.repository

import com.example.coldpreventionguardianapp.data.model.TemperatureRecord
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TemperatureRepository {
    private val database = FirebaseDatabase.getInstance()

    /**
     * Observe temperature records for a given user.
     * Emits a list sorted by timestamp descending (newest first).
     */
    fun observeTemperatureRecords(uid: String): Flow<List<TemperatureRecord>> = callbackFlow {
        val reference = database.getReference("users").child(uid).child("temperatureRecords")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = mutableListOf<TemperatureRecord>()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val date = child.child("date").value as? String ?: continue
                        val temperature = (child.child("temperature").value as? Number)?.toDouble() ?: continue
                        val timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: 0L
                        records.add(TemperatureRecord(date = date, temperature = temperature, timestamp = timestamp))
                    }
                    // Sort by timestamp descending (newest first)
                    records.sortByDescending { it.timestamp }
                }
                trySend(records)
            }

            override fun onCancelled(error: DatabaseError) {
                // Silently ignore
            }
        }
        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }

    /**
     * Get the latest temperature record as a one-shot fetch.
     */
    suspend fun getLatestRecord(uid: String): List<TemperatureRecord> {
        val snapshot = database.getReference("users").child(uid).child("temperatureRecords").get().await()
        val records = mutableListOf<TemperatureRecord>()
        if (snapshot.exists()) {
            for (child in snapshot.children) {
                val date = child.child("date").value as? String ?: continue
                val temperature = (child.child("temperature").value as? Number)?.toDouble() ?: continue
                val timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: 0L
                records.add(TemperatureRecord(date = date, temperature = temperature, timestamp = timestamp))
            }
            records.sortByDescending { it.timestamp }
        }
        return records
    }

    /**
     * Submit a temperature record.
     * - If a record with the same date already exists, overwrite it.
     * - Keep at most 7 records (newest first by timestamp).
     */
    suspend fun submitTemperature(uid: String, record: TemperatureRecord) {
        val reference = database.getReference("users").child(uid).child("temperatureRecords")

        // 1. Fetch all existing records
        val snapshot = reference.get().await()
        val existingRecords = mutableListOf<TemperatureRecord>()
        if (snapshot.exists()) {
            for (child in snapshot.children) {
                val date = child.child("date").value as? String ?: continue
                val temperature = (child.child("temperature").value as? Number)?.toDouble() ?: continue
                val timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: 0L
                existingRecords.add(TemperatureRecord(date = date, temperature = temperature, timestamp = timestamp))
            }
        }

        // 2. Remove existing entry with the same date (if any)
        existingRecords.removeAll { it.date == record.date }

        // 3. Add new record
        existingRecords.add(record)

        // 4. Sort by timestamp descending, keep only latest 7
        existingRecords.sortByDescending { it.timestamp }
        val trimmedRecords = existingRecords.take(7)

        // 5. Write back to Firebase — replace entire node
        val recordsMap = trimmedRecords.mapIndexed { index, rec ->
            index.toString() to mapOf(
                "date" to rec.date,
                "temperature" to rec.temperature,
                "timestamp" to rec.timestamp
            )
        }.toMap()

        reference.setValue(recordsMap).await()
    }
}