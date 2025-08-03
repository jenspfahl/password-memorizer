package de.jepfa.obfusser.util.encrypt.hints;

public class HintChar {

    private char hint;
    private boolean goNext = false;


    public HintChar(char hint, boolean goNext) {
        this.hint = hint;
        this.goNext = goNext;
    }

    public HintChar(char hint) {
        this.hint = hint;
    }

    public char getHint() {
        return hint;
    }

    public boolean isGoNext() {
        return goNext;
    }

    protected void setHint(char hint) {
        this.hint = hint;
    }

    protected void setGoNext(boolean goNext) {
        this.goNext = goNext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !o.getClass().isAssignableFrom(HintChar.class)) return false; // allow supertypes

        HintChar that = (HintChar) o;

        return hint == that.hint;
    }

    @Override
    public int hashCode() {
        return hint;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HintChar{");
        sb.append("hint='").append(hint).append('\'');
        sb.append(", goNext=").append(goNext);
        sb.append('}');
        return sb.toString();
    }

}
