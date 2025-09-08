package no.vebb.f1.scoring;

import no.vebb.f1.util.domainPrimitive.Category;
import no.vebb.f1.util.domainPrimitive.Diff;
import no.vebb.f1.util.domainPrimitive.Points;
import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreService {
    private final DiffPointsMapRepository diffPointsMapRepository;

    public ScoreService(DiffPointsMapRepository diffPointsMapRepository) {
        this.diffPointsMapRepository = diffPointsMapRepository;
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
        List<DiffPointsMapEntity> diffs = diffPointsMapRepository.findAllByIdYearAndIdCategoryNameOrderByIdDiff(year.value, category.value);
        if (diffs.isEmpty()) {
            return new Diff();
        }
        return new Diff(diffs.get(diffs.size() - 1).diff());
    }

    public void addDiffToPointsMap(Category category, Diff diff, Year year) {
        DiffPointsMapEntity diffPointsMapEntity = new DiffPointsMapEntity(category.value, diff.value, year.value, 0);
        diffPointsMapRepository.save(diffPointsMapEntity);
    }

    public void removeDiffToPointsMap(Category category, Diff diff, Year year) {
        DiffPointsMapId diffPointsMapId = new DiffPointsMapId(category.value, diff.value, year.value);
        diffPointsMapRepository.deleteById(diffPointsMapId);
    }

    public void setNewDiffToPointsInPointsMap(Category category, Diff diff, Year year, Points points) {
        DiffPointsMapEntity diffPointsMapEntity = new DiffPointsMapEntity(category.value, diff.value, year.value, points.value);
        diffPointsMapRepository.save(diffPointsMapEntity);
    }

    public boolean isValidDiffInPointsMap(Category category, Diff diff, Year year) {
        DiffPointsMapId diffPointsMapId = new DiffPointsMapId(category.value, diff.value, year.value);
        return diffPointsMapRepository.existsById(diffPointsMapId);
    }

}
