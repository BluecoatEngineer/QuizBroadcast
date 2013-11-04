package net.bradach.jack.quizgame;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds the deck of questions for the current quiz and talks to the
 * question database to populate itself when needed.
 */
final public class QuestionDeck {
    private static final String TAG = "QuestionDeck";
    private final QuestionDatabase questionDatabase;

    private final ArrayList<Question> questionList;
    private Iterator<Question> questionListIterator = null;
    private Question currentQuestion = null;

    /**
     * When a QuestionDeck is instantiated, it initializes the "deck"
     * (array of questions) and finds a handle to the QuestionDatabase.
     */
    public QuestionDeck() {
        /* Create the list object */
        questionList = new ArrayList<Question>();
        questionDatabase = Global.getInstance().questionDatabase;
    }

    /**
     * Create a new quiz of a given length, storing it in the deck.
     *
     * @param quizLength Length of the new quiz.
     */
    public void createQuiz(Integer quizLength) {
        /* Trash any existing iterator. */
        questionListIterator = null;

        /* Flush the list... */
        questionList.clear();

        /* ...and reload it with new questions */
        questionDatabase.getQuestions(quizLength, questionList);
    }

    /**
     * Returns the current question (or, in the case that
     * the quiz hasn't started, the first question from
     * the list iterator).
     */
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
        assert question != null;
        question.shuffleResponses();
        return question;
    } // getNextQuestion

    /**
     * Figure out how many questions remain unanswered in the deck.
     * This could probably be done slightly more efficiently if I
     * had the deck keep track of how many questions had been answered
     * and had it simply return the variable.  This is simpler to
     * deal with, however, and since the size of the list is
     * guaranteed to be small, I'm not going to worry about it.
     *
     * @return number of questions without responses in the deck.
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

    /**
     * Return the number of questions in this deck.
     *
     * @return Size of the deck.
     */
    public Integer getDeckSize() {
        return questionList.size();
    }
}
