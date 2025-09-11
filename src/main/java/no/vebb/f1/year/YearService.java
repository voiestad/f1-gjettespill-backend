package no.vebb.f1.year;

import no.vebb.f1.util.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class YearService {

    private final YearRepository yearRepository;
    private final YearFinishedRepository yearFinishedRepository;

    public YearService(YearRepository yearRepository, YearFinishedRepository yearFinishedRepository) {
        this.yearRepository = yearRepository;
        this.yearFinishedRepository = yearFinishedRepository;
    }

    public Optional<Year> getCurrentYear() {
        return yearRepository.findById(new Year(TimeUtil.getCurrentYear())).map(YearEntity::year);
    }

    public Optional<Year> getYear(int year) {
        return yearRepository.findById(new Year(year)).map(YearEntity::year);
    }

    public List<Year> getAllYears() {
        return yearRepository.findAllByOrderByYearDesc().stream()
                .map(YearEntity::year)
                .toList();
    }

    public Year addYear(int year) {
        yearRepository.save(new YearEntity(year));
        return new Year(year);
    }

    public boolean isFinishedYear(Year year) {
        return yearFinishedRepository.existsById(year);
    }

    public void finalizeYear(Year year) {
        yearFinishedRepository.save(new YearFinishedEntity(year));
    }
}
