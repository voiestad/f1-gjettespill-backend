package no.vebb.f1.year;

import no.vebb.f1.util.TimeUtil;
import no.vebb.f1.util.exception.InvalidYearException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YearService {

    private final YearRepository yearRepository;
    private final YearFinishedRepository yearFinishedRepository;

    public YearService(YearRepository yearRepository, YearFinishedRepository yearFinishedRepository) {
        this.yearRepository = yearRepository;
        this.yearFinishedRepository = yearFinishedRepository;
    }

    public Year getCurrentYear() {
        return yearRepository.findById(new Year(TimeUtil.getCurrentYear())).orElseThrow(InvalidYearException::new).year();
    }

    public Year getYear(int year) {
        return yearRepository.findById(new Year(year)).orElseThrow(InvalidYearException::new).year();
    }

    public List<Year> getAllYears() {
        return yearRepository.findAllByOrderByYearDesc().stream()
                .map(YearEntity::year)
                .toList();
    }

    public void addYear(int year) {
        yearRepository.save(new YearEntity(year));
    }

    public boolean isFinishedYear(Year year) {
        return yearFinishedRepository.existsById(year);
    }

    public void finalizeYear(Year year) {
        yearFinishedRepository.save(new YearFinishedEntity(year));
    }
}
