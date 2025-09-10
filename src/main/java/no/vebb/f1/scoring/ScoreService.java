package no.vebb.f1.scoring;

import no.vebb.f1.guessing.category.Category;
import no.vebb.f1.scoring.diffPointsMap.DiffPointsMapEntity;
import no.vebb.f1.scoring.diffPointsMap.DiffPointsMapId;
import no.vebb.f1.scoring.diffPointsMap.DiffPointsMapRepository;
import no.vebb.f1.scoring.domain.Diff;
import no.vebb.f1.placement.domain.UserPoints;
import no.vebb.f1.year.Year;
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

    public Map<Diff, UserPoints> getDiffPointsMap(Year year, Category category) {
        List<DiffPointsMapEntity> res = diffPointsMapRepository.findAllByIdYearAndIdCategoryNameOrderByIdDiff(year, category);

        Map<Diff, UserPoints> map = new LinkedHashMap<>();
        for (DiffPointsMapEntity entry : res) {
            Diff diff = entry.diff();
            UserPoints points = entry.points();
            map.put(diff, points);
        }
        return map;
    }

    public Diff getMaxDiffInPointsMap(Year year, Category category) {
        List<DiffPointsMapEntity> diffs = diffPointsMapRepository.findAllByIdYearAndIdCategoryNameOrderByIdDiff(year, category);
        if (diffs.isEmpty()) {
            return new Diff();
        }
        return diffs.get(diffs.size() - 1).diff();
    }

    public void addDiffToPointsMap(Category category, Diff diff, Year year) {
        DiffPointsMapEntity diffPointsMapEntity = new DiffPointsMapEntity(category, diff, year, new UserPoints());
        diffPointsMapRepository.save(diffPointsMapEntity);
    }

    public void removeDiffToPointsMap(Category category, Diff diff, Year year) {
        DiffPointsMapId diffPointsMapId = new DiffPointsMapId(category, diff, year);
        diffPointsMapRepository.deleteById(diffPointsMapId);
    }

    public void setNewDiffToPointsInPointsMap(Category category, Diff diff, Year year, UserPoints points) {
        DiffPointsMapEntity diffPointsMapEntity = new DiffPointsMapEntity(category, diff, year, points);
        diffPointsMapRepository.save(diffPointsMapEntity);
    }

    public boolean isValidDiffInPointsMap(Category category, Diff diff, Year year) {
        DiffPointsMapId diffPointsMapId = new DiffPointsMapId(category, diff, year);
        return diffPointsMapRepository.existsById(diffPointsMapId);
    }

}
