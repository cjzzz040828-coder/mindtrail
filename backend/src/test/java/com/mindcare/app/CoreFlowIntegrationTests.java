package com.mindcare.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindcare.app.wellbeing.TrainingObservationSessionEntity;
import com.mindcare.app.wellbeing.TrainingObservationSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mindcare_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
class CoreFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingObservationSessionRepository trainingObservationSessionRepository;

    @Test
    void studentCoreFlowPersistsAndCreatesTeacherAlertForHighRisk() throws Exception {
        String studentId = "S-IT-001";

        mockMvc.perform(post("/api/v1/auth/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "schoolCode", "DEMO",
                                "studentId", studentId,
                                "studentName", "测试同学"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.consentRequired").value(true))
                .andExpect(jsonPath("$.data.nextStep").value("CONSENT"))
                .andExpect(jsonPath("$.data.student.studentId").value(studentId));

        mockMvc.perform(post("/api/v1/consents/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", studentId,
                                "guardianConsent", true,
                                "studentAssent", true,
                                "cameraTrainingConsent", true,
                                "avatarConsent", false
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allRequiredCompleted").value(true))
                .andExpect(jsonPath("$.data.nextStep").value("SCREENING"));

        mockMvc.perform(post("/api/v1/screenings/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", studentId,
                                "sleepScore", 3,
                                "stressScore", 9,
                                "answers", List.of("很差", "几乎每天", "几乎不愿意"),
                                "note", "最近压力很大，需要老师进一步关心。"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.disclaimer").value("当前结果仅作为辅助筛查，不构成医学诊断。"));

        mockMvc.perform(get("/api/v1/trainings/today")
                        .param("studentId", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.tasks.length()").value(3));

        mockMvc.perform(post("/api/v1/trainings/tasks/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", studentId,
                                "taskId", "task-1",
                                "status", "completed"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks[0].status").value("completed"));

        mockMvc.perform(get("/api/v1/guardian/summary")
                        .param("studentId", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentName").value("测试同学"))
                .andExpect(jsonPath("$.data.emotionLabel").value("需要关注"))
                .andExpect(jsonPath("$.data.trainingCompletionRate").value(33))
                .andExpect(jsonPath("$.data.privacyNotice").value("不展示完整聊天、隐私日记和原始视频，仅提供必要摘要与照护建议。"));

        MvcResult alertsResult = mockMvc.perform(get("/api/v1/alerts/teacher/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alerts.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.highlightedDetail.privacyNotice").value("仅展示必要摘要信息，完整隐私内容默认不可见。"))
                .andReturn();

        String alertId = objectMapper.readTree(alertsResult.getResponse().getContentAsString())
                .at("/data/highlightedDetail/id")
                .asText();

        mockMvc.perform(post("/api/v1/alerts/{alertId}/status", alertId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "status", "处理中",
                                "actorId", "teacher-demo",
                                "note", "开始线下复核"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.highlightedDetail.id").value(alertId))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline.length()").value(2))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline[0].title").value("老师更新处置状态为处理中"))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline[0].detail").value("开始线下复核"));

        mockMvc.perform(post("/api/v1/ai-coach/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", studentId,
                                "message", "我不想活了，感觉撑不住。"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.crisisDetected").value(true))
                .andExpect(jsonPath("$.data.disclaimer").value("AI 陪练只提供训练陪伴与安全提醒，不构成医学诊断。"));

        mockMvc.perform(post("/api/v1/wellbeing/observations/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", studentId,
                                "indicatorCodes", List.of("GAZE_DRIFT", "LOW_ATTENTION", "LOW_ENERGY"),
                                "sourceType", "camera_preview",
                                "sceneCode", "FOCUS_RECOVERY",
                                "captureSummary", "浏览器本地预览检测到人脸取景偏移。"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expressionIndicators.length()").value(3))
                .andExpect(jsonPath("$.data.wellbeingIndicators[0].label").value("可能存在注意力疲劳"))
                .andExpect(jsonPath("$.data.disclaimer").value("状态观察仅用于训练辅助，不构成医学诊断，也不参与风险分级。表情、眼神和注意力信号可能受疲劳、光线、镜头角度等因素影响。"))
                .andExpect(jsonPath("$.data.privacyNotice").value("默认不保存原始图片、视频或完整私密内容，仅记录观察摘要和建议结果。"));

        TrainingObservationSessionEntity observationSession = trainingObservationSessionRepository.findTopByOrderByIdDesc()
                .orElseThrow();
        assertThat(observationSession.getSourceType()).isEqualTo("camera_preview");
        assertThat(observationSession.getSceneCode()).isEqualTo("FOCUS_RECOVERY");
        assertThat(observationSession.isRawVideoSaved()).isFalse();
        assertThat(observationSession.getIndicatorSummary()).contains("浏览器本地预览检测到人脸取景偏移");

        mockMvc.perform(get("/api/v1/wellbeing/observations/history")
                        .param("studentId", studentId)
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].sourceType").value("camera_preview"))
                .andExpect(jsonPath("$.data.records[0].sourceLabel").value("摄像头预览辅助采集"))
                .andExpect(jsonPath("$.data.records[0].sceneCode").value("FOCUS_RECOVERY"))
                .andExpect(jsonPath("$.data.records[0].sceneLabel").value("专注回正"))
                .andExpect(jsonPath("$.data.records[0].rawVideoSaved").value(false))
                .andExpect(jsonPath("$.data.records[0].featureHighlights.length()").value(3))
                .andExpect(jsonPath("$.data.records[0].featureHighlights[0].label").value("视线游离"))
                .andExpect(jsonPath("$.data.privacyNotice").value("默认不保存原始图片、视频或完整私密内容，仅记录观察摘要和建议结果。"));

        mockMvc.perform(get("/api/v1/guardian/summary")
                        .param("studentId", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latestObservation.sceneLabel").value("专注回正"))
                .andExpect(jsonPath("$.data.latestObservation.sourceLabel").value("摄像头预览辅助采集"))
                .andExpect(jsonPath("$.data.latestObservation.focusTags.length()").value(3))
                .andExpect(jsonPath("$.data.latestObservation.focusTags[0]").value("视线游离"));

        mockMvc.perform(get("/api/v1/alerts/teacher/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.highlightedDetail.reason").value("系统仅保存危机命中摘要，需由老师或学校心理老师人工复核。"))
                .andExpect(jsonPath("$.data.highlightedDetail.trainingCompletionRate").value(33))
                .andExpect(jsonPath("$.data.highlightedDetail.latestObservation.sceneLabel").value("专注回正"))
                .andExpect(jsonPath("$.data.highlightedDetail.latestObservation.sourceLabel").value("摄像头预览辅助采集"))
                .andExpect(jsonPath("$.data.highlightedDetail.latestObservation.focusTags.length()").value(3))
                .andExpect(jsonPath("$.data.highlightedDetail.recentObservations.length()").value(1))
                .andExpect(jsonPath("$.data.highlightedDetail.recentObservations[0].sceneLabel").value("专注回正"))
                .andExpect(jsonPath("$.data.highlightedDetail.followUpTip.level").value("attention"))
                .andExpect(jsonPath("$.data.highlightedDetail.followUpTip.title").value("今日训练完成不足一半"));

        mockMvc.perform(get("/api/v1/alerts/teacher/demo")
                        .param("highlightAlertId", "alert-demo-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.highlightedDetail.id").value("alert-demo-2"))
                .andExpect(jsonPath("$.data.highlightedDetail.studentName").value("王同学"))
                .andExpect(jsonPath("$.data.highlightedDetail.latestObservation").doesNotExist())
                .andExpect(jsonPath("$.data.highlightedDetail.followUpTip.title").value("当前记录缺少训练档案"))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline.length()").value(1))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline[0].title").value("系统生成预警摘要"));

        mockMvc.perform(post("/api/v1/alerts/{alertId}/status", "alert-demo-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "status", "已联系班主任",
                                "actorId", "teacher-demo",
                                "note", "已转交班主任跟进"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.highlightedDetail.id").value("alert-demo-2"))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline.length()").value(2))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline[0].title").value("老师更新处置状态为已联系班主任"))
                .andExpect(jsonPath("$.data.highlightedDetail.actionTimeline[1].title").value("系统生成预警摘要"))
                .andExpect(jsonPath("$.data.alerts[?(@.id=='alert-demo-2')].status").value(contains("已联系班主任")));
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
