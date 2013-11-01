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

public class MenuActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "QuizGame";

    /* Resource handles*/
    private ImageView imageViewQuizLogo;
    private TextView textViewQuizLength;
    private Button buttonStartQuiz;
    private Button buttonLessQuestions;
    private Button buttonMoreQuestions;
    private Button buttonRemainIndoors;

    /* Game variables */
    private Integer quizLength;

    private int soundsLoaded = 0;
    private int soundCount = 2;

    private int soundRemainIndoors;
    private int soundSlideAdvance;

    private static final Integer DEFAULT_QUIZ_LENGTH = 10;

    private static final String BUNDLE_QUIZ_LENGTH = "net.bradach.jack.quizgame.QUIZ_LENGTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Unpack any extras that may have come with the bundle. */
        quizLength = getIntent().getIntExtra(BUNDLE_QUIZ_LENGTH, DEFAULT_QUIZ_LENGTH);

        /* Set the layout to be fullscreen with no title bar.  This "should" be able to
         * be done in the XML style, but it doesn't seem to be of an inclination to honor
         * the style I've chosen.  Easy enough to do it explicitly in code though.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Inflate our layout */
        setContentView(R.layout.activity_menu);

        /* Match XML resources to the appropriate handles in our object */
        imageViewQuizLogo = (ImageView) findViewById(R.id.imageViewQuizLogo);
        textViewQuizLength = (TextView) findViewById(R.id.textViewQuizLength);
        buttonStartQuiz = (Button) findViewById(R.id.buttonStartQuiz);
        buttonLessQuestions = (Button) findViewById(R.id.buttonLessQuestions);
        buttonMoreQuestions = (Button) findViewById(R.id.buttonMoreQuestions);
        buttonRemainIndoors = (Button) findViewById(R.id.buttonRemainIndoors);

        /* Set up listeners */
        buttonStartQuiz.setOnClickListener(this);
        buttonLessQuestions.setOnClickListener(this);
        buttonMoreQuestions.setOnClickListener(this);

        /* A handler for the "Remain Indoors" button.  It plays a sound and changes
         * the image for the duration the button is held (hence the onTouchListener rather
         * than a simple onClick listener).  It's stupid and an obscure reference, but it
         * amuses me so it stays.
         */
        buttonRemainIndoors.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (soundsLoaded == soundCount) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            imageViewQuizLogo.setImageResource(R.drawable.quiz_title_remain_indoors);
                            Globals.soundPool.play(soundRemainIndoors, 1, 1, 0, 0, 1);
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(TAG, "ACTION_UP");
                            imageViewQuizLogo.setImageResource(R.drawable.quiz_title);
                            break;
                    }
                }
                return true;
            }
        });


        if (Globals.soundPool == null) {
            Globals.soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            soundSlideAdvance = Globals.soundPool.load(this, R.raw.sound_slide_advance, 1);
            soundRemainIndoors = Globals.soundPool.load(this, R.raw.sound_remain_indoors, 1);
        }

        /* Count the number of loaded sounds. */
        Globals.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded++;
            }
        });





    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    /* Handler for response onClicks.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStartQuiz:
                Globals.soundPool.play(soundSlideAdvance, 1, 1, 0, 0, 1);
                Intent i = new Intent(MenuActivity.this, QuizActivity.class);
                i.putExtra(BUNDLE_QUIZ_LENGTH, quizLength);
                startActivity(i);
                this.overridePendingTransition(R.anim.animation_slideleft_newactivity, R.anim.animation_slideleft_oldactivity);
                break;


        }
    }










}
