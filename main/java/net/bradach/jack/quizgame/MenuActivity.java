package net.bradach.jack.quizgame;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "QuizGame";

    private Global global;
    private SoundPool soundPool;
    private QuestionDatabase questionDatabase;
    private QuestionDeck questionDeck;

    /* Resource handles*/
    private ImageView imageViewQuizLogo;
    private TextView textViewQuizLengthValue;

    private TextView textViewQuestionCountValue;

    private TextView textViewLastQuizScoreValue;
    private Button buttonStartQuiz;
    private Button buttonLessQuestions;
    private Button buttonMoreQuestions;
    private Button buttonRemainIndoors;
    private Button buttonReloadDatabase;

    /* Game variables */
    private Integer quizLength;
    private Integer lastQuizScore;

    private int soundsLoaded = 0;
    private int soundCount = 2;

    private int soundRemainIndoors;
    private int soundSlideAdvance;

    private static final Integer DEFAULT_QUIZ_LENGTH = 10;
    private static final Integer MAX_QUIZ_LENGTH = 20;

    private static final String BUNDLE_QUIZ_LENGTH = "net.bradach.jack.quizgame.QUIZ_LENGTH";
    private static final String BUNDLE_QUIZ_SCORE = "net.bradach.jack.quizgame.QUIZ_SCORE";
    private static final String BUNDLE_LAST_QUIZ_SCORE = "net.bradach.jack.quizgame.LAST_QUIZ_SCORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            quizLength = savedInstanceState.getInt(BUNDLE_QUIZ_LENGTH);
            lastQuizScore = savedInstanceState.getInt(BUNDLE_LAST_QUIZ_SCORE);
        } else {
            quizLength = DEFAULT_QUIZ_LENGTH;
            lastQuizScore = 0;
        }

        /* Set the layout to be fullscreen with no title bar.  This "should" be able to
         * be done in the XML style, but it doesn't seem to be of an inclination to honor
         * the style I've chosen.  Easy enough to do it explicitly in code though.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Inflate our layout */
        setContentView(R.layout.activity_menu);

        /* Set up our references to any global objects */
        global = Global.getInstance();



        /* Create the QuestionDatabase if it hasn't already been instantiated. */
        if (global.questionDatabase == null) {
            global.questionDatabase = new QuestionDatabase(getApplicationContext());
        }
        questionDatabase = global.questionDatabase;

        /* Make sure our quizLength isn't more than we have questions in the database.
         * This is really just a catch that the database has been populated.
         */
        if (questionDatabase.getQuestionCount() > quizLength) {
            quizLength = questionDatabase.getQuestionCount();
        }

        /* Initialize the sound pool, again if not previously done. */
        if (global.soundPool == null) {
            global.soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            soundSlideAdvance = global.soundPool.load(this, R.raw.sound_slide_advance, 1);
            soundRemainIndoors = global.soundPool.load(this, R.raw.sound_remain_indoors, 1);
            global.soundsLoaded = 0;
        }

        soundPool = global.soundPool;
        soundsLoaded = global.soundsLoaded;

        /* Callback to count the number of loaded sounds. */
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                global.soundsLoaded++;
            }
        });




        /* Match XML resources to the appropriate handles in our object */
        imageViewQuizLogo = (ImageView) findViewById(R.id.imageViewQuizLogo);
        textViewQuizLengthValue = (TextView) findViewById(R.id.textViewQuizLengthValue);
        textViewQuestionCountValue = (TextView) findViewById(R.id.textViewQuestionCountValue);

        textViewLastQuizScoreValue = (TextView) findViewById(R.id.textViewLastQuizScoreValue);

        buttonStartQuiz = (Button) findViewById(R.id.buttonStartQuiz);
        buttonLessQuestions = (Button) findViewById(R.id.buttonLessQuestions);
        buttonMoreQuestions = (Button) findViewById(R.id.buttonMoreQuestions);
        buttonRemainIndoors = (Button) findViewById(R.id.buttonRemainIndoors);
        buttonReloadDatabase = (Button) findViewById(R.id.buttonReloadDatabase);

        /* Set up listeners */
        buttonStartQuiz.setOnClickListener(this);
        buttonLessQuestions.setOnClickListener(this);
        buttonMoreQuestions.setOnClickListener(this);
        buttonReloadDatabase.setOnClickListener(this);

        textViewQuestionCountValue.setText((questionDatabase.getQuestionCount()).toString());
        textViewQuizLengthValue.setText(quizLength.toString());
        textViewLastQuizScoreValue.setText(lastQuizScore.toString());


        /* A handler for the "Remain Indoors" button.  It plays a sound and changes
         * the image for the duration the button is held (hence the onTouchListener rather
         * than a simple onClick listener).  It's stupid and an obscure reference, but it
         * amuses me so it stays.
         */
        buttonRemainIndoors.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imageViewQuizLogo.setImageResource(R.drawable.quiz_title_remain_indoors);
                        if (global.soundsLoaded == soundCount) {
                            soundPool.play(2, 1, 1, 0, 0, 1);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP");
                        imageViewQuizLogo.setImageResource(R.drawable.quiz_title);
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    /* The main reason that I'm implementing View.onClickListener here is so I can easily
     * override the transition animation when the 'start quiz' button is clicked.  I handle
     * the other buttons here as well with a switch statement.
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartQuiz:
                /* Sanity check: do we have enough questions in the database? */
                if ((quizLength == 0) || (quizLength > questionDatabase.getQuestionCount())) {
                    CharSequence toastText = getString(R.string.toast_db_not_loaded);
                    Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                /* Create the deck of questions */
                global.questionDeck = new QuestionDeck();
                questionDeck = global.questionDeck;
                questionDeck.createQuiz(quizLength);

                soundPool.play(soundSlideAdvance, 1, 1, 0, 0, 1);
                Intent i = new Intent(MenuActivity.this, QuizActivity.class);
                i.putExtra(BUNDLE_QUIZ_LENGTH, quizLength);
                i.putExtra(BUNDLE_QUIZ_SCORE, 0);
                startActivityForResult(i, 1);
                this.overridePendingTransition(R.anim.animation_slideleft_newactivity, R.anim.animation_slideleft_oldactivity);
                break;

            case R.id.buttonReloadDatabase:
                questionDatabase.loadDatabase(getApplicationContext(), R.raw.questions);
                textViewQuestionCountValue.setText((questionDatabase.getQuestionCount()).toString());
                break;

            case R.id.buttonLessQuestions:
                if (quizLength > 1) {
                    quizLength--;
                    textViewQuizLengthValue.setText(quizLength.toString());
                }
                break;

            case R.id.buttonMoreQuestions:
                if ((quizLength < questionDatabase.getQuestionCount()) &&
                    (quizLength < MAX_QUIZ_LENGTH)) {
                    quizLength++;
                    textViewQuizLengthValue.setText(quizLength.toString());
                }
                break;

        }
    }

    /* Save any instance state for things like rotations, navigating away, etc. */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(BUNDLE_QUIZ_LENGTH, quizLength);
        savedInstanceState.putInt(BUNDLE_LAST_QUIZ_SCORE, lastQuizScore);
    }


    /* Handler for the data passed back to this activity from its children.
     * If the quiz completed, the score is retrieved and the "last quiz score"
     * is updated.  Cancelling causes the score to simply be dropped.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        if (requestCode == 1) {
            switch (resultCode) {
                case RESULT_OK:
                    Log.i(TAG, "Quiz complete!");
                    lastQuizScore = i.getIntExtra(BUNDLE_QUIZ_SCORE, -1);
                    textViewLastQuizScoreValue.setText(lastQuizScore.toString());
                    break;

                case RESULT_CANCELED:
                    Log.i(TAG, "Quiz canceled by user!");
                    break;

                default:
                    Log.w(TAG, "Quiz returned with unknown result code: " + resultCode);
            }
        }
    }
}
