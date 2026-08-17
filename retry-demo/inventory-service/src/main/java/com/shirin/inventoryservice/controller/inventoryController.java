package com.shirin.inventoryservice.controller;

import com.shirin.inventoryservice.dto.ReservationMode;
import com.shirin.inventoryservice.dto.ReserveInventoryRequest;
import com.shirin.inventoryservice.dto.ReserveInventoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/inventory")
public class inventoryController {


    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReserveInventoryResponse reserve(
            @Valid @RequestBody ReserveInventoryRequest request,
            @RequestParam(defaultValue = "SUCCESS") ReservationMode mode
    ) throws InterruptedException {

       return switch (mode){
            case SUCCESS -> reserveSuccessfully(request);
            case REJECT -> rejectReservation(request);
            case SERVICE_UNAVAILABLE -> throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Inventory Service is temporarily unavailable"
            );
            case SLOW -> slowReservation(request);
        };
    }

    private ReserveInventoryResponse reserveSuccessfully(ReserveInventoryRequest request) {
        return new ReserveInventoryResponse(
                "res-" + request.productId(),
                "RESERVED"
        );
    }

    private ReserveInventoryResponse rejectReservation(ReserveInventoryRequest request) {
        return new ReserveInventoryResponse(
                null,
                "NOT_RESERVED"
        );
    }

    private ReserveInventoryResponse slowReservation(ReserveInventoryRequest request) throws InterruptedException {

        Thread.sleep(5000);

        return new ReserveInventoryResponse(
                "res-" + request.productId(),
                "RESERVED"
        );
    }

}
