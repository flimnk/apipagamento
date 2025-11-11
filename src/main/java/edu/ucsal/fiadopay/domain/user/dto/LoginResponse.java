package edu.ucsal.fiadopay.domain.user.dto;

public record LoginResponse(
        String token,
        long expires_in,
        UserResponse userResponse
) {

}