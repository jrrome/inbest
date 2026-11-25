package gal.usc.etse.inbest.model.dto;

import jakarta.validation.constraints.Size;

public record CreateWalletRequest(
        @Size(max = 100) String name
) {
}
