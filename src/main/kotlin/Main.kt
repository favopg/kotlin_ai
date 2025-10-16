package org.example

import ExpensiveDao

fun createTable() {
    val expensiveDao = ExpensiveDao()
    expensiveDao.create()
    
    // TODO: Implementation goes here
}

fun main() {
    println("家計簿アプリ起動")
    // Start simple HTTP server on port 8080 and handle /api/init
    val expenseController = ExpenseController()
    expenseController.createServer()

    //createInput()
}
fun createInput() {
    while (true) {

        print("Enter input (type 'exit' to quit): ")
        val input = readln()
        if (input.equals("exit", ignoreCase = true)) {
            println("Exiting the application...")
            break
        } else {
            println("You entered: $input")
            createTable()
            
        }
    }
}



