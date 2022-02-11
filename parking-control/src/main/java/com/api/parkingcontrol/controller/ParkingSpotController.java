package com.api.parkingcontrol.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

//import java.util.List;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.hateoas.Links;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.model.ParkingSpotModel;
import com.api.parkingcontrol.repository.ParkingSpotRepository;
import com.api.parkingcontrol.services.ParkingSpotService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
	
	//Método para informar que a qualquer será necessário instanciar a classe ParkingSpotService
	//Anotation:@Autowired para implementar o instanciamento  
	@Autowired
	ParkingSpotService parkingSpotService;
	
	@Autowired
	ParkingSpotRepository parkingSpotRepository;
	
	//Método para gravar uma nova vaga(recurso)
	@PostMapping	
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
		ParkingSpotModel parkingSpotModel = new ParkingSpotModel();
		
		if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
		}
		
		if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
		}
		
		if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot already registered for Apartment/Block");
		}
		
		/*Faz a copia dos paramentros da classe Dto para classe model*/
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
		
		/*Faz a copia dos paramentros da classe Dto para classe model*/
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		
		/*Retorna o registro gravado no caso a classe parkingSpotModel*/
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
	}

	//Método para listar todas as vagas(recursos) com HATEOS		
	@GetMapping 
	public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots(){ 
		List<ParkingSpotModel> parkingSpotModelList = parkingSpotRepository.findAll(); 
		if(parkingSpotModelList.isEmpty()){ 
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);			
		}else { 
			for (ParkingSpotModel parkingSpotModel : parkingSpotModelList ) {
				UUID id = parkingSpotModel.getId();
				parkingSpotModel.add(linkTo(methodOn(ParkingSpotController.class).getOneParkingSpots(id)).withSelfRel()); 
			}	 
			return new ResponseEntity<List<ParkingSpotModel>>(parkingSpotModelList, HttpStatus.OK); 
		} 
	}
	 

	//Método para listar todas as vagas(recursos) com paginação
	/*
	@GetMapping 
	public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
	 return	ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable)); 
	}
	*/
	
	
	//Método para listar vagas por id(recurso) com HATEOS
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOneParkingSpots(@PathVariable(value = "id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot Not Found.");
		} else {		
		parkingSpotModelOptional.get().add(linkTo(methodOn(ParkingSpotController.class).getAllParkingSpots()).withRel("List Parking Spots"));
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
		}
	}

	//Metodo para deletar uma vaga(recurso)
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteParkingSpotModel(@PathVariable(value = "id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot Not Found.");
		}
		parkingSpotService.delete(parkingSpotModelOptional.get());
		return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully.");
	}
	
	//Método para atualizar uma vaga
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateParkingSpotModel(@PathVariable(value = "id") UUID id,
														 @RequestBody @Valid ParkingSpotDto parkingSpotDto){
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot Not Found.");
		}
		
		
		//Opção 1 de utilizar o método PUT (Update)
		ParkingSpotModel parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
		parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
		parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
		
		//Opção 2 de utilizar o método PUT (Update)
		/*
		 * ParkingSpotModel parkingSpotModel = parkingSpotModelOptional.get();
		 * parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
		 * parkingSpotModel.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
		 * parkingSpotModel.setBrandCar(parkingSpotDto.getBrandCar());
		 * parkingSpotModel.setModelCar(parkingSpotDto.getModelCar());
		 * parkingSpotModel.setColorCar(parkingSpotDto.getColorCar());
		 * parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());
		 * parkingSpotModel.setApartment(parkingSpotDto.getApartment());
		 * parkingSpotModel.setBlock(parkingSpotDto.getBlock());
		 */
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
																		
	}
	
}
