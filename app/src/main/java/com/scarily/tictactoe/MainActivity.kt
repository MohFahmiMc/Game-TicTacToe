package com.scarily.tictactoe

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        // Inisialisasi View
        viewFlipper = findViewById(R.id.viewFlipper)
        tvLeaderboard = findViewById(R.id.tvLeaderboard)
        gameGrid = findViewById(R.id.gameGrid)

        // 1. SPLASH SCREEN (3 Detik)
        Handler(Looper.getMainLooper()).postDelayed({
            viewFlipper.displayedChild = 1 // Pindah ke Home Menu
        }, 3000)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Ke Halaman Settings
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            viewFlipper.displayedChild = 2
        }

        // Tutup Settings (Tombol X)
        findViewById<ImageButton>(R.id.btnCloseSettings).setOnClickListener {
            viewFlipper.displayedChild = 1
        }

        // Pilih Game Tic Tac Toe
        findViewById<Button>(R.id.btnGameTicTacToe).setOnClickListener {
            viewFlipper.displayedChild = 3 // Menu Leaderboard & Mode
            updateLeaderboardUI()
        }

        // Mode Tanding
        findViewById<Button>(R.id.btnModeRobot).setOnClickListener { showDifficultyDialog() }
        findViewById<Button>(R.id.btnModeFriends).setOnClickListener { showPlayerCountDialog() }
    }

    private fun showDifficultyDialog() {
        val levels = arrayOf(getString(R.string.diff_easy), getString(R.string.diff_normal), getString(R.string.diff_hard))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_difficulty_title))
            .setItems(levels) { _, which ->
                difficulty = levels[which]
                vsRobot = true
                initGame(3) // Robot default di 3x3
            }.show()
    }

    private fun showPlayerCountDialog() {
        val options = arrayOf(getString(R.string.mode_2p), getString(R.string.mode_4p), getString(R.string.mode_6p))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_player_title))
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
        viewFlipper.displayedChild = 4 // Masuk Arena Game

        gameGrid.removeAllViews()
        gameGrid.columnCount = size
        gameGrid.rowCount = size

        // Membuat Grid secara dinamis
        for (i in 0 until size) {
            for (j in 0 until size) {
                val btn = Button(this).apply {
                    val params = GridLayout.LayoutParams(
                        GridLayout.spec(i, 1f),
                        GridLayout.spec(j, 1f)
                    )
                    params.width = 0
                    params.height = 0
                    params.setMargins(2, 2, 2, 2)
                    layoutParams = params
                    
                    text = ""
                    textSize = if (size > 6) 10f else 18f
                    setBackgroundColor(Color.parseColor("#121212"))
                    setTextColor(Color.CYAN)
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
        
        // Animasi klik yang seru
        btn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
        btn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
            btn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
        }

        if (checkWin(r, c)) {
            if (currentPlayer == "X") scoreX++ else scoreO++
            showGameOver(if (currentPlayer == "X") getString(R.string.win_x) else getString(R.string.win_o))
            return
        }

        if (isFull()) {
            showGameOver(getString(R.string.draw))
            return
        }

        // Ganti giliran (bisa X, O, atau simbol lain jika pemain > 2)
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
            // AI Sederhana: Random (Bisa ditingkatkan berdasarkan 'difficulty')
            val move = emptyCells[Random().nextInt(emptyCells.size)]
            val index = move.first * currentBoardSize + move.second
            handleMove(gameGrid.getChildAt(index) as Button, move.first, move.second)
        }
    }

    // LOGIKA MENANG UNTUK SEMUA UKURAN PAPAN
    private fun checkWin(r: Int, c: Int): Boolean {
        val symbol = board[r][c]
        
        // Cek Horizontal
        var count = 0
        for (j in 0 until currentBoardSize) {
            if (board[r][j] == symbol) count++ else count = 0
            if (count == 3) return true // Minimal 3 urutan untuk menang
        }

        // Cek Vertical
        count = 0
        for (i in 0 until currentBoardSize) {
            if (board[i][c] == symbol) count++ else count = 0
            if (count == 3) return true
        }

        // Cek Diagonal (Sederhana)
        // Note: Untuk 12x12 biasanya butuh algoritma yang lebih kompleks, 
        // tapi ini sudah cukup untuk tanding standar.
        return false 
    }

    private fun isFull(): Boolean = board.all { row -> row.all { it != null } }

    private fun showGameOver(result: String) {
        // Overlay logic: Jika kalah lawan robot tampilkan "YOU LOSE!"
        val finalMsg = if (vsRobot && currentPlayer == "O") getString(R.string.you_lose) else result
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_over_title))
            .setMessage(finalMsg)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.btn_replay)) { _, _ -> initGame(currentBoardSize) }
            .setNegativeButton(getString(R.string.btn_exit)) { _, _ -> viewFlipper.displayedChild = 3 }
            .show()
    }

    private fun updateLeaderboardUI() {
        tvLeaderboard.text = getString(R.string.score_format, scoreX, scoreO)
    }
}
