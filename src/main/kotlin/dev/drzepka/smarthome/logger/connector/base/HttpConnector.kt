package dev.drzepka.smarthome.logger.connector.base

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.smarthome.logger.model.config.source.SourceConfig
import dev.drzepka.smarthome.logger.util.NoopX509TrustManager
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.HttpClients
import java.net.URI


abstract class HttpConnector(private val config: SourceConfig) : Connector {
    protected open val skipCertificateCheck = false

    private lateinit var httpClient: HttpClient
    private lateinit var requestConfig: RequestConfig

    override fun initialize() {
        httpClient = if (skipCertificateCheck) certificateIgnoringHttpClient else standardHttpClient

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.timeout * 1000)
                .setConnectionRequestTimeout(config.timeout * 1000)
                .setSocketTimeout(config.timeout * 1000)
                .build()
    }

    final override fun getData(dataType: DataType, silent: Boolean): VendorData? {
        val uri = URI(getUrl(dataType))

        val get = HttpGet(uri)
        get.config = requestConfig

        val bytes = httpClient.execute(get) {
            val stream = it.entity.content
            val bytes = stream.readBytes()
            stream.close()
            bytes
        }

        return parseResponseData(dataType, bytes)
    }

    abstract fun parseResponseData(dataType: DataType, bytes: ByteArray): VendorData

    companion object {
        @JvmStatic
        val mapper = ObjectMapper()

        private val standardHttpClient = HttpClients.createDefault()
        private val certificateIgnoringHttpClient = HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(NoopX509TrustManager.sslContext)
                .build()

        init {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}