package org.example

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import java.io.File

class ExpenseController {

    private val gson = Gson()

    /**
     * Ktorでサーバーを起動し、frontフォルダを静的コンテンツとして公開します。
     *
     * 要件:
     * - /expense/init: JSONでcategoryとmoneyのリストを返却
     * - /expense/register: リクエストボディ(JSON)からcategory/moneyを受け取る
     * - front配下のindex.html等を配信
     */
    fun createServer(port: Int = 8080) {
        embeddedServer(Netty, port = port) {
            routing {
                // 静的ファイル配信 (frontディレクトリ直下をルートにマウント)
                staticFiles("/", File("front")) {
                    default("index.html")
                }

                // ルートに来た場合はindex.htmlへ誘導
                get("/") {
                    call.respondRedirect("/index.html")
                }

                // /expense/init: JSONでcategory/moneyのリストを返却
                get("/expense/init") {
                    val items = listOf(
                        mapOf("category" to "食費", "money" to 1000),
                        mapOf("category" to "交通", "money" to 2000),
                        mapOf("category" to "住居", "money" to 3000)
                    )
                    val json = gson.toJson(items)
                    call.respondText(json, ContentType.Application.Json)
                }

                // /expense/register: JSONを受け取り、内容をそのまま返す（エコー）
                post("/expense/register") {
                    val body = call.receiveText()
                    // caretegory(typo)にも対応: 両方見てcategoryに正規化
                    val obj = runCatching { gson.fromJson(body, JsonObject::class.java) }.getOrNull()
                    if (obj == null) {
                        call.respond(HttpStatusCode.BadRequest, "{\"error\":\"invalid json\"}")
                        return@post
                    }
                    val category = (obj.get("category") ?: obj.get("caretegory"))?.asString
                    val money = obj.get("money")?.asString ?: obj.get("money")?.asNumber?.toString()

                    if (category.isNullOrBlank() || money.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "{\"error\":\"category and money are required\"}")
                        return@post
                    }

                    val response = mapOf(
                        "status" to "ok",
                        "category" to category,
                        "money" to money
                    )
                    call.respondText(gson.toJson(response), ContentType.Application.Json)
                }
            }
        }.start(wait = true)
    }
}
