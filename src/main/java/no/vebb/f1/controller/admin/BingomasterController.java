package no.vebb.f1.controller.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.user.PublicUser;
import no.vebb.f1.database.Database;
import no.vebb.f1.user.UserService;

@RestController
@RequestMapping("/api/admin/bingo")
public class BingomasterController {

    private final Database db;
    private final UserService userService;

    public BingomasterController(Database db, UserService userService) {
        this.db = db;
        this.userService = userService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<PublicUser>> getBingomasters() {
        List<PublicUser> bingoMasters = db.getBingomasters().stream()
                .map(PublicUser::new)
                .toList();
        return new ResponseEntity<>(bingoMasters, HttpStatus.OK);
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addBingomaster(@RequestParam("bingomaster") UUID bingomaster) {
        if (userService.loadUser(bingomaster).isPresent()) {
            db.addBingomaster(bingomaster);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/remove")
    @Transactional
    public ResponseEntity<?> removeBingomaster(@RequestParam("bingomaster") UUID bingomaster) {
        if (userService.loadUser(bingomaster).isPresent()) {
            db.removeBingomaster(bingomaster);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
