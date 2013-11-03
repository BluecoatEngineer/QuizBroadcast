package net.bradach.jack.quizgame;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.net.ResponseCache;
import java.util.ArrayList;

public class QuizActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "QuizGame";

    private Global global;
    private SoundPool soundPool;
    private QuestionDeck questionDeck;

    /* Resource handles*/
    private TextView textViewQuestionNumber;
    private TextView textViewQuestionText;
    private TextView textViewScoreValue;
    private Button buttonResponse_a;
    private Button buttonResponse_b;
    private Button buttonResponse_c;
    private Button buttonResponse_d;
    private Button buttonHint;
    private Button buttonSkip;

    /* Game variables */
    private Integer quizLength;
    private Integer quizScore;
    private Question quizQuestion;

    private ArrayList<Button> responseButtonList;

    private boolean quizEnded = false;

    private static final String BUNDLE_QUIZ_SCORE = "net.bradach.jack.quizgame.QUIZ_SCORE";
    private static final String BUNDLE_QUIZ_LENGTH = "net.bradach.jack.quizgame.QUIZ_LENGTH";

    private static final Integer COLOR_CORRECT = 0xFF00A000;
    private static final Integer COLOR_WRONG = 0xFFA00000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Pull in any extras from the intent. */
        Bundle extras = getIntent().getExtras();
        quizScore = extras.getInt(BUNDLE_QUIZ_SCORE);
        quizLength = extras.getInt(BUNDLE_QUIZ_LENGTH);

        /* Restore any variables from the saved instance (or set defaults where appropriate) */
        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring instance state");
            quizLength = savedInstanceState.getInt(BUNDLE_QUIZ_LENGTH);
            quizScore = savedInstanceState.getInt(BUNDLE_QUIZ_SCORE);
        }

        /* Set the layout to be fullscreen with no title bar.  This "should" be able to
         * be done in the XML style, but it doesn't seem to be of an inclination to honor
         * the style I've chosen.  Easy enough to do it explicitly in code though.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Inflate our layout */
        setContentView(R.layout.activity_quiz);

        /* Set up Global pointers */
        global = Global.getInstance();
        soundPool = global.soundPool;
        questionDeck = global.questionDeck;

        /* Pull current question. */
        quizQuestion = questionDeck.getCurrentQuestion();

        /* Match XML resources to the appropriate handles in our object */
        textViewQuestionNumber = (TextView) findViewById(R.id.questionNumber);
        textViewQuestionText = (TextView) findViewById(R.id.questionText);
        textViewScoreValue = (TextView) findViewById(R.id.textViewScoreValue);

        /* Map response button handles to their objects, set up their
         * onClick listeners, and populate the list of response buttons.
         */
        responseButtonList = new ArrayList<Button>();

        buttonResponse_a = (Button) findViewById(R.id.buttonResponse_a);
        buttonResponse_a.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_a);

        buttonResponse_b = (Button) findViewById(R.id.buttonResponse_b);
        buttonResponse_b.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_b);

        buttonResponse_c = (Button) findViewById(R.id.buttonResponse_c);
        buttonResponse_c.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_c);

        buttonResponse_d = (Button) findViewById(R.id.buttonResponse_d);
        buttonResponse_d.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_d);

        /* Map the other buttons handles and set up their listeners. */
        buttonHint = (Button) findViewById(R.id.buttonHint);
        buttonHint.setOnClickListener(this);

        /* The hint button is only enabled if cheats are available. */
        if (!quizQuestion.canCheat()) {
            buttonHint.setEnabled(false);
        } else {
            buttonHint.setEnabled(true);
        }


        buttonSkip = (Button) findViewById(R.id.buttonSkip);
        buttonSkip.setOnClickListener(this);

        /* If this is the last remaining un-attempted question, disable the
         * skip button and change it to say "last question".
         */
        if (questionDeck.getUnansweredQuestionsRemaining() == 1) {
            buttonSkip.setEnabled(false);
            buttonSkip.setText("Last\nQuestion");
        }

        /* Update on-screen score with current value.  */
        textViewScoreValue.setText(quizScore.toString());

        /* Update the question header and question text. */
        textViewQuestionNumber.setText("Question #" + quizQuestion.getNumber()
                + " of " + questionDeck.getDeckSize());
        textViewQuestionText.setText(quizQuestion.getQuestionText());

        /* Finally, update the response buttons. */
        updateResponseButtons();

    }

    /* Draw (or re-draw) the responses printed on the response buttons.
     * This will potentially set them invisible if the question object
     * indicates that there is no text for the button.
     */
    void updateResponseButtons() {
        for (int i = 0; i < 4; i++ ) {
            Button response = (Button) responseButtonList.get(i);
            String responseText = (String) quizQuestion.getResponseText(i);

            /* If getResponseText kicks back a null, that means that
             * the choice should not be displayed, either because
             * the questions doesn't have that many responses or
             * (more likely) because the player has asked for a hint.
             */
            if (responseText == null) {
                response.setVisibility(View.INVISIBLE);
                continue;
            }

            /* Otherwise, set the button to display the text
             * for this response.  Set button visible, just
             * in case there was some carry-over from a previous
             * question.
             */
            response.setText(responseText);
            response.setVisibility(View.VISIBLE);
        }
    }

    /* Handler for response onClicks.  Since the handler is nearly identical
     * in all cases, it makes more sense to have one for them to share.  It
     * uses the view ID from the button clicked to map the response number
     * and test whether the response was correct.  If so, a point is awarded
     * and either goes to the next question or ends the quiz.
     */
    View.OnClickListener responseOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int responseNum = -1;

        /* Figure out what button got hit.  There's no default case
         * because all possibilities are being covered in the switch.
         */
            switch (v.getId()) {
                case (R.id.buttonResponse_a):
                    responseNum = 0;
                    break;
                case (R.id.buttonResponse_b):
                    responseNum = 1;
                    break;
                case (R.id.buttonResponse_c):
                    responseNum = 2;
                    break;
                case (R.id.buttonResponse_d):
                    responseNum = 3;
                    break;
            }

            if (quizQuestion.isResponseCorrect(responseNum)) {
                quizScore += quizQuestion.getWorth();
                textViewScoreValue.setText(quizScore.toString());
            }

            /* Disable all of the response buttons and set
             * Set the button holding the correct answer to be green.
             */
            for (int i = 0; i < 4; i++) {
                Button response = (Button) responseButtonList.get(i);
                response.setEnabled(false);

                if (quizQuestion.isResponseCorrect(i)) {
                    response.setBackgroundColor(COLOR_CORRECT);
                } else if (responseNum == i) {
                    response.setBackgroundColor(COLOR_WRONG);
                }


            }

            /* Mark this question as having been attempted */
            quizQuestion.setAttempted();

            /* After answering a question, change 'skip' to 'next' to suggest what
             * they should do.  If this was the last question, change the button
             * to say "End Quiz" and re-enable it (since it would have been disabled
             * earlier).*/
            if (questionDeck.getUnansweredQuestionsRemaining() > 0) {
                buttonSkip.setText("Next\nQuestion");
            } else {
                quizEnded = true;
                buttonSkip.setEnabled(true);
                buttonSkip.setText("End\nQuiz");
            }


        }
    };

    /* The class interface for onClick is used specifically to allow the enter/exit
     * animations to be overridden.  The parent class has to be an Activity in
     * order to do that.  Having an inner class or anonymous class for the listener
     * doesn't cut it.  There are a couple other methods I could use to get around
     * this, but this was the cleanest.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            /* If 'skip' is chosen, and the quiz is not over, spawn a new QuizActivity.
             * This QuizActivity dies after spawning the new one (no reason for it to
             * stick around).  If we have finished the quiz, then send the result back
             * to
             *
             */
            case R.id.buttonSkip:

                if (quizEnded == true) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(BUNDLE_QUIZ_SCORE, quizScore);
                    soundPool.play(1, 1, 1, 0, 0, 1);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                    this.overridePendingTransition(R.anim.animation_slideright_newactivity, R.anim.animation_slideright_oldactivity);
                } else {
                    /* queue up the next question. */
                    questionDeck.getNextQuestion();
                    Intent nextQuiz = new Intent(QuizActivity.this, QuizActivity.class);
                    nextQuiz.putExtra(BUNDLE_QUIZ_LENGTH, quizLength);
                    nextQuiz.putExtra(BUNDLE_QUIZ_SCORE, quizScore);
                    soundPool.play(1, 1, 1, 0, 0, 1);
                    startActivityForResult(nextQuiz, 1);
                    this.overridePendingTransition(R.anim.animation_slideleft_newactivity, R.anim.animation_slideleft_oldactivity);

                }

                break;

            /* Eliminate an incorrect response.  The 'cheat' call
             * is safe in that if no more cheats were available,
             * it simply does nothing.  After the cheat, the hint
             * button gets disabled if there are no more cheats.
             */
            case R.id.buttonHint:
                quizQuestion.cheat();

                if (!quizQuestion.canCheat()) {
                    buttonHint.setEnabled(false);
                }

                /* The text for the response buttons has
                 * changed, so redraw them.
                 */
                updateResponseButtons();

                break;

        }
    }


    /* If back is pressed, abort the quiz from where we are and spawn a new MenuActivity.
     * Score is not preserved and the result is listed as "canceled."
     */
    @Override
    public void onBackPressed() {
        /* TODO: Add a 'back twice to exit,' with a toast.*/
        soundPool.play(1, 1, 1, 0, 0, 1);
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        this.overridePendingTransition(R.anim.animation_slideright_newactivity, R.anim.animation_slideright_oldactivity);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(BUNDLE_QUIZ_LENGTH, quizLength);
        savedInstanceState.putInt(BUNDLE_QUIZ_SCORE, quizScore);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        Intent returnIntent = new Intent();
        returnIntent.putExtras(i);
        setResult(resultCode, returnIntent);
        finish();
    }


}
