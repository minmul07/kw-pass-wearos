package minmul.kwpass.shared

import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NetworkProfilingListener : EventListener() {
    companion object {
        const val PROFILE_SESSION_HEADER = "X-KWPass-Profile-Session"

        private const val TAG = "NetworkProfile"
        private val sessions = ConcurrentHashMap<String, ProfilingSession>()

        fun startQrProfilingSession(source: String, trigger: String): String? {
            if (!BuildConfig.DEBUG) return null

            val sessionId = "$source-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(8)}"
            val session = ProfilingSession(
                id = sessionId,
                source = source,
            )
            sessions[sessionId] = session
            logInternal(session, "qr.request.start", "trigger=$trigger")
            return sessionId
        }

        fun logStage(sessionId: String?, stage: String, detail: String? = null) {
            if (!BuildConfig.DEBUG) return

            val session = sessionId?.let(sessions::get) ?: return
            logInternal(session, stage, detail)
        }

        fun finishSession(sessionId: String?, stage: String, detail: String? = null) {
            if (!BuildConfig.DEBUG) return

            val session = sessionId?.let(sessions::remove) ?: return
            logInternal(session, stage, detail)
        }

        private fun logInternal(session: ProfilingSession, stage: String, detail: String?) {
            val now = System.nanoTime()
            val (totalMs, deltaMs) = synchronized(session) {
                val totalMs = nanosToMillis(now - session.startedAtNanos)
                val deltaMs = nanosToMillis(now - session.lastLoggedAtNanos)
                session.lastLoggedAtNanos = now
                totalMs to deltaMs
            }

            val detailSuffix = detail
                ?.takeIf { it.isNotBlank() }
                ?.let { " | $it" }
                .orEmpty()

            Timber.tag(TAG).i(
                "[session=${session.id}][source=${session.source}] %s | total=%dms | delta=%dms%s",
                stage,
                totalMs,
                deltaMs,
                detailSuffix,
            )
        }

        private fun nanosToMillis(nanos: Long): Long = nanos / 1_000_000L
    }

    private data class ProfilingSession(
        val id: String,
        val source: String,
        val startedAtNanos: Long = System.nanoTime(),
        var lastLoggedAtNanos: Long = startedAtNanos,
    )

    private fun sessionId(call: Call): String? =
        call.request().header(PROFILE_SESSION_HEADER)

    private fun methodAndPath(request: Request): String =
        "${request.method} ${request.url.encodedPath}"

    override fun callStart(call: Call) {
        logStage(sessionId(call), "okhttp.callStart", methodAndPath(call.request()))
    }

    override fun dnsStart(call: Call, domainName: String) {
        logStage(sessionId(call), "okhttp.dnsStart", "domain=$domainName")
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        val addresses = inetAddressList.joinToString { it.hostAddress ?: "unknown" }
        logStage(
            sessionId(call),
            "okhttp.dnsEnd",
            "domain=$domainName, resolved=${inetAddressList.size}, addresses=$addresses",
        )
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        logStage(
            sessionId(call),
            "okhttp.connectStart",
            "target=${inetSocketAddress.hostString}:${inetSocketAddress.port}, proxy=${proxy.type()}",
        )
    }

    override fun secureConnectStart(call: Call) {
        logStage(sessionId(call), "okhttp.secureConnectStart")
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        logStage(
            sessionId(call),
            "okhttp.secureConnectEnd",
            "tls=${handshake?.tlsVersion ?: "unknown"}, cipher=${handshake?.cipherSuite ?: "unknown"}",
        )
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
    ) {
        logStage(
            sessionId(call),
            "okhttp.connectEnd",
            "target=${inetSocketAddress.hostString}:${inetSocketAddress.port}, protocol=${protocol ?: "unknown"}",
        )
    }

    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
        ioe: IOException,
    ) {
        logStage(
            sessionId(call),
            "okhttp.connectFailed",
            "target=${inetSocketAddress.hostString}:${inetSocketAddress.port}, protocol=${protocol ?: "unknown"}, error=${ioe.javaClass.simpleName}:${ioe.message}",
        )
    }

    override fun connectionAcquired(call: Call, connection: Connection) {
        logStage(
            sessionId(call),
            "okhttp.connectionAcquired",
            "protocol=${connection.protocol()}, route=${connection.route().socketAddress}",
        )
    }

    override fun requestHeadersStart(call: Call) {
        logStage(sessionId(call), "okhttp.requestHeadersStart")
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        logStage(sessionId(call), "okhttp.requestHeadersEnd", methodAndPath(request))
    }

    override fun requestBodyStart(call: Call) {
        logStage(sessionId(call), "okhttp.requestBodyStart")
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        logStage(sessionId(call), "okhttp.requestBodyEnd", "bytes=$byteCount")
    }

    override fun requestFailed(call: Call, ioe: IOException) {
        logStage(
            sessionId(call),
            "okhttp.requestFailed",
            "error=${ioe.javaClass.simpleName}:${ioe.message}",
        )
    }

    override fun responseHeadersStart(call: Call) {
        logStage(sessionId(call), "okhttp.responseHeadersStart")
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        logStage(
            sessionId(call),
            "okhttp.responseHeadersEnd",
            "code=${response.code}, message=${response.message}",
        )
    }

    override fun responseBodyStart(call: Call) {
        logStage(sessionId(call), "okhttp.responseBodyStart")
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        logStage(sessionId(call), "okhttp.responseBodyEnd", "bytes=$byteCount")
    }

    override fun responseFailed(call: Call, ioe: IOException) {
        logStage(
            sessionId(call),
            "okhttp.responseFailed",
            "error=${ioe.javaClass.simpleName}:${ioe.message}",
        )
    }

    override fun connectionReleased(call: Call, connection: Connection) {
        logStage(
            sessionId(call),
            "okhttp.connectionReleased",
            "protocol=${connection.protocol()}, route=${connection.route().socketAddress}",
        )
    }

    override fun callEnd(call: Call) {
        logStage(sessionId(call), "okhttp.callEnd", methodAndPath(call.request()))
    }

    override fun callFailed(call: Call, ioe: IOException) {
        logStage(
            sessionId(call),
            "okhttp.callFailed",
            "error=${ioe.javaClass.simpleName}:${ioe.message}",
        )
    }
}
