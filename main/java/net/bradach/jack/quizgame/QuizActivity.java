package net.bradach.jack.quizgame;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

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

        this.testQuestion();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    void testQuestion() {
        HashMap<QuestionEntries, String> questionMap;
        Question testQuestion;

        questionMap = new HashMap<QuestionEntries, String>();
        questionMap.put(QuestionEntries.QUESTION,
                "What is secured in a ship's cathead?");
        questionMap.put(QuestionEntries.ANSWER_CORRECT,
                "The anchor");
        questionMap.put(QuestionEntries.ANSWER_WRONG_A,
                "Ammunition");
        questionMap.put(QuestionEntries.ANSWER_WRONG_B,
                "Cat litter");
        questionMap.put(QuestionEntries.ANSWER_WRONG_C,
                "Tigers");

        testQuestion = new Question(questionMap);

        testQuestion.shuffleAnswers();

        TextView questionText = (TextView) findViewById(R.id.questionText);
        questionText.setText(testQuestion.getQuestionText());

        TextView buttonResponse_a = (Button) findViewById(R.id.buttonResponse_a);
        buttonResponse_a.setText(testQuestion.getResponseText(0));

        TextView buttonResponse_b = (Button) findViewById(R.id.buttonResponse_b);
        buttonResponse_b.setText(testQuestion.getResponseText(1));

        TextView buttonResponse_c = (Button) findViewById(R.id.buttonResponse_c);
        buttonResponse_c.setText(testQuestion.getResponseText(2));

        TextView buttonResponse_d = (Button) findViewById(R.id.buttonResponse_d);
        buttonResponse_d.setText(testQuestion.getResponseText(3));


    }
    
}
