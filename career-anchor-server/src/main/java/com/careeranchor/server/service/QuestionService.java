package com.careeranchor.server.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.careeranchor.server.config.AppProperties;
import com.careeranchor.server.dto.QuestionsResponse;
import com.careeranchor.server.entity.Question;
import com.careeranchor.server.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper;
    private final AppProperties.Assessment assessment;

    public QuestionService(QuestionMapper questionMapper, AppProperties properties) {
        this.questionMapper = questionMapper;
        this.assessment = properties.assessment();
    }

    public QuestionsResponse getEnabledQuestions() {
        List<QuestionsResponse.QuestionView> questions = questionMapper.selectList(
                        Wrappers.<Question>lambdaQuery().eq(Question::getEnabled, true).orderByAsc(Question::getId))
                .stream()
                .map(question -> new QuestionsResponse.QuestionView(question.getId(), question.getContent()))
                .toList();
        QuestionsResponse.AssessmentConfig config = new QuestionsResponse.AssessmentConfig(
                assessment.scaleMax(), List.copyOf(assessment.scaleLabels()),
                assessment.boostCount(), assessment.boostValue());
        return new QuestionsResponse(questions, config);
    }
}
