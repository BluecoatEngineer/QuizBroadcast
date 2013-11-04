package net.bradach.jack.quizgame;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * The QuizActivity is the "game play" activity.  It is spawned from the
 * MenuActivity and presents the player with a single question.  Further
 * QuizActivities are spawned when they answer the question and hit "next"
 * or skip a question (which will be returned to later).  The result code
 * is then set back through each QuizActivity back to the MenuActivity at
 * the end of the quiz.  There is a bug here, wherein a user can get deep
 * enough that it will lose track of the originating activity.  Most users
 * don't do that, however.  In testing, I had to skip about 50 questions in
 * a row before I could no longer go back.
 */
public class QuizActivity extends Activity implements View.OnClickListener {
    /* References to global structures */
    private Global global;
    private SoundPool soundPool;
    private QuestionDeck questionDeck;

    /* Resource handles*/
    private TextView textViewScoreValue;
    private Button buttonHint;
    private Button buttonSkip;

    /* Game variables */
    private Integer quizLength;
    private Integer quizScore;
    private Question quizQuestion;

    /* List of the response buttons, for easy iterating. */
    private ArrayList<Button> responseButtonList;

    /* Flag indicating that the quiz has completed. */
    private boolean quizDone = false;

    /* Constants! */
    private static final String TAG = "QuizGame";
    private static final String BUNDLE_QUIZ_SCORE = "net.bradach.jack.quizgame.QUIZ_SCORE";
    private static final String BUNDLE_QUIZ_LENGTH = "net.bradach.jack.quizgame.QUIZ_LENGTH";
    private static final String BUNDLE_QUIZ_DONE = "net.bradach.jack.quizgame.QUIZ_DONE";
    private static final Integer COLOR_CORRECT = 0xFF00A000;
    private static final Integer COLOR_WRONG = 0xFFA00000;

    /* Set up everything needed to show a question (or handle coming back
     * from a suspended state).  All listeners are set up in onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Pull in any extras from the intent. */
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        quizScore = extras.getInt(BUNDLE_QUIZ_SCORE);
        quizLength = extras.getInt(BUNDLE_QUIZ_LENGTH);

        /* Restore any variables from the saved instance (or set defaults where appropriate) */
        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring instance state");
            quizLength = savedInstanceState.getInt(BUNDLE_QUIZ_LENGTH);
            quizScore = savedInstanceState.getInt(BUNDLE_QUIZ_SCORE);
            quizDone = savedInstanceState.getBoolean(BUNDLE_QUIZ_DONE);
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

        /* Start the music if it wasn't already playing. */
        if (!global.mediaPlayer.isPlaying()) {
            global.mediaPlayer.start();
        }

        /* Pull current question. */
        quizQuestion = questionDeck.getCurrentQuestion();

        /* Match XML resources to the appropriate handles in our object */
        TextView textViewQuestionNumber = (TextView) findViewById(R.id.questionNumber);
        TextView textViewQuestionText = (TextView) findViewById(R.id.questionText);
        textViewScoreValue = (TextView) findViewById(R.id.textViewScoreValue);

        /* Map response button handles to their objects, set up their
         * onClick listeners, and populate the list of response buttons.
         */
        responseButtonList = new ArrayList<Button>();

