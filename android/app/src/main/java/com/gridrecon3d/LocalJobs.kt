package com.gridrecon3d

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
enum class UnitsMode { METRIC, IMPERIAL, BOTH }

@Serializable
data class LocalJob(
    val jobId: String,
    val mode: String,
    val createdAt: Long,
    val status: String, // LOCAL, UPLOADED, COMPLETE
    val shotCount: Int = 0,
    val scaleFactor: Double = 1.0, // multiply model units -> meters (relative)
    val unitsMode: UnitsMode = UnitsMode.BOTH
)

object LocalJobs {
    private fun file(ctx: Context) = File(ctx.filesDir, "jobs.json")

    fun list(ctx: Context): List<LocalJob> {
        return try {
            val f = file(ctx)
            if (!f.exists()) emptyList()
            else Json.decodeFromString(f.readText())
        } catch (_: Exception) { emptyList() }
    }

    fun upsert(ctx: Context, job: LocalJob) {
        val all = list(ctx).toMutableList()
        val idx = all.indexOfFirst { it.jobId == job.jobId }
        if (idx >= 0) all[idx] = job else all.add(0, job)
        file(ctx).writeText(Json { prettyPrint = true }.encodeToString(all))
    }

    fun get(ctx: Context, jobId: String): LocalJob? = list(ctx).firstOrNull { it.jobId == jobId }

    fun newJobId(): String = UUID.randomUUID().toString()
}
