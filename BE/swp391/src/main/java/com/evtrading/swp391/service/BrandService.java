package com.evtrading.swp391.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evtrading.swp391.dto.BrandDTO;
import com.evtrading.swp391.entity.Brand;
import com.evtrading.swp391.repository.BrandRepository;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public BrandDTO createBrand(BrandDTO dto) {
        Brand brand = new Brand();
        brand.setBrandName(dto.getBrandName());
        Brand savedBrand = brandRepository.save(brand);

        BrandDTO response = new BrandDTO();
        response.setBrandId(savedBrand.getBrandID());
        response.setBrandName(savedBrand.getBrandName());
        return response;
    }
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }
}