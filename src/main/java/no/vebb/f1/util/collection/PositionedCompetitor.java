package no.vebb.f1.util.collection;

public record PositionedCompetitor(String position, String name, String points) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PositionedCompetitor other = (PositionedCompetitor) obj;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (points == null) {
            return other.points == null;
        } else return points.equals(other.points);
    }


}
