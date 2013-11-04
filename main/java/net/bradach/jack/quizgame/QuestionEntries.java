package net.bradach.jack.quizgame;

/**
 * Types of fields in a question.  These correspond to
 * the fields in the database.  Each question is
 * comprised of fives strings: a question, a correct
 * answer, and up to three incorrect responses.
 */
public enum QuestionEntries {
    QUESTION,
    RESPONSE_CORRECT,
    RESPONSE_WRONG_A,
    RESPONSE_WRONG_B,
    RESPONSE_WRONG_C
}
