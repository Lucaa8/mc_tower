package ch.tower.utils.Scoreboard;

import java.util.ArrayList;
import java.util.Objects;

public class Board
{

    private final String name;

    private final String title;

    private final ArrayList<ScoreboardLine> lines;

    public Board(String name, String title, ArrayList<ScoreboardLine> lines)
    {
        this.name = name;
        this.title = title;
        this.lines = lines;
        if (!this.lines.isEmpty())
            ScoreboardLine.sortLines(this.lines);
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public ArrayList<ScoreboardLine> getLines()
    {
        return lines;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Board that))
            return false;
        return name.equals(that.name);
    }

    public int hashCode()
    {
        return Objects.hash(name);
    }

}
