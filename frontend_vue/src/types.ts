export type Role = 'student' | 'guardian' | 'teacher';

export type StudentView =
  | 'login'
  | 'consent'
  | 'screeningIntro'
  | 'question'
  | 'result'
  | 'training'
  | 'home'
  | 'ai'
  | 'digitalHuman';

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

export interface StudentSession {
  sessionToken: string;
  role: string;
  consentRequired: boolean;
  nextStep: string;
  student: {
    studentId: string;
    studentName: string;
    className: string;
  };
  school: {
    schoolName: string;
    schoolCode: string;
  };
}

export interface ConsentStatus {
  studentId: string;
  version: string;
  guardianConsent: boolean;
  studentAssent: boolean;
  cameraTrainingConsent: boolean;
  avatarConsent: boolean;
  allRequiredCompleted: boolean;
  nextStep: string;
}

export interface ScreeningTemplate {
  title: string;
  questionCount: number;
  estimatedDuration: string;
  questions: ScreeningQuestion[];
}

export interface ScreeningQuestion {
  id: string;
  title: string;
  options: string[];
}

export interface ScreeningResult {
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  trend: string;
  disclaimer: string;
  trainingSuggestions: string[];
  safetyNotice: string;
}

export interface TodayTraining {
  studentId: string;
  encouragement: string;
  tasks: TrainingTask[];
}

export interface TrainingTask {
  id: string;
  title: string;
  duration: string;
  status: 'pending' | 'completed' | string;
}

export interface GuardianSummary {
  studentId: string;
  studentName: string;
  className: string;
  weekLabel: string;
  emotionLabel: string;
  trainingCompletionRate: number;
  reminderCount: number;
  trendBars: number[];
  reminders: string[];
  latestObservation?: GuardianLatestObservation | null;
  privacyNotice: string;
}

export interface GuardianLatestObservation {
  sceneLabel: string;
  sourceLabel: string;
  indicatorSummary: string;
  observedAt: string;
  focusTags: string[];
}

export interface TeacherAlerts {
  alerts: AlertItem[];
  highlightedDetail: AlertDetail;
}

export interface AlertItem {
  id: string;
  studentName: string;
  className: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  summary: string;
  status: string;
}

export interface AlertDetail {
  id: string;
  studentName: string;
  riskLabel: string;
  reason: string;
  suggestedActions: string[];
  trainingCompletionRate?: number | null;
  latestObservation?: TeacherLatestObservation | null;
  recentObservations?: TeacherObservationSummary[];
  followUpTip?: TeacherFollowUpTip | null;
  actionTimeline?: TeacherActionTimeline[];
  privacyNotice: string;
}

export interface TeacherObservationSummary {
  sceneLabel: string;
  sourceLabel: string;
  indicatorSummary: string;
  observedAt: string;
  focusTags: string[];
}

export type TeacherLatestObservation = TeacherObservationSummary;

export interface TeacherFollowUpTip {
  level: 'urgent' | 'attention' | 'steady' | string;
  title: string;
  detail: string;
}

export interface TeacherActionTimeline {
  kind: 'created' | 'status_update' | string;
  title: string;
  detail: string;
  actorLabel: string;
  occurredAt: string;
}

export interface AlertStatusUpdate {
  status: string;
  actorId?: string;
  note?: string;
}

export interface AiCoachReply {
  sessionId: string;
  reply: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  crisisDetected: boolean;
  disclaimer: string;
  safetyNotice: string;
  suggestedActions: string[];
}

export interface WellbeingObservationRequest {
  studentId: string;
  indicatorCodes: string[];
  sourceType?: string;
  sceneCode?: string;
  captureSummary?: string;
}

export interface WellbeingObservation {
  studentId: string;
  expressionIndicators: ExpressionIndicator[];
  wellbeingIndicators: WellbeingIndicator[];
  behaviorSuggestions: Suggestion[];
  musicSuggestions: Suggestion[];
  dietSuggestions: Suggestion[];
  avatarScript: string;
  disclaimer: string;
  privacyNotice: string;
}

export interface WellbeingObservationHistory {
  studentId: string;
  records: ObservationHistoryItem[];
  privacyNotice: string;
}

export interface ObservationHistoryItem {
  sessionKey: string;
  sourceType: string;
  sourceLabel: string;
  sceneCode: string;
  sceneLabel: string;
  indicatorSummary: string;
  rawVideoSaved: boolean;
  status: string;
  observedAt: string;
  featureHighlights: ObservationFeatureSummary[];
}

export interface ObservationFeatureSummary {
  code: string;
  label: string;
  confidence: string;
  observation: string;
}

export interface ExpressionIndicator {
  code: string;
  label: string;
  observation: string;
  confidence: string;
}

export interface WellbeingIndicator {
  code: string;
  label: string;
  explanation: string;
}

export interface Suggestion {
  code: string;
  title: string;
  detail: string;
}
