insert into drivers (driver_name, year, position) select * from drivers_year;
insert into constructors (constructor_name, year, position) select * from constructors_year;

alter table drivers_team rename to drivers_team_old;

CREATE TABLE drivers_team (
    driver_id INTEGER PRIMARY KEY,
    constructor_id INTEGER NOT NULL,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id),
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE
);

insert into drivers_team (driver_id, constructor_id)
    select d.driver_id, c.constructor_id from drivers_team_old dto
        join drivers d on dto.driver_name = d.driver_name and dto.year = d.year
        join constructors c on dto.constructor_name = c.constructor_name and dto.year = c.year;

select d.driver_name, c.constructor_name from drivers_team dt
    join drivers d on dt.driver_id = d.driver_id
    join constructors c on c.constructor_id = dt.constructor_id;

drop table drivers_team_old;

alter table constructors_color rename to constructors_color_old;

CREATE TABLE constructors_color (
    constructor_id INTEGER PRIMARY KEY,
    color TEXT NOT NULL,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE
);

insert into constructors_color (constructor_id, color)
    select c.constructor_id, cto.color from constructors_color_old cto
        join constructors c on c.constructor_name = cto.constructor_name and c.year = cto.year;

select c.constructor_name, cc.color from constructors_color cc
    join constructors c on c.constructor_id = cc.constructor_id;

drop table constructors_color_old;

alter table driver_standings rename to driver_standings_old;

CREATE TABLE driver_standings (
    race_id INTEGER NOT NULL,
    driver_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, driver_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE
);

insert into driver_standings (race_id, driver_id, position, points)
    select dso.race_id, d.driver_id, dso.position, dso.points from driver_standings_old dso
        join race_order ro on dso.race_id = ro.race_id
        join drivers d on d.driver_name = dso.driver_name and ro.year = d.year;

select ro.position, ds.position, d.driver_name, ds.points from driver_standings ds
    join drivers d on ds.driver_id = d.driver_id
    join race_order ro on ro.race_id = ds.race_id
    order by ro.position, ds.position;

drop table driver_standings_old;

alter table constructor_standings rename to constructor_standings_old;

CREATE TABLE constructor_standings (
    race_id INTEGER NOT NULL,
    constructor_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, constructor_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE
);

insert into constructor_standings (race_id, constructor_id, position, points)
select cso.race_id, c.constructor_id, cso.position, cso.points from constructor_standings_old cso
    join race_order ro on cso.race_id = ro.race_id
    join constructors c on c.constructor_name = cso.constructor_name and ro.year = c.year;

select ro.position, ds.position, c.constructor_name, ds.points from constructor_standings ds
    join constructors c on ds.constructor_id = c.constructor_id
    join race_order ro on ro.race_id = ds.race_id
    order by ro.position, ds.position;

drop table constructor_standings_old;

alter table race_results rename to race_results_old;

CREATE TABLE race_results (
    race_id INTEGER NOT NULL,
    qualified_position TEXT NOT NULL,
    position INTEGER NOT NULL,
    driver_id INTEGER NOT NULL,
    points INTEGER NOT NULL,
    PRIMARY KEY (race_id, position),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE
);

insert into race_results (race_id, qualified_position, position, driver_id, points)
    select rro.race_id, rro.qualified_position, rro.position, d.driver_id, rro.points from race_results_old rro
    join race_order ro on ro.race_id = rro.race_id
    join drivers d on d.driver_name = rro.driver_name and ro.year = d.year;

select ro.position, rr.position, d.driver_name from race_results rr
    join race_order ro on ro.race_id = rr.race_id
    join drivers d on d.driver_id = rr.driver_id
    order by ro.position, rr.position;

drop table race_results_old;

alter table starting_grids rename to starting_grids_old;

CREATE TABLE starting_grids (
    race_id INTEGER NOT NULL,
    position INTEGER NOT NULL,
    driver_id INTEGER NOT NULL,
    PRIMARY KEY (race_id, driver_id),
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE
);

insert into starting_grids (race_id, position, driver_id)
    select sgo.race_id, sgo.position, d.driver_id from starting_grids_old sgo
    join race_order ro on ro.race_id = sgo.race_id
    join drivers d on d.driver_name = sgo.driver_name and ro.year = d.year;

select ro.position, sg.position, d.driver_name from starting_grids sg
    join race_order ro on sg.race_id = ro.race_id
    join drivers d on d.driver_id = sg.driver_id and d.year = ro.year
    order by ro.position, sg.position;

drop table starting_grids_old;

alter table constructor_guesses rename to constructor_guesses_old;

CREATE TABLE constructor_guesses (
    user_id UUID NOT NULL,
    constructor_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (user_id, position, year),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (constructor_id) REFERENCES constructors(constructor_id) ON DELETE CASCADE,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);

insert into constructor_guesses (user_id, constructor_id, year, position)
    select cgo.user_id, c.constructor_id, cgo.year, cgo.position from constructor_guesses_old cgo
    join constructors c on c.year = cgo.year and c.constructor_name = cgo.constructor_name;

select cg.user_id, c.constructor_name, cg.position from constructor_guesses cg
    join constructors c on cg.constructor_id = c.constructor_id
    order by cg.user_id, cg.position;

drop table constructor_guesses_old;

alter table driver_guesses rename to driver_guesses_old;

CREATE TABLE driver_guesses (
    user_id UUID NOT NULL,
    driver_id INTEGER NOT NULL,
    year INTEGER NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (user_id, position, year),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (year) REFERENCES years(year) ON DELETE CASCADE
);

insert into driver_guesses (user_id, driver_id, year, position)
    select dgo.user_id, d.driver_id, dgo.year, dgo.position from driver_guesses_old dgo
    join drivers d on d.year = dgo.year and d.driver_name = dgo.driver_name;

select dg.user_id, d.driver_name, dg.position from driver_guesses dg
    join drivers d on dg.driver_id = d.driver_id
    order by dg.user_id, dg.position;

drop table driver_guesses_old;

alter table driver_place_guesses rename to driver_place_guesses_old;

CREATE TABLE driver_place_guesses (
    user_id UUID NOT NULL,
    race_id INTEGER NOT NULL,
    category_name TEXT NOT NULL,
    driver_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, race_id, category_name),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id) ON DELETE CASCADE,
    FOREIGN KEY (race_id) REFERENCES races(race_id) ON DELETE CASCADE
);

insert into driver_place_guesses (user_id, race_id, category_name, driver_id)
    select dpgo.user_id, dpgo.race_id, dpgo.category_name, d.driver_id from driver_place_guesses_old dpgo
    join race_order ro on ro.race_id = dpgo.race_id
    join drivers d on d.driver_name = dpgo.driver_name and d.year = ro.year;

select ro.position, dpg.user_id, dpg.category_name, d.driver_name from driver_place_guesses dpg
    join race_order ro on dpg.race_id = ro.race_id
    join drivers d on dpg.driver_id = d.driver_id
    order by ro.position;

drop table driver_place_guesses_old;

update drivers set driver_name = 'Kimi Antonelli' where driver_name = 'Andrea Kimi Antonelli';
drop table drivers_alternative_name;
