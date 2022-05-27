package cinema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

public class Theater {
    private final int rows;
    private final int cols;
    private List<Seat> seats;

    public Theater(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        initSeats();
    }

    public Map<String, Object> getAvailableSeats() {
        Map<String, Object> result = new HashMap<>();
        result.put("total_rows", rows);
        result.put("total_columns", cols);
        result.put("available_seats",
                seats.stream()
                        .filter(s -> !s.isSold())
                        .collect(Collectors.toList()));

        return result;
    }

    public Map<String, Object> purchase(Map<String, Integer> order) {
        if (!order.containsKey("row") || !order.containsKey("column")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong body format");
        }
        int row = order.get("row");
        int col = order.get("column");
        if (row <= 0 || row > rows || col <= 0 || col > cols) {
            throw new TheaterException("The number of a row or a column is out of bounds!");
        }
        Seat seat = seats.stream()
                .filter(s -> s.getRow() == row && s.getColumn() == col)
                .filter(s -> !s.isSold())
                .findAny().orElseThrow(() -> new TheaterException("The ticket has been already purchased!"));
        seat.setSold();
        return Map.of(
                "token", seat.getReturnToken(),
                "ticket", seat
        );
    }

    public Map<String, Object> returnTicket(Map<String, String> returnData) {
        if (!returnData.containsKey("token")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong body format");
        }
        String token = returnData.get("token");
        Seat seat = seats.stream()
                .filter(s -> token.equals(s.getReturnToken()))
                .findAny().orElseThrow(() -> new TheaterException("Wrong token!"));
        seat.setReturned();
        return Map.of(
                "returned_ticket", Map.of(
                        "row", seat.getRow(),
                        "column", seat.getColumn(),
                        "price", seat.getPrice()
                )
        );
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "current_income", getIncome(),
                "number_of_available_seats", getNumberOfAvailable(),
                "number_of_purchased_tickets", getNumberOfSold()
        );

    }

    private void initSeats() {
        seats = new ArrayList<>();
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                seats.add(new Seat(i, j));
            }
        }
    }

    private long getIncome() {
        return seats.stream()
                .filter(Seat::isSold)
                .mapToLong(Seat::getPrice)
                .sum();
    }

    private long getNumberOfSold() {
        return seats.stream()
                .filter(Seat::isSold)
                .count();
    }

    private long getNumberOfAvailable() {
        return seats.stream()
                .filter(s -> !s.isSold())
                .count();
    }
}

class Seat {
    private final int row;
    private final int column;
    @JsonIgnore
    private boolean sold = false;
    @JsonIgnore
    String returnToken;
    private int price;

    public Seat(int row, int column) {
        this.row = row;
        this.column = column;
        setPrice();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getPrice() {
        return price;
    }

    public String getReturnToken() {
        return returnToken;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold() {
        sold = true;
        returnToken = String.valueOf(UUID.randomUUID());
    }

    public void setReturned() {
        sold = false;
        returnToken = null;
    }

    private void setPrice() {
        price = row <= 4 ? 10 : 8;
    }
}
