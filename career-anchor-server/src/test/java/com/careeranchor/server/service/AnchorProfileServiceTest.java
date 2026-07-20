package com.careeranchor.server.service;

import com.careeranchor.server.dto.AnchorProfileResponse;
import com.careeranchor.server.entity.AnchorProfile;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.mapper.AnchorProfileMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnchorProfileServiceTest {
    private final AnchorProfileMapper mapper = mock(AnchorProfileMapper.class);
    private final AnchorProfileService service = new AnchorProfileService(mapper, new ObjectMapper());

    @Test
    void returnsParsedProfilesInTheDocumentedAnchorOrder() {
        when(mapper.selectList(null)).thenReturn(List.of(
                profile(AnchorType.LIFESTYLE), profile(AnchorType.TECHNICAL)));

        List<AnchorProfileResponse> profiles = service.findAll();

        assertThat(profiles).extracting(AnchorProfileResponse::anchor)
                .containsExactly(AnchorType.TECHNICAL, AnchorType.LIFESTYLE);
        assertThat(profiles.getFirst().traits()).singleElement()
                .satisfies(item -> assertThat(item.title()).isEqualTo("特点"));
        assertThat(profiles.getFirst().careers()).singleElement()
                .satisfies(item -> assertThat(item.desc()).isEqualTo("说明"));
    }

    @Test
    void rejectsMalformedOrIncompleteStoredProfileJson() {
        AnchorProfile malformed = profile(AnchorType.TECHNICAL);
        malformed.setTraits("not-json");
        when(mapper.selectList(null)).thenReturn(List.of(malformed));

        assertThatThrownBy(service::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TECHNICAL.traits");

        AnchorProfile incomplete = profile(AnchorType.TECHNICAL);
        incomplete.setTraits("[]");
        when(mapper.selectList(null)).thenReturn(List.of(incomplete));
        assertThatThrownBy(service::findAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("incomplete");
    }

    private AnchorProfile profile(AnchorType anchor) {
        AnchorProfile profile = new AnchorProfile();
        profile.setAnchorCode(anchor);
        profile.setNameCn(anchor.nameCn());
        profile.setNameEn(anchor.name());
        profile.setTagline("一句话定位");
        String items = "[{\"title\":\"特点\",\"desc\":\"说明\"}]";
        profile.setTraits(items);
        profile.setStrengths(items);
        profile.setRisks(items);
        profile.setAdvices(items);
        profile.setCareers(items);
        return profile;
    }
}
