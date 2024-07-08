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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1,scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy{

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScoring(List<String> choices, App app) throws Exception {
        // 1.根据 id 查询题目和题目结果信息
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
        );
        // 2.统计用户每个选择对应的属性个数
        // 初始化一个Map，用于存储每个选项的计数
        Map<String, Integer> optionCount = new HashMap<>();

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
                        // 获取选项中 result 属性
                        String result = option.getResult();
                        // 如果 result 属性不在 optionCount中，初始化为0
                        if (!optionCount.containsKey(result)) {
                            optionCount.put(result, 0);
                        }
                        // 将 result 属性的计数加1
                        optionCount.put(result, optionCount.get(result) + 1);
                    }
                }
            }
        }
        // 3.遍历每种评分结果，计算哪个结果的得分更高
        int maxScore = 0;
        ScoringResult maxScoringResult = scoringResultList.get(0);

        // 遍历评分结果，计算哪个结果的得分更高
        for (ScoringResult scoringResult : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            // 计算当前评分结果的分数
            int score = resultProp.stream()
                    .mapToInt(prop -> optionCount.getOrDefault(prop, 0))
                    .sum();

            // 如果当前评分结果的分数更高，更新最大分数和对应的评分结果
            if (score > maxScore) {
                maxScore = score;
                maxScoringResult = scoringResult;
            }
        }

        // 4.构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        return userAnswer;
    }
}
