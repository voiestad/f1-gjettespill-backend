package no.vebb.f1.controller.admin;

import java.util.List;
import java.util.UUID;

import no.vebb.f1.bingo.BingoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import no.vebb.f1.user.PublicUserDto;
import no.vebb.f1.user.UserService;

@RestController
@RequestMapping("/api/admin/bingo")
public class BingomasterController {

    private final BingoService bingoService;
    private final UserService userService;

    public BingomasterController(BingoService bingoService, UserService userService) {
        this.bingoService = bingoService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<PublicUserDto>> getBingomasters() {
        List<PublicUserDto> bingoMasters = bingoService.getBingomasters().stream()
                .map(PublicUserDto::fromEntity)
                .toList();
        return new ResponseEntity<>(bingoMasters, HttpStatus.OK);
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addBingomaster(@RequestParam("bingomaster") UUID bingomaster) {
        if (userService.loadUser(bingomaster).isPresent()) {
            bingoService.addBingomaster(bingomaster);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/remove")
    @Transactional
    public ResponseEntity<?> removeBingomaster(@RequestParam("bingomaster") UUID bingomaster) {
        if (userService.loadUser(bingomaster).isPresent()) {
            bingoService.removeBingomaster(bingomaster);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
