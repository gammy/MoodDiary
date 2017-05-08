package nu.ere.mooddiary;

public final class MoodValues {
    static int DOWN_SEVERE    = -3; // 0 (progress values)
    static int DOWN_DIFFICULT = -2; // 1
    static int DOWN_MILD      = -1; // 2
    static int NORMAL         =  0; // 3
    static int UP_MILD        =  1; // 4
    static int UP_DIFFICULT   =  2; // 5
    static int UP_SEVERE      =  3; // 6

    public int mapToProgress(int progress) {
        return progress - 3;
    }
}
