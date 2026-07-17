package com.careeranchor.server.service;

import com.careeranchor.server.dto.Answer;
import com.careeranchor.server.dto.ScoreResult;
import com.careeranchor.server.enums.AnchorType;
import com.careeranchor.server.enums.ErrorCode;
import com.careeranchor.server.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ScoringService {
    public static final int QUESTION_COUNT = 40;
    public static final int QUESTIONS_PER_ANCHOR = 5;
    public static final int BOOST_COUNT = 3;
    public static final int BOOST_VALUE = 4;

    /** Pure scoring function for the finalized 40-question assessment. */
    public ScoreResult score(List<Answer> answers, List<Integer> boosted, int scaleMax) {
        validate(answers, boosted, scaleMax);

        Set<Integer> boostedSet = Set.copyOf(boosted);
        Map<AnchorType, Integer> rawScores = new EnumMap<>(AnchorType.class);
        for (AnchorType anchor : AnchorType.values()) {
            rawScores.put(anchor, 0);
        }
        for (Answer answer : answers) {
            AnchorType anchor = AnchorType.forQuestion(answer.q());
            int value = answer.v() + (boostedSet.contains(answer.q()) ? BOOST_VALUE : 0);
            rawScores.merge(anchor, value, Integer::sum);
        }

        List<ScoreResult.AnchorScore> scores = new ArrayList<>();
        for (AnchorType anchor : AnchorType.values()) {
            int raw = rawScores.get(anchor);
            double average = raw / (double) QUESTIONS_PER_ANCHOR;
            int percent = (int) Math.min(100L,
                    Math.round(raw / (double) (QUESTIONS_PER_ANCHOR * scaleMax) * 100));
            scores.add(new ScoreResult.AnchorScore(anchor, anchor.nameCn(), raw, average, percent));
        }
        scores.sort(Comparator.comparingInt(ScoreResult.AnchorScore::raw).reversed()
                .thenComparingInt(score -> score.anchor().priority()));
        List<AnchorType> top3 = scores.stream().limit(3).map(ScoreResult.AnchorScore::anchor).toList();
        return new ScoreResult(List.copyOf(scores), top3);
    }

    private void validate(List<Answer> answers, List<Integer> boosted, int scaleMax) {
        if (scaleMax < 1) {
            invalid("scaleMax 必须大于 0");
        }
        if (answers == null || answers.size() != QUESTION_COUNT) {
            invalid("必须提交 40 条答案");
        }
        Set<Integer> questionIds = new HashSet<>();
        for (Answer answer : answers) {
            if (answer == null || answer.q() < 1 || answer.q() > QUESTION_COUNT) {
                invalid("题号必须为 1–40");
            }
            if (!questionIds.add(answer.q())) {
                invalid("题号不能重复");
            }
            if (answer.v() < 1 || answer.v() > scaleMax) {
                invalid("答案分值必须为 1–" + scaleMax);
            }
        }
        if (boosted == null || boosted.size() != BOOST_COUNT) {
            invalid("必须选择恰好 3 道附加题");
        }
        Set<Integer> uniqueBoosted = new HashSet<>(boosted);
        if (uniqueBoosted.size() != BOOST_COUNT || !questionIds.containsAll(uniqueBoosted)) {
            invalid("附加题必须是 3 个不重复的合法题号");
        }
    }

    private void invalid(String detail) {
        throw new BizException(ErrorCode.INVALID_ANSWER, detail);
    }
}
