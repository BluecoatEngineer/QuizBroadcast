package net.bradach.jack.quizgame;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * QuestionDatabase provides a wrapper to the underlying SQLite database in
 * which questions are stored.  It essentially acts as a Question to Database
 * interface.
 */
final public class QuestionDatabase {
    /* We can only have one instance of QuestionDatabase */
    private static QuestionDatabase instance = null;

    /* Handle to the underlying database. */
    private SQLiteDatabase database;

    /* Constants */
    private static final String TAG = "QuestionDatabase";
    private final static String TABLE_QUESTIONS = "Questions";
    private final static String QUESTIONS_QUESTION = "question";
    private final static String QUESTIONS_RESPONSE_CORRECT = "response_correct";
    private final static String QUESTIONS_RESPONSE_WRONG_A = "response_wrong_a";
    private final static String QUESTIONS_RESPONSE_WRONG_B = "response_wrong_b";
    private final static String QUESTIONS_RESPONSE_WRONG_C = "response_wrong_c";

    /* Constructor which uses the helper class to get our database handle. */
    public QuestionDatabase(Context context) throws ExceptionInInitializerError {
        if (instance == null) {
            QuestionDatabaseHelper dbHelper = new QuestionDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
            instance = this;
        } else {
            throw new ExceptionInInitializerError("Attempted to instantiate QuestionDatabase more than once!");
        }
    }

    /* Pull quizSize number of entries from the question database and return them
     * in a List, suitable for tossing into the QuestionDeck.
     */
    public void getQuestions(Integer quizSize, ArrayList<Question> questionList) {
        /* Submit a database query to return the requested number of trivia questions. */
        final String sqlQuery = "SELECT * FROM 'Questions' ORDER BY RANDOM() LIMIT ?;";
        String[] selectionArgs = {quizSize.toString()};
        Cursor cursor = database.rawQuery(sqlQuery, selectionArgs);

        /* Look up the column IDs for the table fields */
        final int id_question = cursor.getColumnIndex(QUESTIONS_QUESTION);
        final int id_correct = cursor.getColumnIndex(QUESTIONS_RESPONSE_CORRECT);
        final int id_wrong_a = cursor.getColumnIndex(QUESTIONS_RESPONSE_WRONG_A);
        final int id_wrong_b = cursor.getColumnIndex(QUESTIONS_RESPONSE_WRONG_B);
        final int id_wrong_c = cursor.getColumnIndex(QUESTIONS_RESPONSE_WRONG_C);

        /* Iterate through each row and populate the questionList array */
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            /* getPosition is zero-indexed, questionDeck is not, so add 1 to it.*/
            Integer number = cursor.getPosition() + 1;

            String question = cursor.getString(id_question);
            String response_correct = cursor.getString(id_correct);
            String response_wrong_a = cursor.getString(id_wrong_a);
            String response_wrong_b = cursor.getString(id_wrong_b);
            String response_wrong_c = cursor.getString(id_wrong_c);

            /* Create a Question object from the data returned from the database,
             * shove it into the questionList, and move onto the next row.
             */
            Question thisQuestion = new Question(number, question, response_correct,
                                            response_wrong_a, response_wrong_b, response_wrong_c);
            questionList.add(thisQuestion);
            cursor.moveToNext();
        }
    } // getQuestions

    /* Query the database to find out how many questions are contained
     * therein.  It simply returns the number of rows found in the Questions
     * table.
     */
    public Integer getQuestionCount() {
        Integer rowCount;
        /* This casting is safe, provided I'm not getting a hojillion entries from the DB. */
        rowCount = (int) (long) DatabaseUtils.queryNumEntries(database, TABLE_QUESTIONS);
        return rowCount;
    }

    /* Populate the database from a resource ID to a plain text file.
     * It is expected to be in the format:
     * <START OF FILE>
     * ---
     * Question
     * Correct Answer
     * Incorrect Answer A
     * Incorrect Answer B or NULL
     * Incorrect Answer C or NULL
     * <NEXT QUESTION OR END OF FILE>
     */
    public void loadDatabase(Context context, int resId) {
        final String DIVIDER = "---";

        /* First, delete all tables in the database */
        database.execSQL("DELETE FROM " + TABLE_QUESTIONS + ";");

        /* InsertHelper has apparently been deprecated as of API 18.  Replace with
         * SQLiteStatement.  For this project, however, I'm still going to use it.
         */
        DatabaseUtils.InsertHelper insertHelper = new DatabaseUtils.InsertHelper(database, TABLE_QUESTIONS);

        InputStream inputStream = context.getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        /* Get the numeric indexes for each column in the database table */
        final int id_question = insertHelper.getColumnIndex(QUESTIONS_QUESTION);
        final int id_correct = insertHelper.getColumnIndex(QUESTIONS_RESPONSE_CORRECT);
        final int id_wrong_a = insertHelper.getColumnIndex(QUESTIONS_RESPONSE_WRONG_A);
        final int id_wrong_b = insertHelper.getColumnIndex(QUESTIONS_RESPONSE_WRONG_B);
        final int id_wrong_c = insertHelper.getColumnIndex(QUESTIONS_RESPONSE_WRONG_C);

        String divider;
        try {
            while (((divider = bufferedReader.readLine()) != null)) {
                if (!divider.equals(DIVIDER)) {
                    Log.e(TAG, "Incorrect format in questions.txt, expected \"" + DIVIDER +
                            "\" divider," + " but found: \"" + divider + "\"");
                    continue;
                }

                /* Read out the rest of the values we need to populate the database */
                String question = bufferedReader.readLine();
                String response_correct = bufferedReader.readLine();
                String response_wrong_a = bufferedReader.readLine();
                String response_wrong_b = bufferedReader.readLine();
                String response_wrong_c = bufferedReader.readLine();

                /* Use the InsertHelper to prepare the insertion. *snrk* */
                insertHelper.prepareForInsert();

                /* Populate our fields for each column */
                insertHelper.bind(id_question, question);
                insertHelper.bind(id_correct, response_correct);
                insertHelper.bind(id_wrong_a, response_wrong_a);
                insertHelper.bind(id_wrong_b, response_wrong_b);
                insertHelper.bind(id_wrong_c, response_wrong_c);

                /* Execute the query to insert the data */
                insertHelper.execute();
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "IO Exception thrown while reading questions: " + e.getMessage());
        } finally {
            /* Close both buffers.  The try block is to suppress warnings.  If any
             * actual exception occurs here we're squash it since we don't care; The
             * resource is now closed.
             */
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (java.io.IOException e) { /* NARF! */}
        }
    } // loadDatabase
} // QuestionDatabase
