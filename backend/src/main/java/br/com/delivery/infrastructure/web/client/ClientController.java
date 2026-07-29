package br.com.delivery.infrastructure.web.client;

import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.delivery.application.usecases.order.FindClientOrdersUseCase;
import br.com.delivery.infrastructure.web.dto.FindClientOrdersResponse;
import br.com.delivery.application.dto.order.FindClientOrdersInput;
import br.com.delivery.application.dto.order.FindClientOrdersOutput;
import br.com.delivery.domain.account.AccountId;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final FindClientOrdersUseCase findClientOrdersUseCase;

    public ClientController(FindClientOrdersUseCase findClientOrdersUseCase) {
        this.findClientOrdersUseCase = Objects.requireNonNull(findClientOrdersUseCase);
    }

    @GetMapping("/{accountId}/orders")
    public FindClientOrdersResponse findClientOrders(@PathVariable String accountId) {
        FindClientOrdersInput input = new FindClientOrdersInput(new AccountId(UUID.fromString(accountId)));
        FindClientOrdersOutput output = findClientOrdersUseCase.execute(input);
        return FindClientOrdersResponse.fromOutput(output);
    }
}
