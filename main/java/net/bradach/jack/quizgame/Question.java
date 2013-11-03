package net.bradach.jack.quizgame;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Question {
    /* Keeps track of what question we are. */
    private Integer number;

    private String question = null;
    private String response_correct = null;
    private String response_wrong_a = null;
    private String response_wrong_b = null;
    private String response_wrong_c = null;
    private boolean attempted = false;

    private Integer cheatCount = 0;

    private ArrayList< HashMap<QuestionEntries, String>> responses;

    /* Each question starts at a value of 100 points. */
    private final Integer worth = 100;

    /* Constructor for Question.  The questions / responses are simply passed
     * as Strings.  They're all declared final because I need to access them
     * from an inner class below.  Since the inner class can't be guaranteed
     * that the variables I'm having it use will still exist when it needs them,
     * Java complains.  The solution is to declare the Strings as final, which
     * will let the compiler treat them as constants, albeit it ones that are
     * dynamically generated with the creation of the class.
     */
    public Question(final Integer number,
                    final String question,
                    final String response_correct,
                    final String response_wrong_a,
                    final String response_wrong_b,
                    final String response_wrong_c) {

        responses = new ArrayList<HashMap<QuestionEntries, String>>();

        /* Copy the input variables into our internal ones. */
        this.number = number;
        this.question = question;
        this.response_correct = response_correct;
        this.response_wrong_a = response_wrong_a;
        this.response_wrong_b = response_wrong_b;
        this.response_wrong_c = response_wrong_c;

        /* Add the correct answer and any incorrect answers to the
         * response list.  They're shoved into a HashMap so we can figure
         * out which one was the correct answer after randomization.  I'm
         * not doing this in a loop because there are only four entries.
         */
        responses.add(new HashMap<QuestionEntries, String>(){{
            put (QuestionEntries.RESPONSE_CORRECT, response_correct);
        }});

        responses.add(new HashMap<QuestionEntries, String>() {{
            put(QuestionEntries.RESPONSE_WRONG_A, response_wrong_a);
        }});

        /* Incorrect responses b and c are optional, so a true/false
         * or one-of-three is allowed.
         */
        if(null != response_wrong_b) {
            responses.add(new HashMap<QuestionEntries, String>(){{
                put (QuestionEntries.RESPONSE_WRONG_B, response_wrong_b);
            }});
        }

        if(null != response_wrong_c) {
            responses.add(new HashMap<QuestionEntries, String>(){{
                put (QuestionEntries.RESPONSE_WRONG_C, response_wrong_c);
            }});
        }
    }

    /* Mix up the order of the responses. */
    public void shuffleResponses() {
        long seed = System.nanoTime();
        Collections.shuffle(this.responses, new Random(seed));
    }

    /* Accessor to indicate how many possible answers we have */
    public int responseCount() {
        return this.responses.size();
    }

    /* Check if an response is correct.  The specified HashMap object
     * is pulled from the response list and checked to see if the
     * RESPONSE_CORRECT key is present.  If it is, the correct answer
     * was chosen.  The result of the lookup is directly returned
     * to the caller.
     */
    public boolean isResponseCorrect(int responseNumber) {
        HashMap response;
        response = responses.get(responseNumber);
        return response.containsKey(QuestionEntries.RESPONSE_CORRECT);
    }

    /* Accessor to get text of the question. */
    public String getQuestionText() {
        return question;
    }

    /* Accessor to get the text of the responses for this question.
     * The "cheat" functionality is also wedged in here.  Each of
     * the two cheats zaps an incorrect response.  Since they're
     * shuffled up in the the response deck, I can simply tell it
     * to return 'null' for response_incorrect_b and response_incorrect_c
     * as cheatCount increments.  The caller can use this information to
     * hide the text boxes which return null.
     */
    public String getResponseText(int responseNumber) {
        String responseString;
        HashMap<QuestionEntries, String> questionMap;

        questionMap = responses.get(responseNumber);

        /* Note the lack of breaks in-between cases.  Cascading down through the
         * cases from the one that matches simplifies the logic (and hopefully
         * is easier to follow).  If you have two cheats, there are two
         * possibilities to return a null string.
         */
        switch (cheatCount) {
            case 2:
                if (questionMap.containsKey(QuestionEntries.RESPONSE_WRONG_B)) {
                    return null;
                }
            case 1:
                if (questionMap.containsKey(QuestionEntries.RESPONSE_WRONG_C)) {
                    return null;
                }
            default:
                responseString = (String) responses.get(responseNumber).values().toArray()[0];
        }
        return responseString;
    }

    /* Mutator to mark this question as attempted. */
    public void setAttempted() {
        attempted = true;
    }

    /* Accessor for whether or not this question has been attempted. */
    public boolean getAttempted() {
       return attempted;
    }

    public Integer getNumber() {
        return number;
    }

    /* Method to mark the question as having been cheated on.
     * Cheating will remove one incorrect answer at a cost of half
     * the value of the question.  You only get two cheats (so first
     * cheat drops the point value from 100->50, second will go 50->25).
     * If no more cheats were allowed, this does nothing.
     */
    public void cheat() {
        if (cheatCount < 2) {
            cheatCount++;
        }
    }

    /* If cheats are still available, returns true. */
    public boolean canCheat() {
        return ((cheatCount < 2) ? true : false);
    }

    /* Return this questions current (possibly final) point worth,
     * adjusted based on the number of cheats the user took.
     */
    public Integer getWorth() {
        Integer adjusted_worth = worth;

        for (int i = 0; i < cheatCount; i++) {
            adjusted_worth = adjusted_worth / 2;
        }

        return adjusted_worth;
    }
}
