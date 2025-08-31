package no.vebb.f1.bingo;

import no.vebb.f1.user.UserEntity;
import no.vebb.f1.user.UserRespository;
import no.vebb.f1.util.collection.BingoSquare;
import no.vebb.f1.util.domainPrimitive.Year;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BingoService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRespository userRespository;

    public BingoService(JdbcTemplate jdbcTemplate, UserRespository userRespository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRespository = userRespository;
    }

    public void addBingomaster(UUID userId) {
        final String sql = "INSERT INTO bingomasters (user_id) VALUES (?) ON CONFLICT DO NOTHING;";
        jdbcTemplate.update(sql, userId);
    }

    public void removeBingomaster(UUID userId) {
        final String sql = "DELETE FROM bingomasters WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }

    public List<UserEntity> getBingomasters() {
        final String getAllUsersSql = """
                SELECT u.user_id AS id, u.username AS username, u.google_id AS google_id
                FROM users u
                JOIN bingomasters bm ON u.user_id = bm.user_id
                ORDER BY u.username;
                """;
        return jdbcTemplate.queryForList(getAllUsersSql).stream()
                .map(row -> userRespository.findById((UUID) row.get("id")))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public boolean isBingomaster(UUID userId) {
        final String sql = "SELECT COUNT(*) FROM bingomasters WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
    }

    public List<BingoSquare> getBingoCard(Year year) {
        final String sql = """
                SELECT year, bingo_square_id, square_text, marked
                FROM bingo_cards
                WHERE year = ?
                ORDER BY bingo_square_id;
                """;
        return jdbcTemplate.queryForList(sql, year.value).stream()
                .map(row ->
                        new BingoSquare(
                                (String) row.get("square_text"),
                                (boolean) row.get("marked"),
                                (int) row.get("bingo_square_id"),
                                new Year((int) row.get("year"))
                        )
                ).toList();
    }

    public BingoSquare getBingoSquare(Year year, int id) {
        final String sql = """
                SELECT year, bingo_square_id, square_text, marked
                FROM bingo_cards
                WHERE year = ? AND bingo_square_id = ?;
                """;
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, year.value, id);
        return new BingoSquare(
                (String) row.get("square_text"),
                (boolean) row.get("marked"),
                (int) row.get("bingo_square_id"),
                new Year((int) row.get("year"))
        );
    }

    public void addBingoSquare(BingoSquare bingoSquare) {
        final String sql = """
                INSERT INTO bingo_cards
                (year, bingo_square_id, square_text, marked)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (year, bingo_square_id)
                DO UPDATE SET square_text = EXCLUDED.square_text, marked = EXCLUDED.marked;
                """;
        jdbcTemplate.update(
                sql,
                bingoSquare.year().value,
                bingoSquare.id(),
                bingoSquare.text(),
                bingoSquare.marked()
        );
    }

    public void toogleMarkBingoSquare(Year year, int id) {
        BingoSquare bingoSquare = getBingoSquare(year, id);
        boolean newMark = !bingoSquare.marked();
        final String sql = """
                UPDATE bingo_cards
                SET marked = ?
                WHERE year = ? AND bingo_square_id = ?;
                """;
        jdbcTemplate.update(sql, newMark, year.value, id);
    }

    public void setTextBingoSquare(Year year, int id, String text) {
        final String sql = """
                UPDATE bingo_cards
                SET square_text = ?
                WHERE year = ? AND bingo_square_id = ?;
                """;
        jdbcTemplate.update(sql, text, year.value, id);
    }

    public boolean isBingoCardAdded(Year year) {
        final String sql = """
                SELECT COUNT(*)
                FROM bingo_cards
                WHERE year = ?;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, year.value) > 0;
    }
}
