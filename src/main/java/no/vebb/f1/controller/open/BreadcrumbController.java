package no.vebb.f1.controller.open;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.vebb.f1.util.exception.InvalidRaceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.vebb.f1.database.Database;
import no.vebb.f1.user.User;
import no.vebb.f1.user.UserService;
import no.vebb.f1.util.collection.Breadcrumb;
import no.vebb.f1.util.domainPrimitive.RaceId;

@RestController
public class BreadcrumbController {

	@Autowired
	private Database db;

	@Autowired
	private UserService userService;

	@GetMapping("/api/public/breadcrumbs")
	public ResponseEntity<List<Breadcrumb>> preHandle(@RequestParam("path") String path) {
		if (!path.matches("^/[a-zA-Z0-9/\\-]*")) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		List<Breadcrumb> breadcrumbs = new ArrayList<>();
		if (path.equals("/")) {
			breadcrumbs.add(new Breadcrumb("Hjem", null));
		} else {
			addBreadcrumbs(breadcrumbs, path);
		}
		return new ResponseEntity<>(breadcrumbs, HttpStatus.OK);
	}

	private void addBreadcrumbs(List<Breadcrumb> breadcrumbs, String path) {
		breadcrumbs.add(new Breadcrumb("Hjem", "/"));
		List<String> subPaths = getSubPaths(path);
		subPaths.remove(subPaths.size() - 1);
		for (String subPath : subPaths) {
			breadcrumbs.add(new Breadcrumb(getNameForPath(subPath), subPath));
		}
		breadcrumbs.add(new Breadcrumb(getNameForPath(path), null));
	}

	private List<String> getSubPaths(String path) {
		List<String> result = new ArrayList<>();
		String[] segments = path.split("/");

		StringBuilder currentPath = new StringBuilder();
		for (String segment : segments) {
			if (segment.isEmpty()) {
				continue;
			}
			currentPath.append("/").append(segment);
			result.add(currentPath.toString());
		}

		return result;
	}

	private String getNameForPath(String path) {
		Iterator<String> segments = Arrays.asList(path.split("/")).iterator();
		segments.next(); // First is always blank
        return switch (segments.next()) {
            case "admin" -> getAdminPath(segments);
            case "user" -> getUserPath(segments);
            case "race-guess" -> "Tippet på løp";
            case "score" -> "Poengberegning";
            case "contact" -> "Kontakt";
            case "about" -> "Om siden";
            case "guess" -> getGuessPath(segments);
            case "error" -> "Feil";
            case "settings" -> getSettingsPath(segments);
            case "username" -> "Velg brukernavn";
            case "privacy" -> "Personvernerklæring";
            case "stats" -> getStatsPath(segments);
            case "bingo" -> getBingoPath(segments);
            default -> null;
        };
	}

	private String getAdminPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Admin portal";
		}
		String category = segments.next();
        return switch (category) {
            case "flag" -> getFlagPath(segments);
            case "season" -> getSeasonPath(segments);
            case "bingo" -> "Bingo";
            case "log" -> getLogPath(segments);
            case "backup" -> "Sikkerhetskopi";
            default -> null;
        };
    }

	private String getFlagPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Flagg";
		}
		String year = segments.next();
		if (year.matches("add|delete")) {
			return "";
		}
		if (!segments.hasNext()) {
			try {
				return String.valueOf(Integer.parseInt(year));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return getRaceName(segments);
	}

	private String getLogPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Logg";
		}
		String type = segments.next();
		if (!segments.hasNext()) {
			if (type.length() > 1) {
				return type.substring(0, 1).toUpperCase() + type.substring(1);
			}
			return type;
		}
		String file = segments.next();
		if (!segments.hasNext()) {
			return file;
		}
		return null;
	}

	private String getSeasonPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Sesong";
		}
		String year = segments.next();
		if (!segments.hasNext()) {
			if (year.equals("add")) {
				return "Legg til";
			}
			return year;
		}
		String category = segments.next();
        return switch (category) {
            case "competitors" -> getCompetitorsPath(segments);
            case "points" -> getPointsPath(segments);
            case "cutoff" -> getCutoffPath(segments);
            case "manage" -> getManagePath(segments);
            default -> null;
        };
    }

	private String getCompetitorsPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Deltakere";
		}
        return switch (segments.next()) {
            case "constructors" -> "Konstruktører";
            case "drivers" -> "Sjåfører";
            case "alias" -> "Alternative navn";
            default -> null;
        };
    }
	
	private String getPointsPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Poengsystem";
		}
		return null;
	}

	private String getCutoffPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Frister";
		}
		return null;
	}

	private String getManagePath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Administrer";
		}
		String id = segments.next();
		if (id.matches("reload|move|delete|add")) {
			return "";
		}
		return getRaceName(segments);
	}

	private String getUserPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Brukere";
		}
		String userProfile = segments.next();
		if (userProfile.equals("myprofile")) {
			return "Min profil";
		}
		if (userProfile.equals("compare")) {
			return "Sammenlign";
		}
		try {
			Optional<User> optUser = userService.loadUser(UUID.fromString(userProfile));
			if (optUser.isEmpty()) {
				return "Bruker ikke funnet";
			}
			return optUser.get().username;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private String getGuessPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Tipping";
		}
		String category = segments.next();
        return switch (category) {
            case "drivers" -> "Sjåførmesterskap";
            case "constructors" -> "Konstruktørmesterskap";
            case "tenth" -> "10.plass";
            case "winner" -> "1.plass";
            case "flags" -> "Antall";
            default -> null;
        };
    }

	private String getSettingsPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Innstillinger";
		}
		String setting = segments.next();
        return switch (setting) {
            case "info" -> "Brukerinformasjon";
            case "username" -> "Endre brukernavn";
            case "delete" -> "Slett bruker";
            case "mail" -> "Påminnelser";
            case "referral" -> "Inviter brukere";
            default -> null;
        };
    }

	private String getStatsPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Statistikk";
		}

		String year = segments.next();
		if (!segments.hasNext()) {
			return year;
		}
		return getRaceName(segments);
	}

	private String getRaceName(Iterator<String> segments) {
		String id = segments.next();
		if (!segments.hasNext()) {
			try {
				RaceId raceId = new RaceId(Integer.parseInt(id), db);
				int position = db.getPositionOfRace(raceId);
				String raceName = db.getRaceName(raceId);
				return position + ". " + raceName;
			} catch (InvalidRaceException e) {
				return null;
			}
		}
		return null;
	}

	private String getBingoPath(Iterator<String> segments) {
		if (!segments.hasNext()) {
			return "Bingo";
		}
		String admin = segments.next();
		if (!segments.hasNext()) {
			if (admin.equals("admin")) {
				return "Administrer bingo";
			}
		}
		String year = segments.next();
		if (!segments.hasNext()) {
			return year;
		}
		return null;
	}
}