        Button buttonResponse_a = (Button) findViewById(R.id.buttonResponse_a);
        buttonResponse_a.setSoundEffectsEnabled(false);
        buttonResponse_a.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_a);

        Button buttonResponse_b = (Button) findViewById(R.id.buttonResponse_b);
        buttonResponse_b.setSoundEffectsEnabled(false);
        buttonResponse_b.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_b);

        Button buttonResponse_c = (Button) findViewById(R.id.buttonResponse_c);
        buttonResponse_c.setSoundEffectsEnabled(false);
        buttonResponse_c.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_c);

        Button buttonResponse_d = (Button) findViewById(R.id.buttonResponse_d);
        buttonResponse_d.setSoundEffectsEnabled(false);
        buttonResponse_d.setOnClickListener(responseOnClick);
        responseButtonList.add(buttonResponse_d);

        /* Map the other buttons handles and set up their listeners. */
        buttonHint = (Button) findViewById(R.id.buttonHint);
        buttonHint.setSoundEffectsEnabled(false);
        buttonHint.setOnClickListener(this);
        buttonHint.setEnabled(true);

        buttonSkip = (Button) findViewById(R.id.buttonSkip);
        buttonSkip.setSoundEffectsEnabled(false);
        buttonSkip.setOnClickListener(this);

        /* If this is the last remaining un-attempted question, disable the
         * skip button and change it to say "last question".
         */
        if (questionDeck.getUnansweredQuestionsRemaining() == 1) {
            buttonSkip.setEnabled(false);
            buttonSkip.setText("Last Question");
        }

        /* Update on-screen score with current value.  */
        textViewScoreValue.setText(quizScore.toString());

        /* Update the question header and question text. */
        textViewQuestionNumber.setText("Question #" + quizQuestion.getNumber()
                + " of " + questionDeck.getDeckSize());
        textViewQuestionText.setText(quizQuestion.getQuestionText());

        /* Finally, update the response buttons.  The "after restore" method
         * assumes that we just changed orientation or some such and skips
         * any animations used to get them back to that state.
         */
        if (savedInstanceState != null) {
            if (quizQuestion.getAttempted()) {
                updateLayoutForAnswered();
            } else {
                updateResponseButtonsAfterRestore();
            }
        } else {
            updateLayout();
        }
    } // onCreate

    /* Draw (or re-draw) the responses printed on the response buttons.
     * This will potentially set them invisible if the question object
     * indicates that there is no text for the button.  This routine
     * contains animators for when a question is cheated on.  This
     * looks cool, but I rather late in the game realized that they
     * require API >= 11 in order to work, meaning that Honeycomb is
     * a requirement.  I updated the manifest to reflect this.
     */
    void updateLayout() {
        Log.d(TAG, "updateLayout");
        /* If the question has been answered, use the "special" case. */
        if (quizQuestion.getAttempted()) {
            updateLayoutForAnswered();
            return;
        }

        /* Iterate over each response button and update the
         * text associated with it from the Question object.  Apply
         * any modifiers needed given its state.
         */
        for (int i = 0; i < 4; i++ ) {
            final Button response = responseButtonList.get(i);
            String responseText = quizQuestion.getResponseText(i);

            /* If getResponseText kicks back a null, that means that
             * the choice should not be displayed, either because
             * the questions doesn't have that many responses or
             * (more likely) because the player has asked for a hint.
             */
            if (responseText == null) {
                /* If the button was enabled, then we need to 'fade to black' it.  Otherwise,
                 * just set it to be invisible.
                 */
                if (response.isEnabled() && !quizQuestion.wasSkipped()) {
                    final ObjectAnimator anim = ObjectAnimator.ofFloat(response, "alpha", 1.0f, 0.0f);

                    /* A callback for the animator so that we can disable the button once it
                     * has faded out.  The "hint" button gets disabled for the duration of
                     * the animation as well.  It looked bad if you double-tapped it, causing
                     * two animations concurrently.
                     */
                    anim.addListener(new Animator.AnimatorListener() {
                        Boolean oldHintButtonState;
                        @Override
                        public void onAnimationStart(Animator animator) {
                            oldHintButtonState = buttonHint.isEnabled();
                            buttonHint.setEnabled(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            response.setEnabled(false);
                            response.setVisibility(View.INVISIBLE);
                            buttonHint.setEnabled(oldHintButtonState);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) { }

                        @Override
                        public void onAnimationRepeat(Animator animator) { }
                    });
                    anim.setDuration(500);
                    anim.start();
                } else {
                    /* The response was already dead from being cheated on
                     * earlier;  Don't reanimate it, just mark it invisible.
                     */
                    response.setText(null);
                    response.setVisibility(View.INVISIBLE);
                }
            } else {
                /* Otherwise, set the button to display the text
                 * for this response.  Set button visible, just
                 * in case there was some carry-over from a previous
                 * question.
                 */
                response.setText(responseText);
                response.setVisibility(View.VISIBLE);
            }
        }
    }

    /* After a restore / orientation change, this function gets called to quickly
     * "fix" the button states without them getting animated.
     */
    void updateResponseButtonsAfterRestore() {
        Log.d(TAG, "updateLayoutAfterRestore");
        for (int i = 0; i < 4; i++ ) {
            Button response = responseButtonList.get(i);
            String responseText = quizQuestion.getResponseText(i);

            /* Button was disabled, restore that state. */
            if (responseText == null) {
                response.setEnabled(false);
                response.setVisibility(View.INVISIBLE);
            } else {
                /* Button is visible */
                response.setText(responseText);
                response.setVisibility(View.VISIBLE);
            }
        }
    }

    /* Handler for response onClicks.  Since the handler is nearly identical
     * in all cases, it makes more sense to have one for them to share.  It
     * uses the view ID from the button clicked to map the response number
     * and test whether the response was correct.  If so, a point is awarded
     * and either goes to the next question or ends the quiz.
     */
    private final View.OnClickListener responseOnClick = new View.OnClickListener() {
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

            /* Save the response number in the question, so
             * we can refer to it if we get suspended.
             */
            quizQuestion.setResponse(responseNum);

            /* If the question was correct, they get points and a ding.  If
             * not, they get the buzzer.
             */
            if (quizQuestion.isResponseCorrect(responseNum)) {
                quizScore += quizQuestion.getWorth();
                textViewScoreValue.setText(quizScore.toString());
                soundPool.play(global.soundMap.get(SoundList.CORRECT), 1, 1, 0, 0, 1);
            } else {
                soundPool.play(global.soundMap.get(SoundList.WRONG), 1, 1, 0, 0, 1);
            }

            /* Mark this question as having been attempted */
            quizQuestion.setAttempted();

            /* Check if that was the last question. */
            if (questionDeck.getUnansweredQuestionsRemaining() == 0) {
                quizDone = true;
            }

            /* Update the response buttons with answered colors */
            updateLayout();
        }
    };

    /**
     * When the question has been responded to, we use a slightly different method
     * of updating the layout.  It changes some of the labels and disables all the
     * buttons except for next/end.  Also, the correct (and possibly incorrect)
     * responses are highlighted.
     */
    void updateLayoutForAnswered() {
        Integer responseNum = quizQuestion.getResponse();

        /* Can't cheat on an answered question. */
        buttonHint.setEnabled(false);

        /* After answering a question, change 'skip' to 'next' to suggest what
         * they should do.  If this was the last question, change the button
         * to say "End Quiz" and re-enable it (since it would have been disabled
         * earlier).*/
        if (quizDone) {
            buttonSkip.setText("End Quiz");
            buttonSkip.setEnabled(true);
        } else {
            buttonSkip.setText("Next Question");
            buttonSkip.setEnabled(true);
        }

        /* Disable all of the response buttons and set
         * Set the button holding the correct answer to be green.
         */
        for (int i = 0; i < 4; i++) {
            Button response = responseButtonList.get(i);
            String responseText = quizQuestion.getResponseText(i);
            if (responseText == null) {
                response.setVisibility(View.INVISIBLE);
            }

            response.setText(responseText);
            response.setEnabled(false);

            if (quizQuestion.isResponseCorrect(i)) {
                response.setBackgroundColor(COLOR_CORRECT);
            } else if (responseNum == i) {
                response.setBackgroundColor(COLOR_WRONG);
            }
        }
    }

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
             * to the parent of this activity (which will fire-brigade it back to the menu).
             */
            case R.id.buttonSkip:
                if (quizDone) {
                    /* Kill the background music */
                    global.mediaPlayer.stop();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(BUNDLE_QUIZ_SCORE, quizScore);
                    soundPool.play(global.soundMap.get(SoundList.SLIDE_ADVANCE), 1, 1, 0, 0, 1);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                    this.overridePendingTransition(R.anim.animation_slideright_newactivity, R.anim.animation_slideright_oldactivity);
                } else {
                    /* Mark question as skipped and queue up the next question. */
                    quizQuestion.setSkipped();
                    questionDeck.getNextQuestion();
                    Intent nextQuiz = new Intent(QuizActivity.this, QuizActivity.class);
                    nextQuiz.putExtra(BUNDLE_QUIZ_LENGTH, quizLength);
                    nextQuiz.putExtra(BUNDLE_QUIZ_SCORE, quizScore);
                    soundPool.play(global.soundMap.get(SoundList.SLIDE_ADVANCE), 1, 1, 0, 0, 1);
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
                if (quizQuestion.canCheat()) {
                    quizQuestion.cheat();
                    /* Sound cue for cheating */
                    soundPool.play(global.soundMap.get(SoundList.CHEAT), 1, 1, 0, 0, 1);

                    /* The text for the response buttons has
                     * changed, so redraw them.
                     */
                    updateLayout();
                } else {
                    /* Fire a toast to explain the situation */
                    CharSequence toastText = getString(R.string.toast_no_more_cheats);
                    Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();

                    /* Audio cue to indicate no more cheats available. */
                    soundPool.play(global.soundMap.get(SoundList.NOCHEAT), 1, 1, 0, 0, 1);
                }

                break;

        }
    }

    /* If back is pressed, abort the quiz from where we are and spawn a new MenuActivity.
     * Score is not preserved and the result is listed as "canceled."
     */
    @Override
    public void onBackPressed() {
        /* Kill the music */
        global.mediaPlayer.stop();

        /* TODO: Add a 'back twice to exit,' with a toast.*/
        soundPool.play(global.soundMap.get(SoundList.SLIDE_ADVANCE), 1, 1, 0, 0, 1);
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        this.overridePendingTransition(R.anim.animation_slideright_newactivity, R.anim.animation_slideright_oldactivity);
    }

    /* Save the few variables we need to keep our state. */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(BUNDLE_QUIZ_LENGTH, quizLength);
        savedInstanceState.putInt(BUNDLE_QUIZ_SCORE, quizScore);
        savedInstanceState.putBoolean(BUNDLE_QUIZ_DONE, quizDone);
    }

    /* Pass the result to the parent activity (which does the same until it
     * reaches the menuActivity.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        Intent returnIntent = new Intent();
        returnIntent.putExtras(i);
        setResult(resultCode, returnIntent);
        finish();
    }

    /* We're being paused, and so should the music. */
    @Override
    protected void onPause() {
        super.onPause();
        if (global.mediaPlayer.isPlaying()) {
            global.mediaPlayer.pause();
        }
    }

    /* And now?  Back to the show. */
    @Override
    protected void onResume() {
        super.onResume();
        if (!global.mediaPlayer.isPlaying()) {
            global.mediaPlayer.start();
        }
    }
} // QuizActivity
