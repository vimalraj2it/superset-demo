package com.branddashboard.repository;

import com.branddashboard.model.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BrandRepository extends MongoRepository<Brand, String> {
    List<Brand> findByIdIn(List<String> brandIds);
}
