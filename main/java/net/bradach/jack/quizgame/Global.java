package net.bradach.jack.quizgame;

import android.media.SoundPool;

/**
 * Class to store globally-used objects and variables.
 * This is done to avoid having to 'parcelize' complex
 * objects and unpack them on the other end.  Since all
 * activities are running under the same application,
 * they can simply refer to this them in here.
 *
 *
 */
final public class Global {
    private static Global instance = new Global();

    /* Accessor for the Global instance */
    public static Global getInstance() {
        return instance;
    }

    public SoundPool soundPool;
    public QuestionDeck questionDeck;
    public QuestionDatabase questionDatabase;
    public Integer soundsLoaded;
}
