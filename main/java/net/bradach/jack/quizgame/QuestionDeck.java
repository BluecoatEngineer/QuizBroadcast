package net.bradach.jack.quizgame;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds the deck of questions for the current quiz.
 */
final public class QuestionDeck {
    private static final String TAG = "QuestionDeck";
    private Global global;
    private QuestionDatabase questionDatabase;

    private Integer questions = 0;
    private ArrayList<Question> questionList;
    private Iterator<Question> questionListIterator = null;
    private Question currentQuestion = null;

    public QuestionDeck() {
        /* Create the list object */
        questionList = new ArrayList<Question>();
        global = Global.getInstance();
        questionDatabase = global.questionDatabase;
    }

    public void createQuiz(Integer quizLength) {

        /* Trash any existing iterator. */
        questionListIterator = null;

        questionList.clear();

        questionDatabase.getQuestions(quizLength, questionList);
    }

    public Question getCurrentQuestion() {
        if (currentQuestion == null) {
            return getNextQuestion();
        } else {
            return currentQuestion;
        }
    }

    public Question getNextQuestion() {
        Question question;
        /* If this is the first time we've been called,
         * the iterator has to be created.
         */
        if (questionListIterator == null) {
            questionListIterator = questionList.iterator();
        }

        /* Return the next unanswered Question object from the iterator if possible.
         * If we are out of items, test to make sure that we have another unanswered
         * question available (ie, the user hit 'skip').  If there are more questions,
         * create a new iterator and repeat.
         */
         if (questionListIterator.hasNext()) {
            question = questionListIterator.next();
            /* They already answered this question, call
             * getNextQuestion() to try again.
             */
            if (question.getAttempted()) {
                question = getNextQuestion();
            }
         } else {
            /* Are there still questions remaining?  Reset the
             * iterator and call getNextQuestion recursively.
             */
            if (getUnansweredQuestionsRemaining() > 0) {
                questionListIterator = questionList.iterator();
                question = getNextQuestion();
            } else {
                question = null;
            }
         }
        currentQuestion = question;
        question.shuffleResponses();
        return question;
    }

    /* Figure out how many questions remain unanswered in the deck.
     * This could probably be done slightly more efficiently if I
     * had the deck keep track of how many questions had been answered
     * and had it simply return the variable.  This is simpler to
     * deal with, however, and since the size of the list is
     * guaranteed to be small, I'm not going to worry about it.
     */
    public Integer getUnansweredQuestionsRemaining() {
        Integer questionsRemaining = 0;
        for (Question question : questionList) {
            if (!question.getAttempted()) {
                questionsRemaining++;
            }
        }
        return questionsRemaining;
    }

    public Integer getDeckSize() {
        return questionList.size();
    }
}
