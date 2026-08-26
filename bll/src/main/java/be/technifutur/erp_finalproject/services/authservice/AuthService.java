package be.technifutur.erp_finalproject.services.authservice;

import be.technifutur.erp_finalproject.entities.User;

public interface AuthService {

    User login(String email, String password);
}
