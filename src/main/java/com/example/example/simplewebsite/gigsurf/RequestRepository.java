package com.example.simplewebsite.gigsurf;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RequestRepository
 */
public interface RequestRepository extends JpaRepository<Request,Integer>{
}