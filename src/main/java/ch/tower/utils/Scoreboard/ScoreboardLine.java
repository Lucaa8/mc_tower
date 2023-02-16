package ch.tower.utils.Scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class ScoreboardLine
{

    private final int line;

    private String text;

    private ScoreboardLine(int line, String text)
    {
        this.line = line;
        this.text = text;
    }

    public int getLine()
    {
        return line;
    }

    //So we can access it in PlayerBoard (When we need to send a line update) but not everywhere else.
    //Important because others devs will think they can change a client-side line with this setter but no!!!! Use PlayerBoard#updateLines
    protected void setText(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }

    public String toString()
    {
        return "ScoreboardLine{line=" + line + ", text='" + text + "'}";
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof ScoreboardLine that))
            return false;
        return (line == that.line && text.equals(that.text));
    }

    public int hashCode()
    {
        return Objects.hash(line, text);
    }

    public static void sortLines(ArrayList<ScoreboardLine> lines)
    {
        lines.sort(Comparator.comparingInt(l -> l.line));
    }

    public static class LinesBuilder
    {
        private final ArrayList<ScoreboardLine> lines = new ArrayList<>();

        public LinesBuilder add(int line, String text)
        {
            for (ScoreboardLine l : lines)
            {
                if (l.line == line)
                {
                    l.text = text;
                    return this;
                }
            }
            lines.add(new ScoreboardLine(line, text));
            return this;
        }

        public ArrayList<ScoreboardLine> getLines()
        {
            return lines;
        }
    }

}
