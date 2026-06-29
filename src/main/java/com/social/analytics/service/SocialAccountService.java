package com.social.analytics.service;

import com.social.analytics.dto.SocialAccountCreateRequest;
import com.social.analytics.dto.SocialAccountDto;
import com.social.analytics.dto.SocialAccountUpdateRequest;

import java.util.List;

public interface SocialAccountService {
    SocialAccountDto create(SocialAccountCreateRequest request);
    SocialAccountDto update(Long id, SocialAccountUpdateRequest request);
    void delete(Long id);
    List<SocialAccountDto> getAll();
    SocialAccountDto getById(Long id);
    List<SocialAccountDto> getByCityId(Long cityId);
}
