package com.social.analytics.service;

import com.social.analytics.dto.TargetRequest;
import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.model.City;
import com.social.analytics.model.Target;
import com.social.analytics.repository.CityRepository;
import com.social.analytics.repository.TargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class TargetService {

    private final TargetRepository targetRepository;
    private final CityRepository cityRepository;

    @Autowired
    public TargetService(TargetRepository targetRepository, CityRepository cityRepository) {
        this.targetRepository = targetRepository;
        this.cityRepository = cityRepository;
    }

    public List<Target> getTargetsByCity(Long cityId) {
        return targetRepository.findByCityId(cityId);
    }

    public Target getTargetByCityAndPlatform(Long cityId, String platform) {
        return targetRepository.findByCityIdAndPlatform(cityId, platform.toUpperCase())
                .orElse(null);
    }

    @Transactional
    public Target saveOrUpdateTarget(Long cityId, TargetRequest request) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));

        String platform = request.getPlatform().toUpperCase();
        Target target = targetRepository.findByCityIdAndPlatform(cityId, platform)
                .orElse(new Target(city, platform, 0, 0, 0, 0));

        target.setDailyStaticTarget(request.getDailyStaticTarget());
        target.setDailyCarouselTarget(request.getDailyCarouselTarget());
        target.setDailyReelTarget(request.getDailyReelTarget());
        target.setDailyPostTarget(request.getDailyPostTarget());

        return targetRepository.save(target);
    }

    public double calculateAchievementRate(double actual, double target) {
        if (target <= 0) {
            return actual > 0 ? 100.0 : 0.0;
        }
        return Math.min(100.0, (actual / target) * 100.0);
    }

    public int calculatePending(int actual, int target) {
        int pending = target - actual;
        return Math.max(0, pending);
    }
}
