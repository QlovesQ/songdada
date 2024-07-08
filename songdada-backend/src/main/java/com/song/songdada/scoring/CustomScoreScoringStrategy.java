package com.song.songdada.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.song.songdada.model.dto.question.QuestionContentDTO;
import com.song.songdada.model.entity.App;
import com.song.songdada.model.entity.Question;
import com.song.songdada.model.entity.ScoringResult;
import com.song.songdada.model.entity.UserAnswer;
import com.song.songdada.model.vo.QuestionVO;
import com.song.songdada.service.QuestionService;
import com.song.songdada.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 自定义测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 0,scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy{

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;
    @Override
    public UserAnswer doScoring(List<String> choices, App app) throws Exception {
        // 1.根据 id 查询题目和题目结果信息（按分数降序排序）
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );
        // 2.统计用户的总积分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        // 遍历每道题目，统计用户每个选择对应的属性个数
        for(QuestionContentDTO questionContentDTO : questionContent){
            //遍历答案列表
            for (String answer : choices){
                // 遍历题目中的选项
                for (QuestionContentDTO.Option option : questionContentDTO.getOptions()){
                    // 如果答案和选项的key匹配
                    if(option.getKey().equals(answer)){
                        int score = Optional.of(option.getScore()).orElse(0);
                        totalScore += score;
                    }
                }
            }
        }

        // 3.遍历得分结果。找到第一个用户大于得分范围的结果，作为最终结果
        ScoringResult maxScoringResult = scoringResultList.get(0);
        for (ScoringResult scoringResult : scoringResultList){
            if (totalScore >= scoringResult.getResultScoreRange()){
                maxScoringResult = scoringResult;
                break;
            }
        }

        // 4.构造返回值
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
