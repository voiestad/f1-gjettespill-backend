package no.vebb.f1.year;

import no.vebb.f1.util.domainPrimitive.Year;
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

    public List<Year> getAllYears() {
        return yearRepository.findAllByOrderByYearDesc().stream()
                .map(YearEntity::year)
                .map(Year::new)
                .toList();
    }

    public boolean isValidSeason(int year) {
        return yearRepository.existsById(year);
    }

    public void addYear(int year) {
        yearRepository.save(new YearEntity(year));
    }

    public boolean isFinishedYear(Year year) {
        return yearFinishedRepository.existsById(year.value);
    }

    public void finalizeYear(Year year) {
        yearFinishedRepository.save(new YearFinishedEntity(year.value));
    }
}
