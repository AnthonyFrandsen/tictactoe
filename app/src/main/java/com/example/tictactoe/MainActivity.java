package com.example.tictactoe;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    /*
    Class for a space on the tic tac toe game board. It acts as a wrapper for a button
     */
    private class GameBoardSpace {
        public final Button boardButton;
        private String owner;
        private boolean winner;

        public GameBoardSpace(Button boardButton) {
            this.boardButton = boardButton;
            this.owner = ""; //default, and so that we don't compare to a null value in setOwner
            String boardButtonText = boardButton.getText().toString();
            if (!boardButtonText.equals("")) {
                this.setOwner(boardButtonText);
            }
        }

        //Getter and setter for owner
        public String getOwner() {
            return owner;
        }
        public boolean setOwner(String newOwner) {
            //Making sure that the new owner is a player, we let that player claim the space
            //and then lock it
            if (!newOwner.equals("") && this.owner.equals("")){
                this.owner = newOwner;
                this.boardButton.setText(this.owner);
                this.lock();
                return true;
            }
            else {
                return false;
            }
        }

        //Clearing all owners and unlocking the space
        public void clear() {
            this.owner = "";
            this.winner = false;
            this.boardButton.setText("");
            this.boardButton.setEnabled(true);
            this.boardButton.setBackgroundColor(
                    getResources().getColor(R.color.design_default_color_primary));
        }

        //Highlight a winning square
        public void markWinner() {
            this.winner = true;
            this.boardButton.setBackgroundColor(
                    getResources().getColor(R.color.design_default_color_secondary));
        }

        //Lock a space so that it can't be claimed
        public void lock() {
            this.boardButton.setEnabled(false);
            //Change the color unless it's a winning square
            if (!this.winner) {
                this.boardButton.setBackgroundColor(
                        getResources().getColor(R.color.design_default_color_primary_dark));
            }
        }
    }

    /*
    Class for the tic tac toe game board. It serves as the wrapper and controller for the nine
    board spaces, the new game button, and the text display.
     */
    private class GameBoard {
        private final GameBoardSpace[] spaces;
        private final TextView gameDisplay;
        private final Button newGameButton;
        private String currentTurn;

        public GameBoard(GameBoardSpace[] spaces, TextView gameDisplay, Button newGameButton) {
            this.spaces = spaces;
            this.gameDisplay = gameDisplay;
            this.newGameButton = newGameButton;
        }

        private boolean checkIfAllMatch(int[] indexes) {
            //Test if all of the board spaces in the
            //passed array of indexes belong to the same player

            //Assume true and break when false
            boolean allMatch = true;
            String previousOwner = spaces[indexes[0]].getOwner();
            for (int index : indexes) {
                if (spaces[index].getOwner().equals("") ||
                        !spaces[index].getOwner().equals(previousOwner)) {
                    allMatch = false;
                    break;
                }
                previousOwner = spaces[index].getOwner();
            }
            return allMatch;
        }

        private boolean checkForWin() {
            //This two dimensional array represents the rows, columns, and diagonals
            final int[][] lines = {
                    {0, 1, 2},
                    {3, 4, 5},
                    {6, 7, 8},
                    {0, 3, 6},
                    {1, 4, 7},
                    {2 ,5 ,8},
                    {0, 4, 8},
                    {2, 4, 6}
            };

            //Check the lines to see a player owns one

            //Assume false, prove otherwise
            boolean winnerFound = false;
            for (int[] line: lines) {
                if (checkIfAllMatch(line)) {
                    for (int space: line) {
                        this.spaces[space].markWinner();
                    }
                    //If O won
                    if (this.spaces[line[0]].getOwner().equals(getString(R.string.O))) {
                        gameDisplay.setText(getString(R.string.o_wins));
                    }
                    //If X won
                    if (this.spaces[line[0]].getOwner().equals(getString(R.string.X))) {
                        gameDisplay.setText(getString(R.string.x_wins));
                    }
                    //Lock all spaces
                    for (GameBoardSpace space: spaces) {
                        space.lock();
                    }
                    winnerFound = true;
                }
            }
            return winnerFound;
        }

        private boolean checkForTie() {
            //Check to see if all spaces are taken

            //Assume true, break when false
            boolean allSpacesTaken = true;
            for (GameBoardSpace space : spaces) {
                if (space.getOwner().equals("")) {
                    allSpacesTaken = false;
                    break;
                }
            }
            if (allSpacesTaken) {
                gameDisplay.setText(getString(R.string.tie_game));
            }
            return allSpacesTaken;
        }

        public void markSpace(GameBoardSpace space) {
            //Try to mark a space, and check the board afterwards if successful
            if (space.setOwner(this.currentTurn)) {
                if (!this.checkForWin() && !this.checkForTie()) {
                    this.changeTurn();
                }
            }
        }

        private void changeTurn() {
            if (this.currentTurn.equals(getString(R.string.X))) {
                this.currentTurn = getString(R.string.O);
                gameDisplay.setText(getString(R.string.o_turn));
            }
            else {
                this.currentTurn = getString(R.string.X);
                gameDisplay.setText(getString(R.string.x_turn));
            }
        }

        private void reset() {
            //Clear all board spaces and set it to X's turn
            for (GameBoardSpace space: this.spaces) {
                space.clear();
            }
            this.currentTurn = getString(R.string.X);
            gameDisplay.setText(getString(R.string.x_turn));
        }

        /*
        After being created, the game board must be initialized by adding event handlers to the
        game board buttons and the new game button.
         */
        public void initialize() {
            GameBoard parent = this;
            for (GameBoardSpace space : this.spaces) {
                space.boardButton.setOnClickListener(v -> parent.markSpace(space));
            }
            this.newGameButton.setOnClickListener(v -> parent.reset());
        }
    }

    private GameBoard board;
    private SharedPreferences storedValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storedValues = getSharedPreferences("SharedValues", MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = storedValues.edit();

        //Store the values being used by the board
        for (int i = 0; i < board.spaces.length; i++) {
            editor.putString("btn" + i, board.spaces[i].boardButton.getText().toString());
        }
        editor.putString("display", board.gameDisplay.getText().toString());
        editor.putString("currentTurn", board.currentTurn);
        editor.apply();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Get rid of the title bar in landscape mode
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Objects.requireNonNull(getSupportActionBar()).hide();
        }

        //The board is lost on pause, so create a new one
        board = createGameBoard();
        board.initialize();

        //Use the stored values to recreate the old board
        board.gameDisplay.setText(storedValues.getString("display", ""));
        board.currentTurn = storedValues.getString("currentTurn", getString(R.string.X));
        for (int i = 0; i < 9; i++) {
            board.spaces[i].setOwner(storedValues.getString("btn" + i, ""));
        }
        board.checkForWin();
    }

    /*
    Find all the views and link them into a game board
     */
    private GameBoard createGameBoard() {
        return new GameBoard(
                new GameBoardSpace[]{
                        new GameBoardSpace(findViewById(R.id.button0)),
                        new GameBoardSpace(findViewById(R.id.button1)),
                        new GameBoardSpace(findViewById(R.id.button2)),
                        new GameBoardSpace(findViewById(R.id.button3)),
                        new GameBoardSpace(findViewById(R.id.button4)),
                        new GameBoardSpace(findViewById(R.id.button5)),
                        new GameBoardSpace(findViewById(R.id.button6)),
                        new GameBoardSpace(findViewById(R.id.button7)),
                        new GameBoardSpace(findViewById(R.id.button8))
                },
                findViewById(R.id.gameMessageTextView),
                findViewById(R.id.newGameButton)
        );
    }
}