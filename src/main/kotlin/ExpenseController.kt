package org.example

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

/**
 * 簡易的なHTTPサーバを作成し、8080ポートで起動します。
 * `/api/init` へのアクセス時に簡単な処理（メッセージを返却）を行います。
 */
class ExpenseController {
    /**
     * 8080番ポートでサーバを起動し、/api/init エンドポイントをハンドリングします。
     * - GET /api/init: 200 OK と共に初期化メッセージを返します。
     * - それ以外のメソッド: 405 Method Not Allowed
     * 戻り値として HttpServer を返すため、必要であれば呼び出し側で停止させることもできます。
     */
    fun createServer(): HttpServer {
        val server = HttpServer.create(InetSocketAddress(8080), 0)

        server.createContext("/expense/init", HttpHandler { exchange ->
            handleInit(exchange)

        })

        // デフォルト: 他のパスは 404
        server.createContext("/") { exchange ->
            exchange.sendResponseHeaders(404, -1)
            exchange.close()
        }

        server.executor = null // デフォルトのシングルスレッドエグゼキュータ
        server.start()
        return server
    }

    @Throws(IOException::class)
    private fun handleInit(exchange: HttpExchange) {
        if (exchange.requestMethod.equals("GET", ignoreCase = true)) {
            val file = File("front/index.html")
            if (file.exists()) {
                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                val bytes = file.readBytes()
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
                val response = "index.html not found"
                val bytes = response.toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(404, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
        } else {
            exchange.sendResponseHeaders(405, -1)
        }
        exchange.close()
    }
}
