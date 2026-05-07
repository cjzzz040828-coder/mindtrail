import type {
  AlertStatusUpdate,
  AiCoachReply,
  ApiResponse,
  ConsentStatus,
  GuardianSummary,
  WellbeingObservationHistory,
  ScreeningResult,
  ScreeningTemplate,
  StudentSession,
  TeacherAlerts,
  TodayTraining,
  WellbeingObservation,
  WellbeingObservationRequest
} from '../types';

export class ApiClient {
  constructor(private readonly baseUrl: string) {}

  getBaseUrl() {
    return this.baseUrl;
  }

  loginStudent(payload: { schoolCode: string; studentId: string; studentName: string }) {
    return this.post<StudentSession>('/api/v1/auth/student/login', payload);
  }

  getConsentStatus(studentId: string) {
    return this.get<ConsentStatus>(`/api/v1/consents/status?studentId=${encodeURIComponent(studentId)}`);
  }

  submitConsent(payload: {
    studentId: string;
    guardianConsent: boolean;
    studentAssent: boolean;
    cameraTrainingConsent: boolean;
    avatarConsent: boolean;
  }) {
    return this.post<ConsentStatus>('/api/v1/consents/submit', payload);
  }

  getScreeningTemplate() {
    return this.get<ScreeningTemplate>('/api/v1/screenings/template');
  }

  submitScreening(payload: {
    studentId: string;
    sleepScore: number;
    stressScore: number;
    answers: string[];
    note: string;
  }) {
    return this.post<ScreeningResult>('/api/v1/screenings/submit', payload);
  }

  getTodayTraining(studentId: string) {
    return this.get<TodayTraining>(`/api/v1/trainings/today?studentId=${encodeURIComponent(studentId)}`);
  }

  updateTrainingTaskStatus(payload: { studentId: string; taskId: string; status: string }) {
    return this.post<TodayTraining>('/api/v1/trainings/tasks/status', payload);
  }

  getGuardianSummary(studentId: string) {
    return this.get<GuardianSummary>(`/api/v1/guardian/summary?studentId=${encodeURIComponent(studentId)}`);
  }

  getTeacherAlerts(highlightAlertId?: string) {
    const query = highlightAlertId ? `?highlightAlertId=${encodeURIComponent(highlightAlertId)}` : '';
    return this.get<TeacherAlerts>(`/api/v1/alerts/teacher/demo${query}`);
  }

  updateAlertStatus(alertId: string, payload: AlertStatusUpdate) {
    return this.post<TeacherAlerts>(`/api/v1/alerts/${encodeURIComponent(alertId)}/status`, payload);
  }

  sendAiCoachMessage(payload: { studentId: string; sessionId?: string; message: string }) {
    return this.post<AiCoachReply>('/api/v1/ai-coach/messages', payload);
  }

  analyzeWellbeingObservation(payload: WellbeingObservationRequest) {
    return this.post<WellbeingObservation>('/api/v1/wellbeing/observations/analyze', payload);
  }

  getWellbeingObservationHistory(studentId: string, limit = 6) {
    return this.get<WellbeingObservationHistory>(
      `/api/v1/wellbeing/observations/history?studentId=${encodeURIComponent(studentId)}&limit=${limit}`
    );
  }

  private async get<T>(path: string) {
    const response = await fetch(this.url(path));
    return this.unwrap<T>(response);
  }

  private async post<T>(path: string, body: unknown) {
    const response = await fetch(this.url(path), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    });
    return this.unwrap<T>(response);
  }

  private url(path: string) {
    return `${this.baseUrl.replace(/\/$/, '')}${path}`;
  }

  private async unwrap<T>(response: Response) {
    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`);
    }

    const payload = (await response.json()) as ApiResponse<T>;
    if (!payload.success) {
      throw new Error(payload.message || '接口返回失败');
    }

    return payload.data;
  }
}
