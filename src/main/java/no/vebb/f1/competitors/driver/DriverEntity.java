package no.vebb.f1.competitors.driver;

import jakarta.persistence.*;
import no.vebb.f1.competitors.constructor.ConstructorEntity;
import no.vebb.f1.competitors.domain.Color;
import no.vebb.f1.competitors.domain.Competitor;
import no.vebb.f1.competitors.domain.Driver;
import no.vebb.f1.year.Year;

import java.util.Objects;

@Entity
@Table(name = "drivers")
public class DriverEntity implements Competitor {
    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private DriverId driverId;
    @Embedded
    private Driver driverName;
    @Embedded
    private Year year;
    @Column(name =  "position", nullable = false)
    private int position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", insertable = false, updatable = false)
    private DriverTeamEntity driverTeam;

//    @OneToOne()
//    private ConstructorColorEntity constructorColor;

    protected DriverEntity() {}

    public DriverEntity(DriverId driverId, Driver driverName, Year year, int position){
        this.driverId = driverId;
        this.driverName = driverName;
        this.year = year;
        this.position = position;
    }

    public Driver driverName() {
        return driverName;
    }

    public DriverId driverId() {
        return driverId;
    }

    public Year year() {
        return year;
    }

    public int position() {
        return position;
    }

    public ConstructorEntity team() {
        return driverTeam == null ? null : driverTeam.team();
    }

    public DriverEntity withPosition(int newPosition) {
        return new DriverEntity(driverId, driverName, year, newPosition);
    }

    public Color color() {
//        return constructorColor == null ? null : constructorColor.color();
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DriverEntity that)) return false;
        return position == that.position && Objects.equals(driverId, that.driverId) && Objects.equals(driverName, that.driverName) && Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId, driverName, year, position);
    }
}
