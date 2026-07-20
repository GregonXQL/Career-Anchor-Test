package com.careeranchor.server.dto;

import com.careeranchor.server.enums.AnchorType;

import java.util.List;

public record AnchorProfileResponse(
        AnchorType anchor,
        String nameCn,
        String nameEn,
        String tagline,
        List<ProfileItem> traits,
        List<ProfileItem> strengths,
        List<ProfileItem> risks,
        List<ProfileItem> advices,
        List<ProfileItem> careers) {
}
