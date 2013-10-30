package net.bradach.jack.quizgame;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class QuizActivity extends Activity {
    private static final String TAG = "QuizGame";

    /* Resource handles*/
    private TextView textViewQuestionNumber;
    private TextView textViewQuestionText;
    private TextView textViewScoreLabel;
    private TextView textViewScoreValue;
    private Button buttonResponse_a;
    private Button buttonResponse_b;
    private Button buttonResponse_c;
    private Button buttonResponse_d;
    private Button buttonHint;
    private Button buttonSkip;

    /* Game variables */
    private Integer quizScore = 0;
    private Integer questionNumber = 1;
    private Question quizQuestion;

    /* List of questions.  The skip button goes
    * to the next one, but does not mark it as answered.*/
    private ArrayList<Question> questionSet;




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

        /* Match XML resources to the appropriate handles in our object */
        textViewQuestionNumber = (TextView) findViewById(R.id.questionNumber);
        textViewQuestionText = (TextView) findViewById(R.id.questionText);
        textViewScoreLabel = (TextView) findViewById(R.id.scoreLabel);
        textViewScoreValue = (TextView) findViewById(R.id.scoreValue);
        buttonResponse_a = (Button) findViewById(R.id.buttonResponse_a);
        buttonResponse_b = (Button) findViewById(R.id.buttonResponse_b);
        buttonResponse_c = (Button) findViewById(R.id.buttonResponse_c);
        buttonResponse_d = (Button) findViewById(R.id.buttonResponse_d);
        buttonHint = (Button) findViewById(R.id.buttonHint);
        buttonSkip = (Button) findViewById(R.id.buttonSkip);

        /* Set up listeners */
        buttonResponse_a.setOnClickListener(responseOnClick);
        buttonResponse_b.setOnClickListener(responseOnClick);
        buttonResponse_c.setOnClickListener(responseOnClick);
        buttonResponse_d.setOnClickListener(responseOnClick);



        testQuestion();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    void testQuestion() {
        HashMap<QuestionEntries, String> questionMap;

        questionMap = new HashMap<QuestionEntries, String>();
        questionMap.put(QuestionEntries.QUESTION,
                "What is secured in a ship's cathead?");
        questionMap.put(QuestionEntries.RESPONSE_CORRECT,
                "The anchor");
        questionMap.put(QuestionEntries.RESPONSE_WRONG_A,
                "Ammunition");
        questionMap.put(QuestionEntries.RESPONSE_WRONG_B,
                "Cat litter");
        questionMap.put(QuestionEntries.RESPONSE_WRONG_C,
                "Tigers");

        quizQuestion = new Question(questionMap);

        quizQuestion.shuffleResponses();

        textViewQuestionNumber.setText("Question #" + questionNumber);
        textViewQuestionText.setText(quizQuestion.getQuestionText());
        buttonResponse_a.setText(quizQuestion.getResponseText(0));
        buttonResponse_b.setText(quizQuestion.getResponseText(1));
        buttonResponse_c.setText(quizQuestion.getResponseText(2));
        buttonResponse_d.setText(quizQuestion.getResponseText(3));

        textViewScoreValue.setText(quizScore.toString());

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

        /* Figure out what button got hit */
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

                default:
                    Log.e(TAG, "Unknown clicked!");
            }

            if (quizQuestion.isResponseCorrect(responseNum)) {
                quizScore++;
                questionNumber++;
            }

            /*
              if (questionNumber < 10)
                this->nextQuestion();
              else
                this->endQuiz();
            */


            quizQuestion.shuffleResponses();
            textViewQuestionNumber.setText("Question #" + questionNumber);
            textViewQuestionText.setText(quizQuestion.getQuestionText());
            buttonResponse_a.setText(quizQuestion.getResponseText(0));
            buttonResponse_b.setText(quizQuestion.getResponseText(1));
            buttonResponse_c.setText(quizQuestion.getResponseText(2));
            buttonResponse_d.setText(quizQuestion.getResponseText(3));

            textViewScoreValue.setText(quizScore.toString());

        }
    };






}
