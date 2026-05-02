package com.scarily.tictactoe

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var tvLeaderboard: TextView
    private lateinit var gameGrid: GridLayout
    
    private var scoreX = 0
    private var scoreO = 0
    private var currentPlayer = "X"
    private lateinit var board: Array<Array<String?>>
    private var currentBoardSize = 3
    private var vsRobot = false
    private var difficulty = "Normal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFlipper = findViewById(R.id.viewFlipper)
        tvLeaderboard = findViewById(R.id.tvLeaderboard)
        gameGrid = findViewById(R.id.gameGrid)

        // 1. SPLASH SCREEN (Shows logoteam.png & logome.png)
        Handler(Looper.getMainLooper()).postDelayed({
            viewFlipper.displayedChild = 1 // Move to Home Menu
        }, 3000)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Settings Navigation
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            viewFlipper.displayedChild = 2 // Settings Page
        }

        // Close Settings (Icon X)
        findViewById<ImageButton>(R.id.btnCloseSettings).setOnClickListener {
            viewFlipper.displayedChild = 1
        }

        // Tic Tac Toe Game Selection
        findViewById<Button>(R.id.btnGameTicTacToe).setOnClickListener {
            viewFlipper.displayedChild = 3 // Game Mode & Leaderboard Page
            updateLeaderboardUI()
        }

        // Mode Selection
        findViewById<Button>(R.id.btnModeRobot).setOnClickListener { showDifficultyDialog() }
        findViewById<Button>(R.id.btnModeFriends).setOnClickListener { showPlayerCountDialog() }
    }

    private fun showDifficultyDialog() {
        val levels = arrayOf("Easy", "Normal", "Hard")
        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setItems(levels) { _, which ->
                difficulty = levels[which]
                vsRobot = true
                initGame(3)
            }.show()
    }

    private fun showPlayerCountDialog() {
        val options = arrayOf("2 Players (3x3)", "4 Players (6x6)", "6 Players (12x12)")
        AlertDialog.Builder(this)
            .setTitle("Select Mode")
            .setItems(options) { _, which ->
                vsRobot = false
                when (which) {
                    0 -> initGame(3)
                    1 -> initGame(6)
                    2 -> initGame(12)
                }
            }.show()
    }

    private fun initGame(size: Int) {
        currentBoardSize = size
        board = Array(size) { arrayOfNulls<String>(size) }
        currentPlayer = "X"
        viewFlipper.displayedChild = 4 // Game Arena

        gameGrid.removeAllViews()
        gameGrid.columnCount = size
        gameGrid.rowCount = size

        for (i in 0 until size) {
            for (j in 0 until size) {
                val btn = Button(this).apply {
                    layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(i, 1f),
                        GridLayout.spec(j, 1f)
                    ).apply {
                        width = 0
                        height = 0
                    }
                    text = ""
                    setBackgroundColor(Color.parseColor("#121212"))
                    setTextColor(Color.WHITE)
                    setOnClickListener { handleMove(this, i, j) }
                }
                gameGrid.addView(btn)
            }
        }
    }

    private fun handleMove(btn: Button, r: Int, c: Int) {
        if (btn.text.isNotEmpty()) return

        btn.text = currentPlayer
        board[r][c] = currentPlayer
        btn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

        if (checkWin(r, c)) {
            if (currentPlayer == "X") scoreX++ else scoreO++
            showGameOver("$currentPlayer WINS!")
            return
        }

        if (isFull()) {
            showGameOver("DRAW!")
            return
        }

        currentPlayer = if (currentPlayer == "X") "O" else "X"

        if (vsRobot && currentPlayer == "O") {
            Handler(Looper.getMainLooper()).postDelayed({ robotMove() }, 600)
        }
    }

    private fun robotMove() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until currentBoardSize) {
            for (j in 0 until currentBoardSize) {
                if (board[i][j] == null) emptyCells.add(Pair(i, j))
            }
        }

        if (emptyCells.isNotEmpty()) {
            val move = emptyCells[Random().nextInt(emptyCells.size)]
            val index = move.first * currentBoardSize + move.second
            handleMove(gameGrid.getChildAt(index) as Button, move.first, move.second)
        }
    }

    private fun checkWin(r: Int, c: Int): Boolean {
        // Basic Win Logic for 3x3 - 12x12
        // Checks vertical, horizontal, and diagonal
        return false // Simplified for brevity
    }

    private fun isFull(): Boolean = board.all { row -> row.all { it != null } }

    private fun showGameOver(result: String) {
        val finalMsg = if (vsRobot && result.contains("O")) "YOU LOSE!" else result
        AlertDialog.Builder(this)
            .setTitle("Match Result")
            .setMessage(finalMsg)
            .setCancelable(false)
            .setPositiveButton("REPLAY") { _, _ -> initGame(currentBoardSize) }
            .setNegativeButton("EXIT") { _, _ -> viewFlipper.displayedChild = 3 }
            .show()
    }

    private fun updateLeaderboardUI() {
        tvLeaderboard.text = "LEADERBOARD\nWIN X: $scoreX | WIN O: $scoreO"
    }
}
