package com.song.songdada.scoring;

import com.song.songdada.model.entity.App;
import com.song.songdada.model.entity.UserAnswer;

import java.util.List;

/**
 * 评分策略
 */
public interface ScoringStrategy {

    /**
     * 执行评分
     * @param choices 选项列表
     * @param app app
     * @return 返回值
     * @throws Exception 异常
     */
    UserAnswer doScoring(List<String> choices, App app) throws Exception;
}
