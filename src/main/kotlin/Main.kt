package org.example

fun main() {
    println("Hello, world!")
    createInput()
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
        }
    }
}

