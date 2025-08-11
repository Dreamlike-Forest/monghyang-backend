package com.example.monghyang.domain.brewery.main.repository;

import com.example.monghyang.domain.brewery.main.entity.Brewery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreweryRepository extends JpaRepository<Brewery, Long> {
}
