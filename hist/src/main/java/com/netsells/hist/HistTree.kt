package com.netsells.hist

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A [Timber] Tree which sends logs to a Logstash instance.
 *
 * If a log cannot be sent, or Logstash is busy, Hist will retry up to 6 times, and the time between retries increases by 10s each time.
 *
 * @author Peter Bryant
 *
 * @param appName The name of this application e.g. "My Android App"
 * @param appVersionName The version name of this application e.g. "1.2.3"
 * @param appVersionCode The version code of this application e.g. 72
 * @param environment Should be "debug" or "production" as appropriate
 * @param logstashHost The hostname of the Logstash instance e.g. "my.logstash.io"
 * @param logstashPort The port of the Logstash instance e.g. 4000
 */
class HistTree(
    private val appName: String,
    private val appVersionName: String,
    private val appVersionCode: Int,
    private val environment: String,
    private val logstashHost: String,
    private val logstashPort: Int
) : Timber.Tree() {

    private val logstashService by lazy { LogstashService.create("$logstashHost:$logstashPort") }

    private val gson by lazy { Gson() }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val data = mutableMapOf<Any?, Any?>(
            "app" to mapOf(
                "project" to appName,
                "environment" to environment,
                "version" to "$appVersionName ($appVersionCode)"
            ),
            "android_device" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "android_version" to "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})",
            "event" to mapOf(
                "created" to isoDateFormat.format(Date()),
                "type" to if (t != null) "exception" else "log"
            ),
            "level" to priority.priorityString,
            "message" to (if (t != null) "${t.javaClass.name}: ${t.message}" else if (tag != null) "$tag: $message" else message)
        )
        if (t != null) {
            data["exception"] = mapOf(
                "data" to mapOf(
                    "message" to t.localizedMessage,
                    "stacktrace" to t.stackTrace.joinToString(separator = "\n"),
                    "previous" to if (t.cause != null) mapOf(
                        "message" to t.cause!!.message,
                        "stacktrace" to t.cause!!.stackTrace.joinToString(separator = "\n")
                    ) else null
                )
            )
        }

        sendLog(gson.toJsonTree(data).asJsonObject)
    }

    private fun sendLog(data: JsonObject, delayMillis: Long = 0L) {
        scope.launch {
            delay(delayMillis)
            try {
                logstashService.sendLog(data)
            } catch (e: HttpException) {
                if (e.code() == 429 && delayMillis <= maxDelayMillis) {
                    sendLog(data, delayMillis + 10000)
                }
            } catch (e: SocketTimeoutException) {
                if (delayMillis <= maxDelayMillis) {
                    sendLog(data, delayMillis + 10000)
                }
            } catch (e: UnknownHostException) {
                if (delayMillis <= maxDelayMillis) {
                    sendLog(data, delayMillis + 10000)
                }
            }
        }
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        private const val maxDelayMillis = 60000

        private val Int.priorityString: String
            get() {
                return when (this) {
                    Log.VERBOSE, Log.DEBUG -> "DEBUG"
                    Log.INFO -> "INFO"
                    Log.WARN -> "WARN"
                    Log.ERROR -> "ERROR"
                    Log.ASSERT -> "CRITICAL"
                    else -> throw IllegalArgumentException("Priority $this does not exist!")
                }
            }
    }
}