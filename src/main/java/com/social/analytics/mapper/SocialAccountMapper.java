package com.social.analytics.mapper;

import com.social.analytics.dto.SocialAccountDto;
import com.social.analytics.model.SocialAccount;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SocialAccountMapper {

    public SocialAccountDto toDto(SocialAccount account) {
        if (account == null) {
            return null;
        }

        SocialAccountDto dto = new SocialAccountDto();
        dto.setId(account.getId());
        dto.setAccountName(account.getAccountName());
        dto.setAccountHandle(account.getAccountHandle());
        dto.setAccountUrl(account.getAccountUrl());
        dto.setPlatform(account.getPlatform());
        dto.setConnectionStatus(account.getConnectionStatus());
        dto.setTokenExpiry(account.getTokenExpiry());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());

        if (account.getCity() != null) {
            dto.setCityId(account.getCity().getId());
            dto.setCityName(account.getCity().getName());
        }

        return dto;
    }

    public List<SocialAccountDto> toDtoList(List<SocialAccount> accounts) {
        if (accounts == null) {
            return null;
        }
        return accounts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
