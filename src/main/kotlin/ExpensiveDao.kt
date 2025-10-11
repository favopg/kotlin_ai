import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime

class ExpensiveDao {
    object Expense : Table("expense") {
        val id = integer("id").autoIncrement() // Primary key
        val yearsMonth = integer("years_month").check { it.between(10000000, 99999999) } // 8-digit number
        val category = text("category").check { it.neq("") } // Unlimited length string
        val money = decimal("money", precision = 11, scale = 0) // 11-digit number
        val createdAt = datetime("created_at") // Date with millisecond precision
        val updatedAt = datetime("updated_at") // Date with millisecond precision

        override val primaryKey = PrimaryKey(id)
    }

    /**
     * データベースへの接続を確立します。
     *
     * このメソッドは、指定されたJDBC URLとドライバを使用してH2データベースへの接続を初期化します。
     * 接続はデータベースファイルが閉じないように設定されており、同時実行制御のためにファイルベースのロックを使用します。
     * テーブルの作成、レコードの挿入、データのクエリなど、データベース操作を行う前にこの設定が必要です。
     */
    fun dbConnect() {
        Database.connect(
            "jdbc:h2:file:./expense_database;DB_CLOSE_DELAY=-1;FILE_LOCK=FS",
            driver = "org.h2.Driver"
        )
    }

    /**
     * データベースに `expense` テーブルを作成します。
     *
     * このメソッドは、`Expense` オブジェクトで定義されたカラムおよび制約に基づいて
     * `expense` テーブルを作成することで、データベーススキーマを初期化します。
     * データベース接続を確立した後に呼び出す必要があります。
     */
    fun create() {
        SchemaUtils.create(Expense)
    }

    /**
     * 指定された詳細を持つ新しい経費レコードをデータベースに挿入します。
     *
     * @param yearsMonth 年と月を表す8桁の整数（形式: YYYYMM）。
     * @param category 経費カテゴリを表す空でない文字列。
     * @param money 経費の金額を表すBigDecimal値。最大11桁の精度およびスケール0を超えてはなりません。
     */
    fun insert(yearsMonth: Int, category: String, money: java.math.BigDecimal) {
        Expense.insert {
            it[this.yearsMonth] = yearsMonth
            it[this.category] = category
            it[this.money] = money
            it[this.createdAt] = java.time.LocalDateTime.now()
            it[this.updatedAt] = java.time.LocalDateTime.now()
        }
    }

    /**
     * 指定されたIDを基に、`expense` テーブルから経費レコードを検索して返却します。
     *
     * @param id 検索するレコードの主キーID。
     * @return 検索結果のMap形式のデータ、またはnull（結果が存在しない場合）。
     */
    fun selectByPrimaryKey(id: Int): Map<String, Any?>? {
        return Expense.select { Expense.id eq id }
            .map { row ->
                mapOf(
                    "id" to row[Expense.id],
                    "yearsMonth" to row[Expense.yearsMonth],
                    "category" to row[Expense.category],
                    "money" to row[Expense.money],
                    "createdAt" to row[Expense.createdAt],
                    "updatedAt" to row[Expense.updatedAt]
                )
            }.singleOrNull()
    }
}