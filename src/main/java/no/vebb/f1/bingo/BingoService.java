package no.vebb.f1.bingo;

import no.vebb.f1.year.Year;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BingoService {

    private final BingomasterRepository bingoMasterRepository;
    private final BingoCardRepository bingoCardRepository;

    public BingoService(BingomasterRepository bingoMasterRepository, BingoCardRepository bingoCardRepository) {
        this.bingoMasterRepository = bingoMasterRepository;
        this.bingoCardRepository = bingoCardRepository;
    }

    public void addBingomaster(UUID userId) {
        bingoMasterRepository.save(new BingomasterEntity(userId));
    }

    public void removeBingomaster(UUID userId) {
        bingoMasterRepository.deleteById(userId);
    }

    public List<BingomasterEntity> getBingomasters() {
        return bingoMasterRepository.findAllByOrderByUserUsername();
    }

    public boolean isBingomaster(UUID userId) {
        return bingoMasterRepository.existsById(userId);
    }

    public List<BingoSquare> getBingoCard(Year year) {
        return bingoCardRepository.findAllByIdYearOrderByIdBingoSquareId(year).stream()
                .map(BingoSquare::fromBingoCardEntity)
                .toList();
    }

    public void addBingoCard(Year year) {
        List<BingoCardEntity> bingoCard = new ArrayList<>();
        for (int id = 0; id < 25; id++) {
            bingoCard.add(new BingoCardEntity(year, id, "", false));
        }
        bingoCardRepository.saveAll(bingoCard);
    }

    public boolean toogleMarkBingoSquare(Year year, int id) {
        Optional<BingoCardEntity> square = bingoCardRepository.findById(new BingoCardId(year, id));
        if (square.isEmpty()) {
            return false;
        }
        bingoCardRepository.save(square.get().toggled());
        return true;
    }

    public boolean setTextBingoSquare(Year year, int id, String text) {
        Optional<BingoCardEntity> square = bingoCardRepository.findById(new BingoCardId(year, id));
        if (square.isEmpty()) {
            return false;
        }
        bingoCardRepository.save(square.get().withText(text));
        return true;
    }

    public boolean isBingoCardAdded(Year year) {
        return bingoCardRepository.existsByIdYear(year);
    }
}
