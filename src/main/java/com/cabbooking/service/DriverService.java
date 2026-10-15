
package com.cabbooking.service;

import com.cabbooking.dto.DriverRegistrationRequest;
import com.cabbooking.entity.Driver;
import com.cabbooking.entity.User;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public DriverService(
            DriverRepository driverRepository,
            UserRepository userRepository) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Driver registerDriver(
            DriverRegistrationRequest request,
            String authenticatedEmail) {

        String email = authenticatedEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (user.getRole() != User.Role.DRIVER) {
            throw new IllegalArgumentException(
                    "Your account is not registered as a driver");
        }

        if (driverRepository.existsByUser_Id(user.getId())) {
            throw new IllegalArgumentException(
                    "Driver profile already exists");
        }

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setLicenseNumber(request.getLicenseNumber().trim());
        driver.setVehicleNumber(
                request.getVehicleNumber().trim().toUpperCase(Locale.ROOT));
        driver.setVehicleModel(request.getVehicleModel().trim());

        return driverRepository.save(driver);
    }
}
