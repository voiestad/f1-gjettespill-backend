package no.vebb.f1.scoring;

import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreService {
    private final DiffPointsMapRepository diffPointsMapRepository;
    private final JdbcTemplate jdbcTemplate;

    public ScoreService(DiffPointsMapRepository diffPointsMapRepository, JdbcTemplate jdbcTemplate) {
        this.diffPointsMapRepository = diffPointsMapRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Diff, Points> getDiffPointsMap(Year year, Category category) {
        List<DiffPointsMapEntity> res = diffPointsMapRepository.findAllByIdYearAndIdCategoryNameOrderByIdDiff(year.value, category.value);

        Map<Diff, Points> map = new LinkedHashMap<>();
        for (DiffPointsMapEntity entry : res) {
            Diff diff = new Diff(entry.diff());
            Points points = new Points(entry.points());
            map.put(diff, points);
        }
        return map;
    }


    public Diff getMaxDiffInPointsMap(Year year, Category category) {
        final String getMaxDiff = "SELECT MAX(diff) FROM diff_points_mappings WHERE year = ? AND category_name = ?;";
        return new Diff(jdbcTemplate.queryForObject(getMaxDiff, Integer.class, year.value, category.value));
    }

    public void addDiffToPointsMap(Category category, Diff diff, Year year) {
        final String addDiff = "INSERT INTO diff_points_mappings (category_name, diff, points, year) VALUES (?, ?, ?, ?);";
        jdbcTemplate.update(addDiff, category.value, diff.value, 0, year.value);
    }

    public void removeDiffToPointsMap(Category category, Diff diff, Year year) {
        final String deleteRowWithDiff = "DELETE FROM diff_points_mappings WHERE year = ? AND category_name = ? AND diff = ?;";
        jdbcTemplate.update(deleteRowWithDiff, year.value, category.value, diff.value);
    }

    public void setNewDiffToPointsInPointsMap(Category category, Diff diff, Year year, Points points) {
        final String setNewPoints = """
                UPDATE diff_points_mappings
                SET points = ?
                WHERE diff = ? AND year = ? AND category_name = ?;
                """;
        jdbcTemplate.update(setNewPoints, points.value, diff.value, year.value, category.value);
    }

    public boolean isValidDiffInPointsMap(Category category, Diff diff, Year year) {
        final String validateDiff = "SELECT COUNT(*) FROM diff_points_mappings WHERE year = ? AND category_name = ? AND diff = ?;";
        return jdbcTemplate.queryForObject(validateDiff, Integer.class, year.value, category.value, diff.value) > 0;
    }

}
