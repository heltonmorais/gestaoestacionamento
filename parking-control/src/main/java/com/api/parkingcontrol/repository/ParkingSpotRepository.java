//Interface onde o Spring Data utiliza os métodos já prontos para transações com o banco de dados.

package com.api.parkingcontrol.repository;


import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.parkingcontrol.model.ParkingSpotModel;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpotModel, UUID>{

	boolean existsByLicensePlateCar(String licensePlateCar);
	boolean existsByParkingSpotNumber(String parkingSpotNumber);
	boolean existsByApartmentAndBlock(String apartment, String block);
	
}
