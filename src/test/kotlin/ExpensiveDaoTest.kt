import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ExpensiveDaoTest {

    private val expensiveDao = ExpensiveDao()

    @BeforeEach
    fun setup() {
        expensiveDao.dbConnect()
        transaction {
            SchemaUtils.create(ExpensiveDao.Expense)
        }
    }

    @Test
    fun `should retrieve record by primary key when ID exists`() {
        transaction {
            val yearsMonth = 20230901
            val category = "Rent"
            val money = BigDecimal("1200")

            // Insert a record
            expensiveDao.insert(yearsMonth, category, money)
            val insertedId = ExpensiveDao.Expense.selectAll().single()[ExpensiveDao.Expense.id]

            // Retrieve and verify
            val retrieved = expensiveDao.selectByPrimaryKey(insertedId)
            assertTrue {
                retrieved != null &&
                        retrieved["id"] == insertedId &&
                        retrieved["yearsMonth"] == yearsMonth &&
                        retrieved["category"] == category &&
                        retrieved["money"] == money
            }
        }
    }

    @Test
    fun `should return null when ID does not exist`() {
        transaction {
            val nonExistentId = 999

            // Verify retrieval
            val result = expensiveDao.selectByPrimaryKey(nonExistentId)
            assertTrue(result == null)
        }
    }

    @AfterEach
    fun cleanup() {
        transaction {
            SchemaUtils.drop(ExpensiveDao.Expense)
        }
    }

    @Test
    fun `should successfully insert a valid expense`() {
        transaction {
            val yearsMonth = 20230101
            val category = "Groceries"
            val money = BigDecimal("150.00")

            expensiveDao.insert(yearsMonth, category, money)

            val inserted = ExpensiveDao.Expense.selectAll().singleOrNull()
            assertTrue {
                inserted != null &&
                        inserted[ExpensiveDao.Expense.yearsMonth] == yearsMonth &&
                        inserted[ExpensiveDao.Expense.category] == category
            }
        }
    }

    @Test
    fun `should fail when yearsMonth is out of range`() {
        transaction {
            val yearsMonth = 123
            val category = "Utilities"
            val money = BigDecimal("78.50")

            assertFails {
                expensiveDao.insert(yearsMonth, category, money)
            }
        }
    }

    @Test
    fun `should fail when category is empty`() {
        transaction {
            val yearsMonth = 20240101
            val category = ""
            val money = BigDecimal("99.99")

            assertFails {
                expensiveDao.insert(yearsMonth, category, money)
            }
        }
    }

    @Test
    fun `should fail when money exceeds maximum precision`() {
        transaction {
            val yearsMonth = 20240815
            val category = "Luxury"
            val money = BigDecimal("1234567890123.00") // Exceeds precision

            assertFails {
                expensiveDao.insert(yearsMonth, category, money)
            }
        }
    }

    @Test
    fun `should populate createdAt and updatedAt with non-null values`() {
        transaction {
            val yearsMonth = 20230701
            val category = "Savings"
            val money = BigDecimal("750.00")

            expensiveDao.insert(yearsMonth, category, money)

            val inserted = ExpensiveDao.Expense.selectAll().singleOrNull()
            assertTrue {
                inserted != null &&
                        inserted[ExpensiveDao.Expense.createdAt] != null &&
                        inserted[ExpensiveDao.Expense.updatedAt] != null
            }
        }
    }
}