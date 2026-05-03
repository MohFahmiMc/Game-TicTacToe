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
import androidx.appcompat.app.AppCompatDelegate
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

        // Inisialisasi View
        viewFlipper = findViewById(R.id.viewFlipper)
        tvLeaderboard = findViewById(R.id.tvLeaderboard)
        gameGrid = findViewById(R.id.gameGrid)
        
        val imgTeam = findViewById<ImageView>(R.id.imgLogoTeam)
        val imgMe = findViewById<ImageView>(R.id.imgLogoMe)

        // 1. SPLASH SCREEN ANIMASI (Logo Team -> Logo Me -> Home)
        imgTeam.alpha = 0f
        imgTeam.animate().alpha(1f).setDuration(1000).withEndAction {
            Handler(Looper.getMainLooper()).postDelayed({
                imgTeam.visibility = View.GONE
                imgMe.visibility = View.VISIBLE
                imgMe.alpha = 0f
                imgMe.animate().alpha(1f).setDuration(1000).withEndAction {
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewFlipper.displayedChild = 1 // Pindah ke Home Menu
                    }, 1000)
                }.start()
            }, 1000)
        }.start()

        setupNavigation()
    }

    private fun setupNavigation() {
        // Ke Halaman Settings
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            viewFlipper.displayedChild = 2
        }

        // Tutup Settings
        findViewById<ImageButton>(R.id.btnCloseSettings).setOnClickListener {
            viewFlipper.displayedChild = 1
        }

        // Ganti Tema
        findViewById<Button>(R.id.btnChangeTheme)?.setOnClickListener {
            val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // Play Button di Home
        findViewById<Button>(R.id.btnGameTicTacToe).setOnClickListener {
            viewFlipper.displayedChild = 3 // Ke Halaman Pilih Mode
            updateLeaderboardUI()
        }

        // Pilih Mode
        findViewById<Button>(R.id.btnModeRobot).setOnClickListener { showDifficultyDialog() }
        findViewById<Button>(R.id.btnModeFriends).setOnClickListener { showPlayerCountDialog() }
        
        // Tombol Exit di dalam Game
        findViewById<Button>(R.id.btnExitGame).setOnClickListener {
            viewFlipper.displayedChild = 3
        }
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
        viewFlipper.displayedChild = 4 

        gameGrid.removeAllViews()
        gameGrid.columnCount = size
        gameGrid.rowCount = size

        for (i in 0 until size) {
            for (j in 0 until size) {
                val btn = Button(this).apply {
                    val params = GridLayout.LayoutParams(
                        GridLayout.spec(i, 1f),
                        GridLayout.spec(j, 1f)
                    )
                    params.width = 0
                    params.height = 0
                    params.setMargins(4, 4, 4, 4)
                    layoutParams = params
                    
                    text = ""
                    textSize = if (size == 12) 10f else if (size == 6) 16f else 28f
                    setBackgroundColor(Color.parseColor("#222222"))
                    setTextColor(Color.parseColor("#00FFCC"))
                    // Membuat tombol game agak melengkung
                    stateListAnimator = null 
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
        
        // Animasi saat tombol ditekan
        btn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
        btn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
            btn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
        }

        if (checkWin(r, c)) {
            if (currentPlayer == "X") scoreX++ else scoreO++
            showGameOver("Winner: $currentPlayer")
            return
        }

        if (isFull()) {
            showGameOver("Draw!")
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
        val symbol = board[r][c]
        val winCondition = 3 
        
        var count = 0
        for (j in 0 until currentBoardSize) {
            if (board[r][j] == symbol) count++ else count = 0
            if (count == winCondition) return true
        }
        count = 0
        for (i in 0 until currentBoardSize) {
            if (board[i][c] == symbol) count++ else count = 0
            if (count == winCondition) return true
        }
        return checkDiagonals(symbol, winCondition)
    }

    private fun checkDiagonals(s: String?, win: Int): Boolean {
        for (i in 0..currentBoardSize - win) {
            for (j in 0..currentBoardSize - win) {
                var count = 0
                for (k in 0 until win) if (board[i+k][j+k] == s) count++
                if (count == win) return true
            }
        }
        for (i in 0..currentBoardSize - win) {
            for (j in win - 1 until currentBoardSize) {
                var count = 0
                for (k in 0 until win) if (board[i+k][j-k] == s) count++
                if (count == win) return true
            }
        }
        return false
    }

    private fun isFull(): Boolean = board.all { row -> row.all { it != null } }

    private fun showGameOver(result: String) {
        updateLeaderboardUI()
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage(result)
            .setCancelable(false)
            .setPositiveButton("Replay") { _, _ -> initGame(currentBoardSize) }
            .setNegativeButton("Exit") { _, _ -> viewFlipper.displayedChild = 3 }
            .show()
    }

    private fun updateLeaderboardUI() {
        tvLeaderboard.text = "WIN X: $scoreX | WIN O: $scoreO"
    }
}
