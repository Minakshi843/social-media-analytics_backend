package com.social.analytics.service;

import com.social.analytics.dto.CityRequest;
import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.model.City;
import com.social.analytics.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class CityService {

    private final CityRepository cityRepository;

    @Autowired
    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public City getCityById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
    }

    @Transactional
    public City createCity(CityRequest request) {
        if (cityRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("City with name '" + request.getName() + "' already exists");
        }
        City city = new City(request.getName());
        return cityRepository.save(city);
    }

    @Transactional
    public City updateCity(Long id, CityRequest request) {
        City city = getCityById(id);
        cityRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("City with name '" + request.getName() + "' already exists");
            }
        });
        city.setName(request.getName());
        return cityRepository.save(city);
    }

    @Transactional
    public void deleteCity(Long id) {
        City city = getCityById(id);
        cityRepository.delete(city);
    }
}
