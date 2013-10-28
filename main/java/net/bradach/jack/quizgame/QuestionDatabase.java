package net.bradach.jack.quizgame;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by jack on 27/10/13.
 */
public class QuestionDatabase {
    private QuestionDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public final static String TABLE_QUESTIONS = "Questions";

    public final static String QUESTIONS_ID = "id";
    public final static String QUESTIONS_QUESTION = "question";
    public final static String QUESTIONS_ANSWER = "answer_correct";

    /* Populate the database from a text file located in the
    * filesystem.  It is expected to be in the format:
    * <START OF FILE>
    * Question
    * Correct Answer
    * Incorrect Answer A
    * Incorrect Answer B or NULL
    * Incorrect Answer C or NULL
    * ---
    * <NEXT QUESTION OR END OF FILE>
    */
    public void loadDatabase(File dbFile) {


    }


}
