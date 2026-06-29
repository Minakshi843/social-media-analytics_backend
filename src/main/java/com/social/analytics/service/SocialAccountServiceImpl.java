package com.social.analytics.service;

import com.social.analytics.dto.SocialAccountCreateRequest;
import com.social.analytics.dto.SocialAccountDto;
import com.social.analytics.dto.SocialAccountUpdateRequest;
import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.mapper.SocialAccountMapper;
import com.social.analytics.model.City;
import com.social.analytics.model.ConnectionStatus;
import com.social.analytics.model.SocialAccount;
import com.social.analytics.repository.CityRepository;
import com.social.analytics.repository.SocialAccountRepository;
import com.social.analytics.repository.OAuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@SuppressWarnings("null")
public class SocialAccountServiceImpl implements SocialAccountService {

    private final SocialAccountRepository socialAccountRepository;
    private final CityRepository cityRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final SocialAccountMapper socialAccountMapper;

    @Autowired
    public SocialAccountServiceImpl(SocialAccountRepository socialAccountRepository,
                                     CityRepository cityRepository,
                                     OAuthTokenRepository oAuthTokenRepository,
                                     SocialAccountMapper socialAccountMapper) {
        this.socialAccountRepository = socialAccountRepository;
        this.cityRepository = cityRepository;
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.socialAccountMapper = socialAccountMapper;
    }

    @Override
    public SocialAccountDto create(SocialAccountCreateRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + request.getCityId()));

        SocialAccount account = new SocialAccount();
        account.setCity(city);
        account.setPlatform(request.getPlatform().toUpperCase());
        account.setAccountName(request.getAccountName());
        account.setAccountHandle(request.getAccountHandle());
        account.setAccountUrl(request.getAccountUrl());
        account.setConnectionStatus(ConnectionStatus.NOT_CONNECTED);

        account = socialAccountRepository.save(account);
        return socialAccountMapper.toDto(account);
    }

    @Override
    public SocialAccountDto update(Long id, SocialAccountUpdateRequest request) {
        SocialAccount account = socialAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found with id: " + id));

        account.setAccountName(request.getAccountName());
        account.setAccountHandle(request.getAccountHandle());
        account.setAccountUrl(request.getAccountUrl());

        account = socialAccountRepository.save(account);
        return socialAccountMapper.toDto(account);
    }

    @Override
    public void delete(Long id) {
        SocialAccount account = socialAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found with id: " + id));
        
        // Delete related OAuthToken first to prevent foreign key constraint violation
        oAuthTokenRepository.findBySocialAccountId(id)
                .ifPresent(oAuthTokenRepository::delete);

        socialAccountRepository.delete(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialAccountDto> getAll() {
        List<SocialAccount> accounts = socialAccountRepository.findAll();
        return socialAccountMapper.toDtoList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public SocialAccountDto getById(Long id) {
        SocialAccount account = socialAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found with id: " + id));
        return socialAccountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialAccountDto> getByCityId(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City not found with id: " + cityId);
        }
        List<SocialAccount> accounts = socialAccountRepository.findByCityId(cityId);
        return socialAccountMapper.toDtoList(accounts);
    }
}
