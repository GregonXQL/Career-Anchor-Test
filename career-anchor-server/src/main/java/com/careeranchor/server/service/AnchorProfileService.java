package com.careeranchor.server.service;

import com.careeranchor.server.dto.AnchorProfileResponse;
import com.careeranchor.server.dto.ProfileItem;
import com.careeranchor.server.entity.AnchorProfile;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.mapper.AnchorProfileMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AnchorProfileService {
    private static final TypeReference<List<ProfileItem>> ITEMS = new TypeReference<>() {};

    private final AnchorProfileMapper anchorProfileMapper;
    private final ObjectMapper objectMapper;

    public AnchorProfileService(AnchorProfileMapper anchorProfileMapper, ObjectMapper objectMapper) {
        this.anchorProfileMapper = anchorProfileMapper;
        this.objectMapper = objectMapper;
    }

    public List<AnchorProfileResponse> findAll() {
        return anchorProfileMapper.selectList(null).stream()
                .sorted(Comparator.comparingInt(profile -> profile.getAnchorCode().priority()))
                .map(this::toResponse)
                .toList();
    }

    private AnchorProfileResponse toResponse(AnchorProfile profile) {
        AnchorType anchor = profile.getAnchorCode();
        return new AnchorProfileResponse(anchor, profile.getNameCn(), profile.getNameEn(),
                profile.getTagline(), parse(profile.getTraits(), anchor, "traits"),
                parse(profile.getStrengths(), anchor, "strengths"),
                parse(profile.getRisks(), anchor, "risks"),
                parse(profile.getAdvices(), anchor, "advices"),
                parse(profile.getCareers(), anchor, "careers"));
    }

    private List<ProfileItem> parse(String json, AnchorType anchor, String field) {
        try {
            List<ProfileItem> items = objectMapper.readValue(json, ITEMS);
            if (items.isEmpty() || items.stream().anyMatch(item -> item.title() == null || item.desc() == null)) {
                throw new IllegalStateException("Anchor profile JSON is incomplete: " + anchor + "." + field);
            }
            return List.copyOf(items);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Anchor profile JSON is invalid: " + anchor + "." + field, exception);
        }
    }
}
