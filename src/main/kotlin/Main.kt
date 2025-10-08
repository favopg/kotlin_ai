package org.example

import com.sun.net.httpserver.HttpServer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import java.io.File
import java.net.InetSocketAddress

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 50)

    override val primaryKey = PrimaryKey(id)
}

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    // HTML 配信
    server.createContext("/") { exchange ->
        val file = File("front/index.html")
        val bytes = file.readBytes()
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    // Kotlin処理1
    server.createContext("/api/hello") { exchange ->
        val response = "Kotlin処理1の結果: こんにちは！"
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    // Kotlin処理2
    server.createContext("/api/goodbye") { exchange ->
        val response = "Kotlin処理2の結果: さようなら！"
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    server.executor = null
    server.start()
    println("Server started at http://localhost:8080")


    Database.connect(
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
        "org.h2.Driver"
    )

    transaction {
        // テーブルの作成
        create(Users)

        // データの挿入
        Users.insert {
            it[name] = "イッシー"
        }
        Users.insert {
            it[name] = "ロボット"
        }

        // 検索
        Users.selectAll().forEach {
            println("id: ${it[Users.id]}, name: ${it[Users.name]}")
        }
    }

}