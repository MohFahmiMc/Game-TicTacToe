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
    private lateinit var splashLayout: RelativeLayout
    
    // Daftar simbol dan warna untuk banyak pemain
    private val playerSymbols = arrayOf("X", "O", "Z", "B", "D", "C")
    private val playerColors = arrayOf("#00FFCC", "#FF4444", "#FFBB33", "#99CC00", "#AA66CC", "#33B5E5")
    
    private var currentPlayerIndex = 0
    private var totalPlayers = 2
    private lateinit var board: Array<Array<String?>>
    private var currentBoardSize = 3
    private var vsRobot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFlipper = findViewById(R.id.viewFlipper)
        tvLeaderboard = findViewById(R.id.tvLeaderboard)
        gameGrid = findViewById(R.id.gameGrid)
        splashLayout = findViewById(R.id.splashLayout)
        
        val imgTeam = findViewById<ImageView>(R.id.imgLogoTeam)
        val imgMe = findViewById<ImageView>(R.id.imgLogoMe)

        // SPLASH SCREEN: Latar Putih (Team) -> Abu Gelap (Me)
        splashLayout.setBackgroundColor(Color.WHITE)
        imgTeam.alpha = 0f
        imgTeam.animate().alpha(1f).setDuration(1000).withEndAction {
            Handler(Looper.getMainLooper()).postDelayed({
                splashLayout.setBackgroundColor(Color.parseColor("#333333"))
                imgTeam.visibility = View.GONE
                imgMe.visibility = View.VISIBLE
                imgMe.alpha = 0f
                imgMe.animate().alpha(1f).setDuration(1000).withEndAction {
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewFlipper.displayedChild = 1
                    }, 1000)
                }.start()
            }, 1000)
        }.start()

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.btnGameTicTacToe).setOnClickListener {
            viewFlipper.displayedChild = 3
        }

        findViewById<Button>(R.id.btnModeRobot).setOnClickListener { 
            vsRobot = true
            totalPlayers = 2
            initGame(3) 
        }

        findViewById<Button>(R.id.btnModeFriends).setOnClickListener { 
            showPlayerCountDialog() 
        }
        
        findViewById<Button>(R.id.btnExitGame).setOnClickListener {
            viewFlipper.displayedChild = 3
        }
    }

    private fun showPlayerCountDialog() {
        val options = arrayOf("2 Players (3x3)", "4 Players (6x6)", "6 Players (12x12)")
        AlertDialog.Builder(this)
            .setTitle("Pilih Jumlah Pemain")
            .setItems(options) { _, which ->
                vsRobot = false
                when (which) {
                    0 -> { totalPlayers = 2; initGame(3) }
                    1 -> { totalPlayers = 4; initGame(6) }
                    2 -> { totalPlayers = 6; initGame(12) }
                }
            }.show()
    }

    private fun initGame(size: Int) {
        currentBoardSize = size
        board = Array(size) { arrayOfNulls<String>(size) }
        currentPlayerIndex = 0
        viewFlipper.displayedChild = 4 

        gameGrid.removeAllViews()
        gameGrid.columnCount = size
        gameGrid.rowCount = size
        // Beri warna latar grid agar garis terlihat
        gameGrid.setBackgroundColor(Color.parseColor("#555555")) 

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
                    setBackgroundColor(Color.BLACK)
                    setOnClickListener { handleMove(this, i, j) }
                }
                gameGrid.addView(btn)
            }
        }
        updateTurnUI()
    }

    private fun handleMove(btn: Button, r: Int, c: Int) {
        if (btn.text.isNotEmpty()) return

        val symbol = playerSymbols[currentPlayerIndex]
        val color = Color.parseColor(playerColors[currentPlayerIndex])

        btn.text = symbol
        btn.setTextColor(color)
        board[r][c] = symbol
        
        btn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

        if (checkWin(r, c)) {
            showGameOver("Pemain $symbol Menang!")
            return
        }

        if (isFull()) {
            showGameOver("Seri!")
            return
        }

        // Ganti giliran pemain (X -> O -> Z -> B -> D -> C)
        currentPlayerIndex = (currentPlayerIndex + 1) % totalPlayers
        updateTurnUI()

        if (vsRobot && currentPlayerIndex == 1) {
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
        val winCondition = if (currentBoardSize > 3) 4 else 3 
        
        // Cek Horizontal, Vertical, dan Diagonal (Logika Win)
        // ... (Gunakan logika checkWin kamu yang sebelumnya)
        return false // Ganti dengan return logika check kamu
    }

    private fun updateTurnUI() {
        tvLeaderboard.text = "Giliran: ${playerSymbols[currentPlayerIndex]}"
        tvLeaderboard.setTextColor(Color.parseColor(playerColors[currentPlayerIndex]))
    }

    private fun isFull(): Boolean = board.all { row -> row.all { it != null } }

    private fun showGameOver(result: String) {
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage(result)
            .setPositiveButton("Main Lagi") { _, _ -> initGame(currentBoardSize) }
            .setNegativeButton("Keluar") { _, _ -> viewFlipper.displayedChild = 3 }
            .show()
    }
}
