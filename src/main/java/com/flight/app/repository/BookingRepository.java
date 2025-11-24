package com.flight.app.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flight.app.entity.Booking;

import reactor.core.publisher.Mono; 

@Repository
public interface BookingRepository extends ReactiveMongoRepository<Booking, String> { 
    
    Mono<Booking> findByPnr(String pnr);
    
}
