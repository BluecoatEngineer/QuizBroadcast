package net.bradach.jack.quizgame;

import android.media.MediaPlayer;
import android.media.SoundPool;
import java.util.HashMap;

/**
 * Class to store globally-used objects and variables.
 * This is done to avoid having to 'parcelize' complex
 * objects and unpack them on the other end.  Since all
 * activities are running under the same application,
 * they can simply refer to this them in here.  This
 * is also apparently Google's BKM.  This is an
 * eagerly-instantiated singleton.  All users just
 * need to call getInstance.
 */
final public class Global {
    private static final Global instance = new Global();

    /* Accessor for the Global instance */
    public static Global getInstance() {
        return instance;
    }

    /* Handles sound effects */
    public SoundPool soundPool;

    /* Keeps track of how many sounds have been loaded and
     * whether or not all of them have finished.
     */
    public Integer soundsLoadedCount;
    public boolean soundsLoaded;

    /* Maps the enumerated sounds identifiers to whatever
     * the SoundPool assigned them for a resource ID.
     */
    public HashMap<SoundList, Integer> soundMap;

    /* Contains the deck of questions for the quiz-in-progress */
    public QuestionDeck questionDeck;

    /* Wrapper class for pulling questions out of the
     * database (and loading it with new ones).
     */
    public QuestionDatabase questionDatabase;

    /* The MediaPlayer is used for playing the background
     * music during the MenuActivity and QuizActivities
     */
    public MediaPlayer mediaPlayer;
} // Global
