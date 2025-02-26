package no.vebb.f1.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.Cutoff;
import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.domainPrimitive.RaceId;
import no.vebb.f1.util.domainPrimitive.Year;
import no.vebb.f1.util.exception.InvalidYearException;
import no.vebb.f1.util.exception.NoAvailableRaceException;

@Component
public class HeaderInterceptor  implements HandlerInterceptor  {

	@Autowired
	private Database db;

	@Autowired
	private Cutoff cutoff;

	@Autowired
	private UserService userService;

	@SuppressWarnings("null")
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		request.setAttribute("loggedOut", !userService.isLoggedIn());
		request.setAttribute("raceGuess", isRaceGuess());
		request.setAttribute("isAdmin", userService.isAdmin());
		request.setAttribute("isAbleToGuess", cutoff.isAbleToGuessCurrentYear() || isRaceToGuess());
		return true;
	}

	private boolean isRaceGuess() {
		try {
			Year year = new Year(TimeUtil.getCurrentYear(), db);
			RaceId raceId = db.getLatestRaceForPlaceGuess(year).id;
			return !cutoff.isAbleToGuessRace(raceId);
		} catch (InvalidYearException e) {
			return false;
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (NoAvailableRaceException e) {
			return false;
		}
	}

	private boolean isRaceToGuess() {
		try {
			RaceId raceId = db.getCurrentRaceIdToGuess();
			return cutoff.isAbleToGuessRace(raceId);
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (NoAvailableRaceException e) {
			return false;
		}
	}
}
