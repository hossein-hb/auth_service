package com.traazu.auth_service.domain.factory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.traazu.auth_service.domain.dtos.auth.sign_up.impl.UserSignUpInfos;
import com.traazu.auth_service.domain.entities.User;

@Component
public class UserFactory extends AbstractBaseUserFactory<User, UserSignUpInfos> {

    public UserFactory(PasswordEncoder passwordEncoder) {
        super(passwordEncoder);
    }

    @Override 
    protected User instantiate() {
        return new User();
    }

    @Override 
    protected void customMapping(User user, UserSignUpInfos request) {
        return;
    }
    
}
