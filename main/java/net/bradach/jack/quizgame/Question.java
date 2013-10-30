package net.bradach.jack.quizgame;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;



public class Question {

    private String question = null;
    private String response_correct = null;
    private String response_wrong_a = null;
    private String response_wrong_b = null;
    private String response_wrong_c = null;
    private boolean attempted = false;

    private ArrayList< HashMap<QuestionEntries, String>> responses;

    public Question(HashMap<QuestionEntries,String> questionData) {
        /* First, extract the values from the Map we were provided */
        this.question = questionData.get(QuestionEntries.QUESTION);
        this.response_correct = questionData.get(QuestionEntries.RESPONSE_CORRECT);
        this.response_wrong_a = questionData.get(QuestionEntries.RESPONSE_WRONG_A);
        this.response_wrong_b = questionData.get(QuestionEntries.RESPONSE_WRONG_B);
        this.response_wrong_c = questionData.get(QuestionEntries.RESPONSE_WRONG_C);

        responses = new ArrayList<HashMap<QuestionEntries, String>>();

        /* And now add the correct answer and any incorrect answers to the
         * list.  They're shoved into a HashMap so we can figure out which one
         * was the correct answer after randomization.  I'm not doing this in
         * a loop because there are only four entries.
         */
        responses.add(new HashMap<QuestionEntries, String>(){{
            put (QuestionEntries.RESPONSE_CORRECT, response_correct);
        }});

        responses.add(new HashMap<QuestionEntries, String>() {{
            put(QuestionEntries.RESPONSE_WRONG_A, response_wrong_a);
        }});

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

    /* Accessor to get the text of the responses for this question. */
    public String getResponseText(int responseNumber) {
        return (String) responses.get(responseNumber).values().toArray()[0];
    }

    public void setAttempted() {
        attempted = true;
    }
}
