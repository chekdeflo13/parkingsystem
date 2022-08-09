package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;
	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() throws Exception {
		// Given
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();// tickets + parking sauvegardÃ©

		Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
		assertNotNull(ticket);// place de parking + ticket associÃ©s
		assertNotNull(ticket.getParkingSpot());
		// TODO: check that a ticket is actually saved in DB and Parking table is
		// updated with availability

	}

	@Test
	public void testParkingLotExit() throws Exception {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();

		Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
		assertNotNull(ticket.getPrice());
		assertNotNull(ticket.getOutTime());

		// TODO: check that the fare generated and out time are populated correctly in
		// the database
	}

	@Test
	public void testParkingLotExitReduction() throws Exception {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();
		parkingService.processIncomingVehicle();
		Date outTime = new Date();
		outTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
		Ticket ticket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());//recupère le ticket mais les 1h ne sont pas appliqués
		ticket.setOutTime(outTime);// simuler 1h d'attente 
		fareCalculatorService.calculateFare(ticket,true);
		double value = ticket.getPrice() * Math.pow(10, 3);//10 puissance 3
        value = Math.floor(value);//on prend la partie entiere
        value = value / Math.pow(10, 3); //on divise par puissance 3
		assertEquals(value, (Fare.CAR_RATE_PER_HOUR - (Fare.CAR_RATE_PER_HOUR  * 5.0 / 100.0)));
	}

}
