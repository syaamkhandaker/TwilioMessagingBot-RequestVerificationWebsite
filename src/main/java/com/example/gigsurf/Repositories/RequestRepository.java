package com.example.simplewebsite.gigsurf.Repositories;

import com.example.simplewebsite.gigsurf.Entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RequestRepository
 */
public interface RequestRepository extends JpaRepository<Request,Integer>{
}