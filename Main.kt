package minesweeper

import java.util.InputMismatchException
import kotlin.random.Random
import kotlin.system.exitProcess

class GameBoard(private val mineCount: Int) {
    var gameField: Array<Array<Char>> = Array(9) { Array(9) { '.' } }
    var gameFieldVisibility: Array<Array<Boolean>> = Array(9) { Array(9) { false } }
    var gameFieldMarking: Array<Array<Boolean>> = Array(9) { Array(9) { false } }


    init {
        var x = Random.nextInt(0, 9)
        var y = Random.nextInt(0, 9)
        repeat(mineCount) {
            while (gameField[x][y] == 'X') {
                x = Random.nextInt(0, 9)
                y = Random.nextInt(0, 9)
            }
            gameField[x][y] = 'X'
            x = Random.nextInt(0, 9)
            y = Random.nextInt(0, 9)
        }
        updateBoardWithHints()
    }

    private fun updateBoardWithHints() {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (gameField[i][j] == 'X') {
                    markAroundX(i, j)
                }
            }
        }
    }

    private fun markAroundX(x: Int, y: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                increment(x + i, y + j)
            }
        }
    }

    private fun increment(x: Int, y: Int) {
        if (x !in 0..8 || y !in 0..8) return
        if (gameField[x][y] == '.') gameField[x][y] = '1'
        else if (gameField[x][y] != 'X') gameField[x][y]++
    }

    fun print() {
        println(
            """
 │123456789│
—│—————————│
    """.trimIndent()
        )
        for (i in gameField.indices) {
            print("${i + 1}|")
            for (j in gameField[i].indices) {
                if (gameFieldVisibility[i][j] && gameFieldMarking[i][j]) print('*')
                else if (!gameFieldVisibility[i][j]) print('.')
                else {
                    if (gameField[i][j] == '.') print('/')
                    else print(gameField[i][j])
                }
            }
            print("|")
            println()
        }
        println("—│—————————│")
    }

    private fun hasAllBombs(): Boolean {
        for (i in 0..8) {
            for (j in 0..8) {
                if (gameField[i][j] == 'X' && !gameFieldMarking[i][j]) return false
            }
        }
        return true
    }

    private fun hasAllSafeCells(): Boolean {
        for (i in 0..8) {
            for (j in 0..8) {
                if (gameField[i][j] != 'X' && !gameFieldVisibility[i][j]) return false
            }
        }
        return true
    }

    fun isFinished(): Boolean {
        return hasAllBombs() || hasAllSafeCells()
    }

    enum class Mode {
        FREE, MINE
    }

    private fun free(x: Int, y: Int) {
        if (x !in 0..8 || y !in 0..8) return
        if (gameField[x][y] != 'X') {
            gameFieldVisibility[x][y] = true
            if (gameField[x][y] == '.') {
                gameField[x][y] = '/'
                free(x - 1, y)
                free(x + 1, y)
                free(x, y - 1)
                free(x, y + 1)
                free(x - 1, y - 1)
                free(x + 1, y + 1)
                free(x - 1, y + 1)
                free(x + 1, y - 1)

            }
        }
    }

    private fun updateBoard(x: Int, y: Int) {
        var done = false
        for (i in 0..8) {
            for (j in 0..8) {
                if (gameField[i][j] == 'X') continue
                if (gameField[i][j].isDigit()) gameField[i][j] = '.'
                if (gameField[i][j] == '.' && x != i && j != y && !done) {
                    gameField[i][j] = 'X'
                    done = true
                }
            }
        }
        updateBoardWithHints()
    }

    fun play(input: Pair<Pair<Int, Int>, Mode>) {
        val x = input.first.first
        val y = input.first.second
        if (input.second == Mode.FREE && firstFree) {
            firstFree = false
            if (gameField[x][y] == 'X') {
                gameField[x][y] = '.'
                updateBoard(x, y)
            }
        }

        when (input.second) {
            Mode.FREE -> {
                if (gameField[x][y] == 'X') {
                    for (i in 0..8) {
                        for (j in 0..8) {
                            if (gameField[i][j] == 'X') {
                                gameFieldVisibility[i][j] = true
                            }
                        }
                    }
                    board.print()
                    println("You stepped on a mine and failed!")

                    exitProcess(0)
                } else if (gameField[x][y].isDigit()) {
                    gameFieldVisibility[x][y] = true
                } else {
                    free(x, y)
                }
            }
            Mode.MINE -> {
                gameFieldMarking[x][y] = !gameFieldMarking[x][y]
                gameFieldVisibility[x][y] = !gameFieldVisibility[x][y]
            }
        }
    }
}


var mineCount = 0
var board = GameBoard(0)
var firstFree = true

fun main() {
    println("How many mines do you want on the field?")
    mineCount = readln().toInt()
    board = GameBoard(mineCount)
    board.print()
    var input = getUserInput()
    board.play(input)
    while (!board.isFinished()) {
        checkGrid()
        board.print()
        input = getUserInput()
        board.play(input)
    }
    checkGrid()
    board.print()
    println("Congratulations! You found all the mines!")
}

fun checkGrid() {
    for (i in 0..8) {
        for (j in 0..8) {
            if (board.gameFieldMarking[i][j]) {
                for (k in -1..1) {
                    for (l in -1..1) {
                        val x = i + k
                        val y = j + l
                        if (x in 0..8 && y in 0..8) {
                            if (board.gameField[x][y] == '/') {
                                board.gameFieldMarking[i][j] = false
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getUserInput(): Pair<Pair<Int, Int>, GameBoard.Mode> {
    println("Set/unset mines marks or claim a cell as free:")
    val input = readln().split(" ")
    return Pair(
        Pair(input[1].toInt() - 1, input[0].toInt() - 1),
        if (input.last() == "free") GameBoard.Mode.FREE else GameBoard.Mode.MINE
    )

}



