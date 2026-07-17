package com.careeranchor.server.service;

import com.careeranchor.server.dto.Answer;
import com.careeranchor.server.dto.ScoreResult;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.exception.BizException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoringServiceTest {
    private final ScoringService service = new ScoringService();

    @Test
    void scoresMaximumAnswersAndCapsDisplayPercentAtOneHundred() {
        ScoreResult result = service.score(answersWithValue(6), List.of(1, 9, 17), 6);

        assertThat(result.top3()).containsExactly(
                AnchorType.TECHNICAL, AnchorType.MANAGERIAL, AnchorType.AUTONOMY);
        assertThat(result.scores().getFirst())
                .extracting(ScoreResult.AnchorScore::raw, ScoreResult.AnchorScore::avg,
                        ScoreResult.AnchorScore::percent)
                .containsExactly(42, 8.4, 100);
        assertThat(result.scores()).allMatch(score -> score.percent() == 100);
    }

    @Test
    void scoresMinimumAnswersWithRequiredBoosts() {
        ScoreResult result = service.score(answersWithValue(1), List.of(1, 2, 3), 6);

        assertThat(result.top3()).containsExactly(
                AnchorType.TECHNICAL, AnchorType.MANAGERIAL, AnchorType.AUTONOMY);
        assertThat(result.scores()).extracting(ScoreResult.AnchorScore::raw)
                .containsExactly(9, 9, 9, 5, 5, 5, 5, 5);
        assertThat(result.scores()).extracting(ScoreResult.AnchorScore::percent)
                .containsExactly(30, 30, 30, 17, 17, 17, 17, 17);
    }

    @Test
    void resolvesEqualRawScoresUsingDocumentedAnchorPriority() {
        ScoreResult result = service.score(answersWithValue(3), List.of(8, 16, 24), 6);

        assertThat(result.top3()).containsExactly(
                AnchorType.LIFESTYLE, AnchorType.TECHNICAL, AnchorType.MANAGERIAL);
        assertThat(result.scores().getFirst().raw()).isEqualTo(27);
        assertThat(result.scores().subList(1, 8)).extracting(ScoreResult.AnchorScore::anchor)
                .containsExactly(AnchorType.TECHNICAL, AnchorType.MANAGERIAL, AnchorType.AUTONOMY,
                        AnchorType.SECURITY, AnchorType.CREATIVITY, AnchorType.SERVICE, AnchorType.CHALLENGE);
    }

    @Test
    void rejectsIllegalInput() {
        List<Answer> duplicateQuestion = new ArrayList<>(answersWithValue(3));
        duplicateQuestion.set(39, new Answer(1, 3));

        assertThatThrownBy(() -> service.score(duplicateQuestion, List.of(1, 2, 3), 6))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("重复");
        assertThatThrownBy(() -> service.score(answersWithValue(7), List.of(1, 2, 3), 6))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("分值");
        assertThatThrownBy(() -> service.score(answersWithValue(3), List.of(1, 1, 2), 6))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不重复");
    }

    private List<Answer> answersWithValue(int value) {
        return IntStream.rangeClosed(1, 40).mapToObj(id -> new Answer(id, value)).toList();
    }
}
