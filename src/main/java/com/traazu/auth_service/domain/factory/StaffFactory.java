package com.traazu.auth_service.domain.factory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.traazu.auth_service.domain.dtos.auth.sign_up.impl.StaffSignUpInfos;
import com.traazu.auth_service.domain.entities.Staff;

@Component
public class StaffFactory extends AbstractBaseUserFactory<Staff, StaffSignUpInfos> {

    public StaffFactory(PasswordEncoder passwordEncoder) {
        super(passwordEncoder);
    }

    @Override 
    protected Staff instantiate() {
        return new Staff();
    }

    @Override 
    protected void customMapping(Staff staff, StaffSignUpInfos request) {
        staff.setCreatedBy(request.getCreatedBy());
        staff.setCreatedByName(request.getCreatedByName());
        staff.setCreatedByRole(request.getCreatedByRole());
    }
    
}
