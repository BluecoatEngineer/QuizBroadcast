package net.bradach.jack.quizgame;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class QuizActivity extends Activity {
    private static final String TAG = "QuizGame";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Set the layout to be fullscreen with no title bar.  This "should" be able to
         * be done in the XML style, but it doesn't seem to be of an inclination to honor
         * the style I've chosen.  Easy enough to do it explicitly in code though.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Inflate our layout */
        setContentView(R.layout.activity_quiz);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }
    
}
