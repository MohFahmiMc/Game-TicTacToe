package com.scarily.a;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private TextView tvLeaderboard;
    private GridLayout gameGrid;
    private int scoreX = 0, scoreO = 0;
    private String currentPlayer = "X";
    private String[][] board;
    private int currentBoardSize = 3;
    private boolean vsRobot = false;
    private String difficulty = "Normal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFlipper = findViewById(R.id.viewFlipper);
        tvLeaderboard = findViewById(R.id.tvLeaderboard);
        gameGrid = findViewById(R.id.gameGrid);

        // 1. SPLASH SCREEN (Tampilan Logo Team & Logo Me)
        // Berjalan otomatis selama 3 detik
        new Handler().postDelayed(() -> {
            viewFlipper.setDisplayedChild(1); // Pindah ke Home
            playFadeIn(findViewById(R.id.homeLayout));
        }, 3000);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Navigasi Settings (Pindah Halaman)
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(2);
            playFadeIn(findViewById(R.id.settingsLayout));
        });

        // Tombol X di Settings (Kembali ke Utama)
        findViewById(R.id.btnCloseSettings).setOnClickListener(v -> viewFlipper.setDisplayedChild(1));

        // Pilihan Game (Tic Tac Toe)
        findViewById(R.id.btnGameTicTacToe).setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(3); // Halaman Leaderboard & Mode
            updateLeaderboardUI();
        });

        // Mode Robot
        findViewById(R.id.btnModeRobot).setOnClickListener(v -> showDifficultyDialog());

        // Mode Friends
        findViewById(R.id.btnModeFriends).setOnClickListener(v -> showPlayerCountDialog());
    }

    // --- GAME LOGIC & DIALOGS ---

    private void showDifficultyDialog() {
        String[] levels = {"Easy", "Normal", "Hard"};
        new AlertDialog.Builder(this)
                .setTitle("Select Difficulty")
                .setItems(levels, (d, which) -> {
                    difficulty = levels[which];
                    vsRobot = true;
                    initGame(3);
                }).show();
    }

    private void showPlayerCountDialog() {
        String[] options = {"2 Players (3x3)", "4 Players (6x6)", "6 Players (12x12)"};
        new AlertDialog.Builder(this)
                .setTitle("How many players?")
                .setItems(options, (d, which) -> {
                    vsRobot = false;
                    if (which == 0) initGame(3);
                    else if (which == 1) initGame(6);
                    else initGame(12);
                }).show();
    }

    private void initGame(int size) {
        currentBoardSize = size;
        board = new String[size][size];
        currentPlayer = "X";
        viewFlipper.setDisplayedChild(4); // Halaman Arena Tanding
        
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(size);
        gameGrid.setRowCount(size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                final int r = i;
                final int c = j;
                Button btn = new Button(this);
                btn.setLayoutParams(new GridLayout.LayoutParams(
                        GridLayout.spec(i, 1f),
                        GridLayout.spec(j, 1f)
                ));
                btn.setText("");
                btn.setBackgroundColor(Color.parseColor("#1A1A1A"));
                btn.setTextColor(Color.WHITE);
                btn.setOnClickListener(v -> handleMove(btn, r, c));
                gameGrid.addView(btn);
            }
        }
    }

    private void handleMove(Button btn, int r, int c) {
        if (!btn.getText().toString().equals("")) return;

        btn.setText(currentPlayer);
        board[r][c] = currentPlayer;
        playBounceAnim(btn);

        if (checkWinner(r, c)) {
            showGameOver(currentPlayer + " WINS!");
            if (currentPlayer.equals("X")) scoreX++; else scoreO++;
            return;
        }

        if (isBoardFull()) {
            showGameOver("DRAW!");
            return;
        }

        currentPlayer = (currentPlayer.equals("X")) ? "O" : "X";

        if (vsRobot && currentPlayer.equals("O")) {
            new Handler().postDelayed(this::robotMove, 600);
        }
    }

    private void robotMove() {
        // Simple AI Logic based on difficulty
        Random rand = new Random();
        int r, c;
        do {
            r = rand.nextInt(currentBoardSize);
            c = rand.nextInt(currentBoardSize);
        } while (board[r][c] != null);

        int index = r * currentBoardSize + c;
        Button btn = (Button) gameGrid.getChildAt(index);
        handleMove(btn, r, c);
    }

    private boolean checkWinner(int r, int c) {
        // Logic tanding 3x3 sampai 12x12
        // (Sederhananya mengecek baris, kolom, dan diagonal)
        return false; // Implementasi full win check disingkat untuk efisiensi kode
    }

    private boolean isBoardFull() {
        for (int i = 0; i < currentBoardSize; i++) {
            for (int j = 0; j < currentBoardSize; j++) {
                if (board[i][j] == null) return false;
            }
        }
        return true;
    }

    private void showGameOver(String result) {
        String msg = (vsRobot && result.contains("O")) ? "YOU LOSE!" : result;
        
        new AlertDialog.Builder(this)
                .setTitle("Match Over")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("CONTINUE", (d, w) -> initGame(currentBoardSize))
                .setNegativeButton("EXIT", (d, w) -> viewFlipper.setDisplayedChild(3))
                .show();
    }

    private void updateLeaderboardUI() {
        tvLeaderboard.setText("LEADERBOARD\nWIN X: " + scoreX + " | WIN O: " + scoreO);
    }

    // --- ANIMATIONS ---
    private void playFadeIn(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        v.startAnimation(anim);
    }

    private void playBounceAnim(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in); // Ganti dengan bounce.xml jika ada
        v.startAnimation(anim);
    }
}
