package net.bradach.jack.quizgame;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

/* MenuActivity is the "main" activity for this application.  It handles creation and
 * initialization of all globally shared resources and preparing the questions that
 * the QuizActivities will use.  It implements a View.OnClickListener so that it
 * can have a slightly easier time of implementing the transition animations.
 */
public class MenuActivity extends Activity implements View.OnClickListener {
    /* References to the global structure and items therein.  These
     * are globally shared resources.  Handling them like this is
     * significantly quicker than Parcelizing them in order to pass
     * among activities (and much easier too).  It's also apparently
     * the BKM from Google.
     */
    private Global global;
    private SoundPool soundPool;
    private QuestionDatabase questionDatabase;

    /* Resource handles for objects defined in XML */
    private ImageView imageViewQuizLogo;
    private TextView textViewQuizLengthValue;
    private TextView textViewQuestionCountValue;
    private TextView textViewLastQuizScoreValue;

    /* Game variables */
    private Integer quizLength;
    private Integer lastQuizScore;

    /* Constants aplenty. */
    private static final String TAG = "MenuActivity";
    private static final Integer DEFAULT_QUIZ_LENGTH = 10;
    private static final Integer MAX_QUIZ_LENGTH = 20;
    private static final String BUNDLE_QUIZ_LENGTH = "net.bradach.jack.quizgame.QUIZ_LENGTH";
    private static final String BUNDLE_QUIZ_SCORE = "net.bradach.jack.quizgame.QUIZ_SCORE";
    private static final String BUNDLE_LAST_QUIZ_SCORE = "net.bradach.jack.quizgame.LAST_QUIZ_SCORE";

    /* Set up activity resources (or restore them after being stopped).  There are
     * special cases as need be for when we are initializing vs simply being
     * restored due to orientation change or losing visibility.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* If we just popped back from being suspended, snag the previous
         * values of the game variables.  Otherwise set them to sane defaults.
         */
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
        if (quizLength > questionDatabase.getQuestionCount()) {
            quizLength = questionDatabase.getQuestionCount();
        }

        /* Initialize the sound pool and associated structures if not previously done. */
        if (global.soundMap == null) {
            global.soundMap = new HashMap<SoundList, Integer>();
        }

        /* Sound effects are handled by a SoundPool object.  If this is the first time
         * through, one does not exist and we have to create it, load the sounds, and
         * put the resultant sound IDs into the sound map, so we can easily play them
         * where they are needed.
         */
        if (global.soundPool == null) {
            int soundId;
            global.soundsLoadedCount = 0;
            global.soundsLoaded = false;
            global.soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

            /* Load the "Slide Advancing" sound */
            soundId = global.soundPool.load(this, R.raw.sound_slide_advance, 1);
            global.soundMap.put(SoundList.SLIDE_ADVANCE, soundId);

            /* Load the "Buzzer" sound */
            soundId = global.soundPool.load(this, R.raw.sound_buzzer, 1);
            global.soundMap.put(SoundList.BUZZER, soundId);

            /* Load the "Correct" sound */
            soundId = global.soundPool.load(this, R.raw.sound_correct, 1);
            global.soundMap.put(SoundList.CORRECT, soundId);

            /* Load the "Wrong" sound */
            soundId = global.soundPool.load(this, R.raw.sound_wrong, 1);
            global.soundMap.put(SoundList.WRONG, soundId);

            /* Load the "Cheat" sound */
            soundId = global.soundPool.load(this, R.raw.sound_cheat, 1);
            global.soundMap.put(SoundList.CHEAT, soundId);

            /* Load the "No Cheat" sound */
            soundId = global.soundPool.load(this, R.raw.sound_nocheat, 1);
            global.soundMap.put(SoundList.NOCHEAT, soundId);

        }
        soundPool = global.soundPool;

        /* Set up the media player (for background music) if it's never been
         * created, load the intro music and start it.  Otherwise, leave it alone.
         * After the first time the MenuActivity is created, the media player
         * will be recreated in the onActivityResult callback.
         */
        if (global.mediaPlayer == null) {
            global.mediaPlayer = MediaPlayer.create(this, R.raw.sound_matchgameintro);
            global.mediaPlayer.setLooping(true);
            global.mediaPlayer.setVolume(0.5f, 0.5f);
            global.mediaPlayer.start();
        }

