package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket, boolean isRecurring) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}
		long inTime = ticket.getInTime().getTime();
		long outTime = ticket.getOutTime().getTime();

		double duration = outTime - inTime; // durée en milisecondes
		duration = duration / 1000;// durée en seconde
		duration = duration / 60; // durée en minute
		duration = duration / 60; // durée en heure
		
		
		if (duration > 0.5) {

			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
				break;
			}
			case BIKE: {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		} else
			ticket.setPrice(0);
		
		if (isRecurring == true) {
			ticket.setPrice(ticket.getPrice() - (ticket.getPrice() * 5.0 / 100.0));
		}
	}
}