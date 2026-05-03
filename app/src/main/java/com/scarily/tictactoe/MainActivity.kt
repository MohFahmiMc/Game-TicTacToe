package com.scarily.tictactoe

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
    private lateinit var tvTurnStatus: TextView
    private lateinit var gameGrid: GridLayout
    private lateinit var splashLayout: RelativeLayout
    
    private val playerSymbols = arrayOf("1", "2", "3", "4", "5", "6")
    private val playerColors = arrayOf("#00FFCC", "#FF4444", "#FFBB33", "#99CC00", "#AA66CC", "#33B5E5")
    
    private var playerScores = IntArray(6) { 0 }
    private lateinit var scoreTextViews: Array<TextView>
    
    private var currentPlayerIndex = 0
    private var totalPlayers = 2
    private lateinit var board: Array<Array<String?>>
    private var currentBoardSize = 3
    private var vsRobot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi View
        viewFlipper = findViewById(R.id.viewFlipper)
        tvTurnStatus = findViewById(R.id.tvTurnStatus)
        gameGrid = findViewById(R.id.gameGrid)
        splashLayout = findViewById(R.id.splashLayout)
        
        scoreTextViews = arrayOf(
            findViewById(R.id.score_p1), findViewById(R.id.score_p2),
            findViewById(R.id.score_p3), findViewById(R.id.score_p4),
            findViewById(R.id.score_p5), findViewById(R.id.score_p6)
        )

        loadScores()
        startSplashScreen()
        setupNavigation()
    }

    private fun startSplashScreen() {
        val imgTeam = findViewById<ImageView>(R.id.imgLogoTeam)
        val imgMe = findViewById<ImageView>(R.id.imgLogoMe)
        
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
                        viewFlipper.displayedChild = 1 // Masuk ke Home
                    }, 1000)
                }.start()
            }, 1000)
        }.start()
    }

    private fun setupNavigation() {
        // Navigasi Utama
        findViewById<Button>(R.id.btnGameTicTacToe).setOnClickListener { 
            viewFlipper.displayedChild = 3 // Pergi ke PILIH MODE
        }
        
        findViewById<Button>(R.id.btnExitApp).setOnClickListener { finishAffinity() }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { viewFlipper.displayedChild = 2 }
        findViewById<Button>(R.id.btnCloseSettings).setOnClickListener { viewFlipper.displayedChild = 1 }

        // Social Media
        findViewById<ImageButton>(R.id.btnGithub).setOnClickListener { openUrl("https://github.com/MohFahmiMc") }
        findViewById<ImageButton>(R.id.btnWeb).setOnClickListener { openUrl("https://mifahmi.vercel.app/") }

        // LOGIKA PEMILIHAN MODE (DI HALAMAN CHILD 3)
        findViewById<Button>(R.id.btnModeRobot).setOnClickListener { 
            vsRobot = true
            totalPlayers = 2
            initGame(3) // Robot selalu 3x3
        }

        findViewById<Button>(R.id.btnModeFriends).setOnClickListener { 
            vsRobot = false
            showPlayerCountOverlay() // Panggil overlay pemilihan jumlah pemain
        }

        // Kontrol Game
        findViewById<ImageButton>(R.id.btnResetScore).setOnClickListener {
            for (i in 0..5) playerScores[i] = 0
            saveScores()
            updateLeaderboardUI()
            Toast.makeText(this, "Skor direset!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.btnExitGame).setOnClickListener { 
            viewFlipper.displayedChild = 1 
        }
    }

    private fun showPlayerCountOverlay() {
        val dialogOverlay = findViewById<RelativeLayout>(R.id.dialogPlayerCount)
        dialogOverlay.visibility = View.VISIBLE
        dialogOverlay.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

        // Set Click Listener di dalam overlay agar tidak tumpang tindih
        findViewById<Button>(R.id.btnOpt2).setOnClickListener {
            dialogOverlay.visibility = View.GONE
            totalPlayers = 2
            initGame(3)
        }

        findViewById<Button>(R.id.btnOpt4).setOnClickListener {
            dialogOverlay.visibility = View.GONE
            totalPlayers = 4
            initGame(6)
        }

        findViewById<Button>(R.id.btnOpt6).setOnClickListener {
            dialogOverlay.visibility = View.GONE
            totalPlayers = 6
            initGame(10)
        }
    }

    private fun initGame(size: Int) {
        currentBoardSize = size
        board = Array(size) { arrayOfNulls<String>(size) }
        currentPlayerIndex = 0
        
        // PINDAH KE LAYAR GAME (CHILD 4)
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
                    params.setMargins(1, 1, 1, 1)
                    layoutParams = params
                    
                    text = ""
                    textSize = if (size >= 10) 10f else 18f
                    setBackgroundColor(Color.BLACK)
                    setOnClickListener { handleMove(this, i, j) }
                }
                gameGrid.addView(btn)
            }
        }
        updateTurnUI()
    }

    // --- LOGIKA PERMAINAN (checkWin, handleMove, robotMove tetap sama) ---
    // ... (Gunakan logika checkWin diagonal yang saya berikan sebelumnya agar tidak bug)

    private fun handleMove(btn: Button, r: Int, c: Int) {
        if (btn.text.isNotEmpty()) return

        val symbol = playerSymbols[currentPlayerIndex]
        val color = Color.parseColor(playerColors[currentPlayerIndex])

        btn.text = symbol
        btn.setTextColor(color)
        board[r][c] = symbol
        btn.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

        if (checkWin(r, c)) {
            playerScores[currentPlayerIndex]++
            saveScores()
            updateLeaderboardUI()
            showGameOver("Pemain $symbol Menang!")
            return
        }

        if (isFull()) {
            showGameOver("Pertandingan Seri!")
            return
        }

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
            val childBtn = gameGrid.getChildAt(index) as? Button
            childBtn?.let { handleMove(it, move.first, move.second) }
        }
    }

    private fun checkWin(r: Int, c: Int): Boolean {
        val symbol = board[r][c] ?: return false
        val winCondition = if (currentBoardSize >= 6) 4 else 3 

        fun checkDirection(dr: Int, dc: Int): Int {
            var count = 0
            var currR = r + dr
            var currC = c + dc
            while (currR in 0 until currentBoardSize && currC in 0 until currentBoardSize && board[currR][currC] == symbol) {
                count++
                currR += dr
                currC += dc
            }
            return count
        }

        if (checkDirection(0, 1) + checkDirection(0, -1) + 1 >= winCondition) return true
        if (checkDirection(1, 0) + checkDirection(-1, 0) + 1 >= winCondition) return true
        if (checkDirection(1, 1) + checkDirection(-1, -1) + 1 >= winCondition) return true
        if (checkDirection(1, -1) + checkDirection(-1, 1) + 1 >= winCondition) return true

        return false
    }

    private fun showGameOver(result: String) {
        val gameOverLayout = findViewById<RelativeLayout>(R.id.gameOverLayout)
        val tvWinnerResult = findViewById<TextView>(R.id.tvWinnerResult)
        
        tvWinnerResult.text = result
        gameOverLayout.visibility = View.VISIBLE
        gameOverLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))

        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            gameOverLayout.visibility = View.GONE
            initGame(currentBoardSize)
        }

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener {
            gameOverLayout.visibility = View.GONE
            viewFlipper.displayedChild = 1
        }
    }

    private fun updateTurnUI() {
        tvTurnStatus.text = "Giliran: P${playerSymbols[currentPlayerIndex]}"
        tvTurnStatus.setTextColor(Color.parseColor(playerColors[currentPlayerIndex]))
    }

    private fun isFull(): Boolean = board.all { row -> row.all { it != null } }

    private fun updateLeaderboardUI() {
        for (i in 0..5) {
            scoreTextViews[i].text = "P${i + 1}: ${playerScores[i]}"
        }
    }

    private fun saveScores() {
        val sharedPref = getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            for (i in 0..5) putInt("SCORE_P${i+1}", playerScores[i])
            apply()
        }
    }

    private fun loadScores() {
        val sharedPref = getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        for (i in 0..5) {
            playerScores[i] = sharedPref.getInt("SCORE_P${i+1}", 0)
        }
        updateLeaderboardUI()
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