        /* Callback to count the number of loaded sounds and set a flag when they're
         * all loaded. Any time a sound is played, it's a good idea to check that flag.
         * In practice, though, they'll all be loaded by the time any of them are needed.
         */
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                global.soundsLoadedCount++;
                if (global.soundsLoadedCount == SoundList.values().length) {
                    global.soundsLoaded = true;
                }
            }
        });

        /* Match XML resources to the appropriate handles in our object */
        imageViewQuizLogo = (ImageView) findViewById(R.id.imageViewQuizLogo);
        textViewQuizLengthValue = (TextView) findViewById(R.id.textViewQuizLengthValue);
        textViewQuestionCountValue = (TextView) findViewById(R.id.textViewQuestionCountValue);
        textViewLastQuizScoreValue = (TextView) findViewById(R.id.textViewLastQuizScoreValue);

        /* The buttons aren't used anywhere else, so they're local to this method. */
        Button buttonStartQuiz = (Button) findViewById(R.id.buttonStartQuiz);
        Button buttonLessQuestions = (Button) findViewById(R.id.buttonLessQuestions);
        Button buttonMoreQuestions = (Button) findViewById(R.id.buttonMoreQuestions);
        Button buttonRemainIndoors = (Button) findViewById(R.id.buttonRemainIndoors);
        Button buttonReloadDatabase = (Button) findViewById(R.id.buttonReloadDatabase);

        /* Set up listeners */
        buttonStartQuiz.setOnClickListener(this);
        buttonLessQuestions.setOnClickListener(this);
        buttonMoreQuestions.setOnClickListener(this);
        buttonReloadDatabase.setOnClickListener(this);

        /* Update the on-screen value elements */
        textViewQuestionCountValue.setText((questionDatabase.getQuestionCount()).toString());
        textViewQuizLengthValue.setText(quizLength.toString());
        textViewLastQuizScoreValue.setText(lastQuizScore.toString());

        /* A handler for the "Remain Indoors" button.  It plays a sound and changes
         * the image for the duration the button is held (hence the onTouchListener rather
         * than a simple onClick listener).  It's stupid and a semi-obscure pop-culture
         * reference, but it amuses me so it stays.
         */
        buttonRemainIndoors.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imageViewQuizLogo.setImageResource(R.drawable.quiz_title_remain_indoors);
                        if (global.soundsLoaded) {
                            soundPool.play(global.soundMap.get(SoundList.BUZZER), 1, 1, 0, 0, 1);
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
    } // onCreate

    /* The main reason that I'm implementing View.onClickListener here is so I can easily
     * override the transition animation when the 'start quiz' button is clicked.  I handle
     * the other buttons here as well with a switch statement.
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartQuiz:
                /* Sanity check: do we have enough questions in the database?  Throw up
                 * a toast prompting the user to load the database if they haven't already.
                 */
                if ((quizLength == 0) || (quizLength > questionDatabase.getQuestionCount())) {
                    CharSequence toastText = getString(R.string.toast_db_not_loaded);
                    Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                /* Create the deck of questions */
                global.questionDeck = new QuestionDeck();
                global.questionDeck.createQuiz(quizLength);

                /* Kill the music and set up the one for the quiz */
                global.mediaPlayer.stop();
                global.mediaPlayer = MediaPlayer.create(this, R.raw.sound_matchgame);
                global.mediaPlayer.setVolume(0.5f, 0.5f);
                global.mediaPlayer.setLooping(true);

                /* Play transition sound clip. */
                soundPool.play(global.soundMap.get(SoundList.SLIDE_ADVANCE), 1, 1, 0, 0, 1);

                /* Create the new intent and spawn it. */
                Intent i = new Intent(MenuActivity.this, QuizActivity.class);
                i.putExtra(BUNDLE_QUIZ_LENGTH, quizLength);
                i.putExtra(BUNDLE_QUIZ_SCORE, 0);
                startActivityForResult(i, 1);
                this.overridePendingTransition(R.anim.animation_slideleft_newactivity, R.anim.animation_slideleft_oldactivity);
                break;

            /* Reload the database from the text file resource.  This isn't done automatically
             * because at some point it might be able to load from a file.
             */
            case R.id.buttonReloadDatabase:
                questionDatabase.loadDatabase(getApplicationContext(), R.raw.questions);
                textViewQuestionCountValue.setText((questionDatabase.getQuestionCount()).toString());
                /* Reset the default quiz length, since it was probably zero. */
                quizLength = DEFAULT_QUIZ_LENGTH;
                textViewQuizLengthValue.setText(quizLength.toString());
                break;

            /* Reduce the size of the quiz. */
            case R.id.buttonLessQuestions:
                if (quizLength > 1) {
                    quizLength--;
                    textViewQuizLengthValue.setText(quizLength.toString());
                }
                break;

            /* Increase the size of the quiz (up to MAX_QUIZ_LENGTH) */
            case R.id.buttonMoreQuestions:
                if ((quizLength < questionDatabase.getQuestionCount()) &&
                    (quizLength < MAX_QUIZ_LENGTH)) {
                    quizLength++;
                    textViewQuizLengthValue.setText(quizLength.toString());
                }
                break;

        } // switch (v.getId())
    } // onClick

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

            /* Start the background music back up. */
            global.mediaPlayer = MediaPlayer.create(this, R.raw.sound_matchgameintro);
            global.mediaPlayer.setLooping(true);
            global.mediaPlayer.setVolume(0.5f, 0.5f);
            global.mediaPlayer.start();
        }
    } // onActivityResult
} // MenuActivity
