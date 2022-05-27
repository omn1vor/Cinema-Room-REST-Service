package cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}

@RestController
class Controller {
    private final Theater theater;

    public Controller() {
        theater = new Theater(9, 9);
    }

    @GetMapping("/seats")
    public Map<String, Object> getAvailableSeats() {
        return theater.getAvailableSeats();
    }

    @PostMapping("/purchase")
    public Map<String, Object> purchase(@RequestBody Map<String, Integer> order) {
        return theater.purchase(order);
    }

    @PostMapping("/return")
    public Map<String, Object> returnTicket(@RequestBody Map<String, String> returnData) {
        return theater.returnTicket(returnData);
    }

    @PostMapping("/stats")
    public Map<String, Object> stats(@RequestParam(required = false) String password) {
        Security.checkAdminAccess(password);
        return theater.getStats();
    }
}

class Security {
    private static final String adminPass = "super_secret";

    public static void checkAdminAccess(String password) {
        if (!adminPass.equals(password)) {
            throw new TheaterException("The password is wrong!", HttpStatus.UNAUTHORIZED);
        }
    }
}

@ControllerAdvice
class ApplicationExceptionHandler {
    @ExceptionHandler(TheaterException.class)
    public ResponseEntity<Object> handleException(TheaterException e) {
        String reason = Optional.ofNullable(e.getReason()).orElse("");
        return new ResponseEntity<>(
                Map.of("error", reason),
                e.getStatus());
    }
}

class TheaterException extends ResponseStatusException {
    public TheaterException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }

    public TheaterException(String reason,  HttpStatus status) {
        super(status, reason);
    }
}
