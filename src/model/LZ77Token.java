package model;

/**
 * Represents a single token used in LZ77 encoding.
 */
public class LZ77Token {
    private int distance;
    private int length;
    private char nextCharacter;

    public LZ77Token(int distance, int length, char nextCharacter) {
        this.distance = distance;
        this.length = length;
        this.nextCharacter = nextCharacter;
    }

    public int getDistance() {
        return distance;
    }

    public int getLength() {
        return length;
    }

    public char getNextCharacter() {
        return nextCharacter;
    }

    @Override
    public String toString() {
        return "<" + distance + "," + length + "," + nextCharacter + ">";
    }
}
