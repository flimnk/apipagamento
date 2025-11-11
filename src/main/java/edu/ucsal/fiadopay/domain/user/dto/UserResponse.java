package edu.ucsal.fiadopay.domain.user.dto;

import edu.ucsal.fiadopay.domain.user.Role;
import edu.ucsal.fiadopay.domain.user.User;

public record UserResponse (
        String email,
        Role role


) {
    public  UserResponse (User user){
        this(user.getEmail(),user.getRole());
    }
}
