<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue';
import {
  Bell,
  Bot,
  CheckCircle2,
  ClipboardCheck,
  HeartPulse,
  Home,
  Loader2,
  LockKeyhole,
  PhoneCall,
  School,
  Send,
  ShieldCheck,
  Sparkles,
  UserRound,
  Users
} from 'lucide-vue-next';

import { ApiClient } from './api/client';
import type {
  ConsentStatus,
  GuardianSummary,
  Role,
  ScreeningResult,
  ScreeningTemplate,
  StudentSession,
  StudentView,
  TeacherAlerts,
  TrainingTask,
  TodayTraining,
  WellbeingObservation,
  WellbeingObservationHistory
} from './types';

const api = new ApiClient(import.meta.env.VITE_API_BASE_URL ?? '');

type DigitalSceneId = 'release' | 'focus' | 'restore';
type CameraStatus = 'idle' | 'active' | 'blocked' | 'unsupported';
type CameraFaceState = 'unknown' | 'detected' | 'missing';

interface DigitalScene {
  id: DigitalSceneId;
  title: string;
  subtitle: string;
  duration: string;
  sceneCode: string;
  manualIndicatorCodes: string[];
  cameraBaseIndicatorCodes: string[];
  targetExpression: string;
  targetBehavior: string;
  syncTaskId: string;
  syncTaskTitle: string;
  steps: Array<{
    title: string;
    detail: string;
    cue: string;
  }>;
}

interface CameraObservationPayload {
  indicatorCodes: string[];
  captureSummary: string;
  localHint: string;
  sourceType: string;
}

interface FaceDetectionResult {
  boundingBox?: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
}

interface FaceDetectorLike {
  detect(input: ImageBitmapSource): Promise<FaceDetectionResult[]>;
}

type FaceDetectorConstructor = new (options?: {
  fastMode?: boolean;
  maxDetectedFaces?: number;
}) => FaceDetectorLike;

interface WindowWithFaceDetector extends Window {
  FaceDetector?: FaceDetectorConstructor;
}

const selectedRole = ref<Role>('student');
const studentView = ref<StudentView>('login');
const loading = ref(false);
const error = ref('');
const cameraSupported = typeof navigator !== 'undefined' && typeof navigator.mediaDevices?.getUserMedia === 'function';

const loginForm = reactive({
  schoolCode: 'SCH-001',
  studentId: 'S2024001',
  studentName: '林同学'
});

const consentForm = reactive({
  guardianConsent: false,
  studentAssent: false,
  cameraTrainingConsent: false,
  avatarConsent: false
});

const session = ref<StudentSession | null>(null);
const consentStatus = ref<ConsentStatus | null>(null);
const screeningTemplate = ref<ScreeningTemplate | null>(null);
const answers = ref<string[]>([]);
const note = ref('');
const currentQuestionIndex = ref(0);
const screeningResult = ref<ScreeningResult | null>(null);
const todayTraining = ref<TodayTraining | null>(null);
const guardianSummary = ref<GuardianSummary | null>(null);
const teacherAlerts = ref<TeacherAlerts | null>(null);
const teacherActionStatus = ref('处理中');
const teacherActionNote = ref('');
const aiInput = ref('');
const aiSessionId = ref<string | null>(null);
const wellbeingObservation = ref<WellbeingObservation | null>(null);
const observationHistory = ref<WellbeingObservationHistory | null>(null);
const activeDigitalSceneId = ref<DigitalSceneId>('focus');
const digitalStepIndex = ref(0);
const digitalSessionComplete = ref(false);
const cameraVideoRef = ref<HTMLVideoElement | null>(null);
const cameraStream = ref<MediaStream | null>(null);
const cameraStatus = ref<CameraStatus>(cameraSupported ? 'idle' : 'unsupported');
const cameraFaceState = ref<CameraFaceState>('unknown');
const cameraStatusText = ref(
  cameraSupported
    ? '当前展示为场景默认分析，可开启摄像头采集实时状态。'
    : '当前浏览器或设备不支持摄像头访问，可先使用场景默认指标。'
);
const cameraCaptureSummary = ref('');
const cameraLastAnalysisSource = ref<'manual' | 'camera'>('manual');
const aiMessages = ref([
  {
    role: 'assistant',
    content: '你好，我会陪你做一个很短的练习。你可以先告诉我：现在最明显的感受是什么？'
  }
]);

const roles = [
  { key: 'student' as const, label: '学生', icon: School },
  { key: 'guardian' as const, label: '家长', icon: Users },
  { key: 'teacher' as const, label: '老师', icon: ClipboardCheck }
];

const teacherActionStatusOptions = ['处理中', '已记录处置', '继续观察', '已联系家长', '已联系班主任'];
const teacherActionNoteTemplates = ['已联系班主任同步关注', '已联系家长说明情况', '计划明天课后单独沟通', '建议先补做一次训练打卡'];

const digitalScenes: DigitalScene[] = [
  {
    id: 'release' as const,
    title: '舒压呼吸',
    subtitle: '适合紧张、身体绷住、想先慢下来时',
    duration: '约 3 分钟',
    sceneCode: 'TENSION_RELIEF',
    manualIndicatorCodes: ['TENSION', 'LOW_ATTENTION'],
    cameraBaseIndicatorCodes: ['TENSION'],
    targetExpression: '下颌放松、肩膀下沉、呼吸变慢',
    targetBehavior: '把节奏从“顶着走”切回“稳住再走”',
    syncTaskId: 'task-1',
    syncTaskTitle: '呼吸放松',
    steps: [
      { title: '坐稳落地', detail: '双脚踩稳地面，背部轻轻靠住椅背。', cue: '数字人会提醒你先不着急表现，只要稳定身体。' },
      { title: '方块呼吸', detail: '吸气 4 秒，停 2 秒，呼气 4 秒，重复 5 轮。', cue: '视线看向屏幕上方一点，让呼吸慢下来。' },
      { title: '微笑收束', detail: '轻轻抬起嘴角，不需要夸张，只要让面部放松。', cue: '训练目标不是“表演开心”，而是让身体先退出绷紧。' }
    ]
  },
  {
    id: 'focus' as const,
    title: '专注回正',
    subtitle: '适合视线游离、注意力波动、难以进入状态时',
    duration: '约 4 分钟',
    sceneCode: 'FOCUS_RECOVERY',
    manualIndicatorCodes: ['GAZE_DRIFT', 'LOW_ATTENTION', 'LOW_ENERGY'],
    cameraBaseIndicatorCodes: ['LOW_ATTENTION'],
    targetExpression: '目光回正、眉眼稳定、头部姿态更平衡',
    targetBehavior: '把注意力从发散拉回当前任务',
    syncTaskId: 'task-3',
    syncTaskTitle: '正念专注',
    steps: [
      { title: '视线定位', detail: '选屏幕中的一个固定点，连续看 10 秒。', cue: '如果想走神，只是把目光轻轻带回来。' },
      { title: '5-4-3-2-1 感官回正', detail: '说出看到的 5 个物体、听到的 4 种声音。', cue: '数字人会一步一步带你把注意力拉回当下。' },
      { title: '专注收尾', detail: '给自己一个 30 秒小目标，例如读完一句话或完成一个小动作。', cue: '任务越小，越容易重新启动。' }
    ]
  },
  {
    id: 'restore' as const,
    title: '能量唤醒',
    subtitle: '适合低能量、回应偏慢、对事情提不起劲时',
    duration: '约 5 分钟',
    sceneCode: 'ENERGY_RESTORE',
    manualIndicatorCodes: ['LOW_ENERGY', 'GAZE_DRIFT'],
    cameraBaseIndicatorCodes: ['LOW_ENERGY'],
    targetExpression: '面部更有活力、眼神不再发空、动作更有启动感',
    targetBehavior: '从低负担行动开始，重新建立可完成感',
    syncTaskId: 'task-2',
    syncTaskTitle: '情绪日记',
    steps: [
      { title: '身体启动', detail: '抬肩一次、放松一次，再喝一口水。', cue: '数字人会用很轻的节奏提醒你动起来。' },
      { title: '30 秒微任务', detail: '整理桌面一角，或把手边物品归位。', cue: '先完成一个超小动作，帮大脑恢复“我做得到”的感觉。' },
      { title: '感受记录', detail: '写下一句现在的感受，例如“我有点累，但已经开始动了”。', cue: '不要求积极，只要求真实和简短。' }
    ]
  }
];

const companionProfiles = {
  focus: {
    name: '花花狸',
    tone: '会陪你把乱掉的念头一条条理顺',
    badge: '认知重构'
  },
  release: {
    name: '森森鹿',
    tone: '擅长在紧绷的时候陪你慢慢放松',
    badge: '呼吸安抚'
  },
  restore: {
    name: '咕咕熊',
    tone: '会陪你从一点点行动里找回能量',
    badge: '能量唤醒'
  }
} as const;

const studentSteps: StudentView[] = ['login', 'consent', 'screeningIntro', 'question', 'result', 'training', 'home'];

const currentQuestion = computed(() => screeningTemplate.value?.questions[currentQuestionIndex.value]);
const studentName = computed(() => session.value?.student.studentName ?? loginForm.studentName);
const completedAnswers = computed(() => answers.value.filter(Boolean).length);
const todayCompletedTaskCount = computed(() => {
  return todayTraining.value?.tasks.filter((task) => task.status === 'completed').length ?? 0;
});
const currentProgressView = computed<StudentView>(() => {
  return studentSteps.includes(studentView.value) ? studentView.value : 'home';
});
const activeDigitalScene = computed(() => {
  return digitalScenes.find((scene) => scene.id === activeDigitalSceneId.value) ?? digitalScenes[0];
});
const activeCompanion = computed(() => companionProfiles[activeDigitalSceneId.value]);
const currentDigitalStep = computed(() => activeDigitalScene.value.steps[digitalStepIndex.value] ?? null);
const linkedDigitalTask = computed(() => {
  return todayTraining.value?.tasks.find((task) => task.id === activeDigitalScene.value.syncTaskId) ?? null;
});
const digitalCompletionText = computed(() => {
  return `${Math.min(digitalStepIndex.value + 1, activeDigitalScene.value.steps.length)} / ${activeDigitalScene.value.steps.length}`;
});
const faceDetectorAvailable = computed(() => typeof window !== 'undefined' && 'FaceDetector' in window);
const cameraPreviewActive = computed(() => cameraStatus.value === 'active' && !!cameraStream.value);
const cameraStatusBadge = computed(() => {
  switch (cameraStatus.value) {
    case 'active':
      return '摄像头已开启';
    case 'blocked':
      return '未拿到权限';
    case 'unsupported':
      return '设备不支持';
    default:
      return '使用默认指标';
  }
});
const cameraSupportBadge = computed(() => {
  return faceDetectorAvailable.value ? '支持本地轻量取景检测' : '当前浏览器仅支持预览';
});
const cameraFaceText = computed(() => {
  if (!cameraPreviewActive.value) {
    return '未开启实时预览';
  }
  if (cameraFaceState.value === 'detected') {
    return '已检测到正脸预览';
  }
  if (cameraFaceState.value === 'missing') {
    return '暂未稳定识别到正脸';
  }
  return '预览已开启，等待采集';
});
const cameraAnalysisLabel = computed(() => {
  return cameraLastAnalysisSource.value === 'camera'
    ? '最近一次建议来自摄像头辅助采集'
    : '当前结果来自训练场景默认指标';
});
const cameraSummaryText = computed(() => {
  return cameraCaptureSummary.value || '本次版本默认不保存原始视频，只记录观察摘要和建议结果。';
});
const recentObservationRecords = computed(() => observationHistory.value?.records ?? []);
const latestObservationRecord = computed(() => recentObservationRecords.value[0] ?? null);
const homeMoodHeadline = computed(() => {
  if (latestObservationRecord.value) {
    return latestObservationRecord.value.indicatorSummary;
  }
  return '今天也可以慢一点，我们先从一句话开始。';
});
const currentTeacherAlertId = computed(() => teacherAlerts.value?.highlightedDetail?.id ?? '');
const currentTeacherAlertItem = computed(() => {
  return teacherAlerts.value?.alerts.find((alert) => alert.id === currentTeacherAlertId.value) ?? null;
});
const teacherRecentObservationRecords = computed(() => teacherAlerts.value?.highlightedDetail?.recentObservations ?? []);
const teacherFollowUpTip = computed(() => teacherAlerts.value?.highlightedDetail?.followUpTip ?? null);
const teacherActionTimeline = computed(() => teacherAlerts.value?.highlightedDetail?.actionTimeline ?? []);
const activeManualIndicatorLabels = computed(() => activeDigitalScene.value.manualIndicatorCodes.map(getIndicatorLabel));

function chooseRole(role: Role) {
  selectedRole.value = role;
  error.value = '';
  if (role !== 'student') {
    stopCameraPreview();
  }
  if (role === 'guardian' && !guardianSummary.value) {
    void loadGuardianSummary();
  }
  if (role === 'teacher' && !teacherAlerts.value) {
    void loadTeacherAlerts();
  }
}

function isStepDone(view: StudentView) {
  return studentSteps.indexOf(view) < studentSteps.indexOf(currentProgressView.value);
}

async function withLoading(action: () => Promise<void>) {
  loading.value = true;
  error.value = '';
  try {
    await action();
  } catch (err) {
    error.value = err instanceof Error ? err.message : String(err);
  } finally {
    loading.value = false;
  }
}

async function loginStudent() {
  await withLoading(async () => {
    session.value = await api.loginStudent(loginForm);
    consentStatus.value = await api.getConsentStatus(session.value.student.studentId);
    Object.assign(consentForm, {
      guardianConsent: consentStatus.value.guardianConsent,
      studentAssent: consentStatus.value.studentAssent,
      cameraTrainingConsent: consentStatus.value.cameraTrainingConsent,
      avatarConsent: consentStatus.value.avatarConsent
    });
    studentView.value = 'consent';
  });
}

async function submitConsent() {
  if (!session.value) return;

  await withLoading(async () => {
    consentStatus.value = await api.submitConsent({
      studentId: session.value!.student.studentId,
      ...consentForm
    });

    if (!consentStatus.value.allRequiredCompleted) {
      throw new Error('请先完成监护人同意、学生知情同意和摄像头训练授权。');
    }

    screeningTemplate.value = await api.getScreeningTemplate();
    answers.value = screeningTemplate.value.questions.map(() => '');
    currentQuestionIndex.value = 0;
    studentView.value = 'screeningIntro';
  });
}

function startQuestions() {
  studentView.value = 'question';
}

function selectAnswer(option: string) {
  answers.value[currentQuestionIndex.value] = option;
}

function goPreviousQuestion() {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value -= 1;
  }
}

async function goNextQuestion() {
  if (!answers.value[currentQuestionIndex.value]) {
    error.value = '请先选择一个答案。';
    return;
  }

  if (!screeningTemplate.value) return;

  if (currentQuestionIndex.value < screeningTemplate.value.questions.length - 1) {
    currentQuestionIndex.value += 1;
    error.value = '';
    return;
  }

  await submitScreening();
}

async function submitScreening() {
  if (!session.value) return;

  await withLoading(async () => {
    screeningResult.value = await api.submitScreening({
      studentId: session.value!.student.studentId,
      sleepScore: mapSleepScore(answers.value[0]),
      stressScore: mapStressScore(answers.value[1]),
      answers: answers.value,
      note: note.value
    });
    studentView.value = 'result';
  });
}

async function loadTraining(target: StudentView = 'training') {
  if (!session.value) return;

  await withLoading(async () => {
    todayTraining.value = await api.getTodayTraining(session.value!.student.studentId);
    if (target === 'home') {
      observationHistory.value = await fetchObservationHistory();
    }
    studentView.value = target;
  });
}

async function toggleTrainingTask(task: TrainingTask) {
  const studentId = session.value?.student.studentId;
  if (!studentId) return;

  await withLoading(async () => {
    todayTraining.value = await api.updateTrainingTaskStatus({
      studentId,
      taskId: task.id,
      status: task.status === 'completed' ? 'pending' : 'completed'
    });
    guardianSummary.value = null;
  });
}

function enterHome() {
  void loadTraining('home');
}

function openAiCoach() {
  studentView.value = 'ai';
  error.value = '';
}

async function openDigitalHumanMode(sceneId: DigitalSceneId = activeDigitalSceneId.value) {
  studentView.value = 'digitalHuman';
  error.value = '';
  if (!todayTraining.value && session.value) {
    await withLoading(async () => {
      todayTraining.value = await api.getTodayTraining(session.value!.student.studentId);
    });
  }
  await selectDigitalScene(sceneId);
}

async function sendAiMessage() {
  const content = aiInput.value.trim();
  if (!content) return;

  aiMessages.value.push({ role: 'student', content });
  aiInput.value = '';

  await withLoading(async () => {
    const response = await api.sendAiCoachMessage({
      studentId: session.value?.student.studentId ?? loginForm.studentId,
      sessionId: aiSessionId.value ?? undefined,
      message: content
    });
    aiSessionId.value = response.sessionId;
    aiMessages.value.push({
      role: 'assistant',
      content: response.reply
    });

    if (response.crisisDetected) {
      teacherAlerts.value = null;
      guardianSummary.value = null;
    }
  });
}

function restartScreening() {
  studentView.value = 'screeningIntro';
  currentQuestionIndex.value = 0;
  answers.value = screeningTemplate.value?.questions.map(() => '') ?? [];
  note.value = '';
  screeningResult.value = null;
}

async function loadTeacherAlerts(highlightAlertId = currentTeacherAlertId.value || undefined) {
  await withLoading(async () => {
    teacherAlerts.value = await api.getTeacherAlerts(highlightAlertId);
  });
}

async function selectTeacherAlert(alertId: string) {
  if (!alertId || alertId === currentTeacherAlertId.value) {
    return;
  }

  await loadTeacherAlerts(alertId);
}

function isActiveTeacherAlert(alertId: string) {
  return alertId === currentTeacherAlertId.value;
}

function applyTeacherNoteTemplate(template: string) {
  const current = teacherActionNote.value.trim();
  teacherActionNote.value = current ? `${current}；${template}` : template;
  error.value = '';
}

async function loadGuardianSummary() {
  await withLoading(async () => {
    guardianSummary.value = await api.getGuardianSummary(session.value?.student.studentId ?? loginForm.studentId);
  });
}

async function updateTeacherAlert(status: string, note: string) {
  const alertId = currentTeacherAlertId.value;
  if (!alertId) return;
  const normalizedNote = note.trim();

  await withLoading(async () => {
    teacherAlerts.value = await api.updateAlertStatus(alertId, {
      status,
      actorId: 'teacher-demo',
      note: normalizedNote
    });
    teacherActionStatus.value = status;
    teacherActionNote.value = '';
  });
}

async function submitTeacherActionNote() {
  const normalizedNote = teacherActionNote.value.trim();
  if (!normalizedNote) {
    error.value = '请先输入老师跟进备注，再保存。';
    return;
  }

  await updateTeacherAlert(teacherActionStatus.value, normalizedNote);
}

async function loadWellbeingObservation() {
  await loadManualSceneObservation(activeDigitalScene.value);
}

async function fetchObservationHistory(limit = 6) {
  return api.getWellbeingObservationHistory(session.value?.student.studentId ?? loginForm.studentId, limit);
}

async function loadObservationHistory(limit = 6) {
  await withLoading(async () => {
    observationHistory.value = await fetchObservationHistory(limit);
  });
}

async function loadSceneObservation(
  indicatorCodes: string[],
  sourceType = 'manual_indicator_input',
  sceneCode = activeDigitalScene.value.sceneCode,
  captureSummary = ''
) {
  await withLoading(async () => {
    wellbeingObservation.value = await api.analyzeWellbeingObservation({
      studentId: session.value?.student.studentId ?? loginForm.studentId,
      indicatorCodes,
      sourceType,
      sceneCode,
      captureSummary
    });
    observationHistory.value = await fetchObservationHistory();
  });
}

async function loadManualSceneObservation(scene: DigitalScene = activeDigitalScene.value) {
  cameraLastAnalysisSource.value = 'manual';
  cameraCaptureSummary.value = '当前展示为训练场景默认辅助指标，可结合摄像头重新采集。';
  if (cameraStatus.value === 'active') {
    cameraStatusText.value = '摄像头已开启，可采集当前状态覆盖默认分析。';
  } else if (cameraStatus.value !== 'blocked' && cameraStatus.value !== 'unsupported') {
    cameraStatusText.value = '当前展示为场景默认分析，可开启摄像头采集实时状态。';
  }
  await loadSceneObservation(scene.manualIndicatorCodes, 'manual_indicator_input', scene.sceneCode, '当前为训练场景默认辅助指标。');
}

async function selectDigitalScene(sceneId: DigitalSceneId) {
  const scene = digitalScenes.find((item) => item.id === sceneId) ?? digitalScenes[0];
  activeDigitalSceneId.value = sceneId;
  digitalStepIndex.value = 0;
  digitalSessionComplete.value = false;
  await loadManualSceneObservation(scene);
}

function nextDigitalStep() {
  if (digitalStepIndex.value < activeDigitalScene.value.steps.length - 1) {
    digitalStepIndex.value += 1;
    return;
  }
  digitalSessionComplete.value = true;
}

async function syncDigitalTrainingTask() {
  const studentId = session.value?.student.studentId;
  const task = linkedDigitalTask.value;
  if (!studentId || !task || task.status === 'completed') {
    return;
  }

  await withLoading(async () => {
    todayTraining.value = await api.updateTrainingTaskStatus({
      studentId,
      taskId: task.id,
      status: 'completed'
    });
    guardianSummary.value = null;
  });
}

async function bindCameraStream() {
  if (!cameraVideoRef.value || !cameraStream.value) {
    return;
  }
  cameraVideoRef.value.srcObject = cameraStream.value;
  try {
    await cameraVideoRef.value.play();
  } catch {
    cameraStatusText.value = '摄像头已开启，但浏览器尚未开始播放预览，可再次点击采集。';
  }
}

async function startCameraPreview() {
  error.value = '';
  if (!cameraSupported) {
    cameraStatus.value = 'unsupported';
    cameraStatusText.value = '当前浏览器或设备不支持摄像头访问，可继续使用场景默认指标。';
    return;
  }

  if (cameraStream.value) {
    cameraStatus.value = 'active';
    cameraStatusText.value = '摄像头已开启，可直接采集当前状态。';
    await bindCameraStream();
    return;
  }

  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'user' },
      audio: false
    });
    cameraStream.value = stream;
    cameraStatus.value = 'active';
    cameraFaceState.value = 'unknown';
    cameraStatusText.value = '摄像头已开启，当前只做本地轻量预览与辅助取景。';
    await bindCameraStream();
  } catch (err) {
    cameraStatus.value = 'blocked';
    cameraFaceState.value = 'unknown';
    cameraStatusText.value = '没有拿到摄像头权限，请在浏览器里允许访问后再试。';
    error.value = err instanceof Error ? err.message : '摄像头开启失败';
  }
}

function stopCameraPreview() {
  cameraStream.value?.getTracks().forEach((track) => track.stop());
  cameraStream.value = null;
  if (cameraVideoRef.value) {
    cameraVideoRef.value.pause();
    cameraVideoRef.value.srcObject = null;
  }
  cameraFaceState.value = 'unknown';
  cameraStatus.value = cameraSupported ? 'idle' : 'unsupported';
  cameraStatusText.value = cameraSupported
    ? '摄像头已关闭，可继续使用场景默认指标或重新开启采集。'
    : '当前浏览器或设备不支持摄像头访问，可先使用场景默认指标。';
}

async function deriveCameraObservation(): Promise<CameraObservationPayload> {
  const indicatorCodes = new Set(activeDigitalScene.value.cameraBaseIndicatorCodes);
  const fallbackSourceType = cameraStream.value ? 'camera_preview' : 'manual_indicator_input';
  const video = cameraVideoRef.value;

  if (!cameraSupported) {
    cameraStatus.value = 'unsupported';
    cameraFaceState.value = 'unknown';
    return {
      indicatorCodes: [...indicatorCodes],
      captureSummary: '当前浏览器或设备不支持摄像头访问，本次使用训练场景基础指标。',
      localHint: '当前浏览器不支持摄像头能力，已回退到场景辅助分析。',
      sourceType: 'manual_indicator_input'
    };
  }

  if (!cameraStream.value) {
    return {
      indicatorCodes: [...indicatorCodes],
      captureSummary: '摄像头未开启，本次使用训练场景基础指标。',
      localHint: '摄像头未开启，已回退到场景辅助分析。',
      sourceType: 'manual_indicator_input'
    };
  }

  if (!video || video.readyState < 2 || !video.videoWidth || !video.videoHeight) {
    cameraFaceState.value = 'unknown';
    return {
      indicatorCodes: [...indicatorCodes],
      captureSummary: '摄像头画面暂未就绪，本次先沿用当前训练场景的基础指标。',
      localHint: '画面还未准备好，已先使用场景基础指标。',
      sourceType: fallbackSourceType
    };
  }

  const browserWindow = window as WindowWithFaceDetector;
  if (!browserWindow.FaceDetector) {
    cameraFaceState.value = 'unknown';
    return {
      indicatorCodes: [...indicatorCodes],
      captureSummary: '已开启摄像头预览，但当前浏览器不支持本地人脸检测接口，保留场景基础指标。',
      localHint: '已开启摄像头，但当前浏览器只支持预览，不支持轻量取景检测。',
      sourceType: fallbackSourceType
    };
  }

  try {
    const detector = new browserWindow.FaceDetector({
      fastMode: true,
      maxDetectedFaces: 1
    });
    const detections = await detector.detect(video);
    const firstFace = detections[0];

    if (!firstFace?.boundingBox) {
      indicatorCodes.add('GAZE_DRIFT');
      indicatorCodes.add('LOW_ATTENTION');
      cameraFaceState.value = 'missing';
      return {
        indicatorCodes: [...indicatorCodes],
        captureSummary: '已开启摄像头预览，但暂未稳定检测到正脸，可能存在离屏、遮挡或取景偏移。',
        localHint: '暂未稳定识别到正脸，本次已将离屏和注意力波动作为训练辅助信号。',
        sourceType: fallbackSourceType
      };
    }

    const centerX = (firstFace.boundingBox.x + firstFace.boundingBox.width / 2) / video.videoWidth;
    const centerY = (firstFace.boundingBox.y + firstFace.boundingBox.height / 2) / video.videoHeight;
    const offsetX = Math.abs(centerX - 0.5);
    const offsetY = Math.abs(centerY - 0.5);
    const areaRatio = (firstFace.boundingBox.width * firstFace.boundingBox.height) / (video.videoWidth * video.videoHeight);
    const summaryParts = ['已通过本地摄像头预览检测到正脸'];

    cameraFaceState.value = 'detected';

    if (offsetX > 0.18 || offsetY > 0.24) {
      indicatorCodes.add('GAZE_DRIFT');
      summaryParts.push('取景中心偏移较明显');
    } else {
      summaryParts.push('取景基本居中');
    }

    if (areaRatio < 0.08) {
      indicatorCodes.add('LOW_ATTENTION');
      summaryParts.push('人脸占比偏小，可能离屏较远或坐姿后仰');
    } else {
      summaryParts.push('画面距离相对稳定');
    }

    return {
      indicatorCodes: [...indicatorCodes],
      captureSummary: summaryParts.join('；'),
      localHint: '已基于本地预览的取景稳定度生成辅助指标，不默认保存原始视频。',
      sourceType: fallbackSourceType
    };
  } catch {
    cameraFaceState.value = 'unknown';
    return {
      indicatorCodes: [...indicatorCodes],
      captureSummary: '摄像头已开启，但本地轻量检测未能完成，本次保留场景基础指标。',
      localHint: '当前浏览器的人脸检测能力不可用，已回退到场景辅助分析。',
      sourceType: fallbackSourceType
    };
  }
}

async function captureCameraObservation() {
  const cameraObservation = await deriveCameraObservation();
  cameraLastAnalysisSource.value = cameraObservation.sourceType === 'camera_preview' ? 'camera' : 'manual';
  cameraCaptureSummary.value = cameraObservation.captureSummary;
  cameraStatusText.value = cameraObservation.localHint;
  await loadSceneObservation(
    cameraObservation.indicatorCodes,
    cameraObservation.sourceType,
    activeDigitalScene.value.sceneCode,
    cameraObservation.captureSummary
  );
}

function mapSleepScore(answer: string) {
  return answer === '很好' ? 2 : answer === '一般' ? 5 : answer === '不太好' ? 7 : 9;
}

function mapStressScore(answer: string) {
  return answer === '几乎没有' ? 1 : answer === '偶尔' ? 4 : answer === '经常' ? 7 : 9;
}

function getIndicatorLabel(code: string) {
  switch (code) {
    case 'GAZE_DRIFT':
      return '视线游离';
    case 'LOW_ATTENTION':
      return '注意力波动';
    case 'LOW_ENERGY':
      return '能量偏低';
    case 'TENSION':
      return '紧张迹象';
    default:
      return code;
  }
}

function formatObservationTime(observedAt: string) {
  const date = new Date(observedAt);
  if (Number.isNaN(date.getTime())) {
    return '刚刚';
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

watch(currentTeacherAlertId, () => {
  teacherActionStatus.value = currentTeacherAlertItem.value?.status ?? '处理中';
  teacherActionNote.value = '';
});

watch(studentView, (nextView, previousView) => {
  if (previousView === 'digitalHuman' && nextView !== 'digitalHuman') {
    stopCameraPreview();
  }
});

onBeforeUnmount(() => {
  stopCameraPreview();
});
</script>

<template>
  <main class="workspace">
    <aside class="side-panel">
      <div class="brand-panel">
        <p class="eyebrow">Forest Companion</p>
        <h1>林间聊愈室</h1>
        <p class="side-copy">把心理健康产品改成更像陪伴应用的样子，让用户先感到被接住，再愿意慢慢打开自己。</p>
      </div>

      <div class="feature-stack">
        <article class="feature-note warm">
          <strong>24 小时安心倾诉</strong>
          <p>先说情绪，再给建议，不把人直接丢进冷冰冰的流程里。</p>
        </article>
        <article class="feature-note blue">
          <strong>思维工具卡</strong>
          <p>把认知重构、呼吸舒压、能量唤醒拆成更轻的互动模块。</p>
        </article>
        <article class="feature-note green">
          <strong>成长日记</strong>
          <p>把每次倾诉后的微小变化沉淀下来，形成连续的自我故事。</p>
        </article>
      </div>

      <div class="role-tabs" aria-label="角色切换">
        <button
          v-for="role in roles"
          :key="role.key"
          class="role-tab"
          :class="{ active: selectedRole === role.key }"
          type="button"
          @click="chooseRole(role.key)"
        >
          <component :is="role.icon" :size="18" />
          <span>{{ role.label }}</span>
        </button>
      </div>
    </aside>

    <section class="phone-shell" aria-label="应用预览">
      <div class="phone-status">
        <span>9:41</span>
        <span>● ● ●</span>
      </div>

      <div v-if="error" class="error-banner">
        {{ error }}
      </div>

      <template v-if="selectedRole === 'student'">
        <nav class="stepper" aria-label="学生流程">
          <span
            v-for="step in studentSteps"
            :key="step"
            :class="{ done: isStepDone(step), current: currentProgressView === step }"
          />
        </nav>

        <section v-if="studentView === 'login'" class="screen">
          <header class="screen-header">
            <p>学生端</p>
            <h2>学生登录</h2>
          </header>

          <label class="field">
            <span>学校代码</span>
            <input v-model="loginForm.schoolCode" autocomplete="off" />
          </label>
          <label class="field">
            <span>学号</span>
            <input v-model="loginForm.studentId" autocomplete="off" />
          </label>
          <label class="field">
            <span>姓名</span>
            <input v-model="loginForm.studentName" autocomplete="off" />
          </label>

          <button class="primary-action" type="button" :disabled="loading" @click="loginStudent">
            <Loader2 v-if="loading" class="spin" :size="18" />
            <UserRound v-else :size="18" />
            <span>登录并继续</span>
          </button>
        </section>

        <section v-else-if="studentView === 'consent'" class="screen">
          <header class="screen-header compact">
            <p>{{ session?.school.schoolName }} · {{ session?.student.className }}</p>
            <h2>同意与授权</h2>
          </header>

          <label class="switch-row">
            <span>
              <strong>监护人同意</strong>
            </span>
            <input v-model="consentForm.guardianConsent" type="checkbox" />
          </label>
          <label class="switch-row">
            <span>
              <strong>学生知情同意</strong>
            </span>
            <input v-model="consentForm.studentAssent" type="checkbox" />
          </label>
          <label class="switch-row">
            <span>
              <strong>摄像头训练授权</strong>
            </span>
            <input v-model="consentForm.cameraTrainingConsent" type="checkbox" />
          </label>
          <label class="switch-row optional">
            <span>
              <strong>数字人授权</strong>
            </span>
            <input v-model="consentForm.avatarConsent" type="checkbox" />
          </label>

          <button class="primary-action" type="button" :disabled="loading" @click="submitConsent">
            <ShieldCheck :size="18" />
            <span>确认授权</span>
          </button>
        </section>

        <section v-else-if="studentView === 'screeningIntro'" class="screen">
          <header class="screen-header">
            <p>{{ studentName }}</p>
            <h2>{{ screeningTemplate?.title }}</h2>
          </header>

          <div class="metric-grid">
            <div>
              <span>题目</span>
              <strong>{{ screeningTemplate?.questionCount }}</strong>
            </div>
            <div>
              <span>预计</span>
              <strong>{{ screeningTemplate?.estimatedDuration }}</strong>
            </div>
          </div>

          <ul class="notice-list">
            <li>结果仅作为辅助筛查</li>
            <li>高风险表达会进入人工复核</li>
          </ul>

          <button class="primary-action" type="button" @click="startQuestions">
            <HeartPulse :size="18" />
            <span>开始答题</span>
          </button>
        </section>

        <section v-else-if="studentView === 'question'" class="screen">
          <header class="screen-header compact">
            <p>{{ completedAnswers }} / {{ screeningTemplate?.questionCount }}</p>
            <h2>筛查答题</h2>
          </header>

          <div class="progress-track">
            <span :style="{ width: `${((currentQuestionIndex + 1) / (screeningTemplate?.questionCount || 1)) * 100}%` }" />
          </div>

          <div class="question-block">
            <h3>{{ currentQuestion?.title }}</h3>
            <button
              v-for="option in currentQuestion?.options"
              :key="option"
              class="option-row"
              :class="{ selected: answers[currentQuestionIndex] === option }"
              type="button"
              @click="selectAnswer(option)"
            >
              <span>{{ option }}</span>
              <CheckCircle2 v-if="answers[currentQuestionIndex] === option" :size="18" />
            </button>
          </div>

          <label class="field">
            <span>补充说明</span>
            <textarea v-model="note" rows="3" placeholder="可以写下最近的感受" />
          </label>

          <div class="split-actions">
            <button class="secondary-action" type="button" :disabled="currentQuestionIndex === 0" @click="goPreviousQuestion">
              上一题
            </button>
            <button class="primary-action" type="button" :disabled="loading" @click="goNextQuestion">
              {{ currentQuestionIndex === (screeningTemplate?.questionCount || 1) - 1 ? '提交' : '下一题' }}
            </button>
          </div>
        </section>

        <section v-else-if="studentView === 'result'" class="screen">
          <header class="screen-header">
            <p>筛查结果</p>
            <h2>{{ screeningResult?.trend }}</h2>
          </header>
          <div class="list-panel">
            <h3>下一步建议</h3>
            <div v-for="item in screeningResult?.trainingSuggestions" :key="item" class="list-row">
              <CheckCircle2 :size="18" />
              <span>{{ item }}</span>
            </div>
          </div>

          <div class="safety-panel">
            <LockKeyhole :size="18" />
            <span>{{ screeningResult?.safetyNotice }}</span>
          </div>

          <button class="primary-action" type="button" :disabled="loading" @click="loadTraining('training')">
            <ClipboardCheck :size="18" />
            <span>查看训练计划</span>
          </button>
        </section>

        <section v-else-if="studentView === 'training'" class="screen">
          <header class="screen-header compact">
            <p>{{ todayTraining?.encouragement }}</p>
            <h2>今日训练计划</h2>
          </header>

          <div class="task-list">
            <button
              v-for="task in todayTraining?.tasks"
              :key="task.id"
              class="task-row task-button"
              :class="{ completed: task.status === 'completed' }"
              type="button"
              :disabled="loading"
              @click="toggleTrainingTask(task)"
            >
              <CheckCircle2 :size="19" />
              <span>
                <strong>{{ task.title }}</strong>
                <small>{{ task.duration }}</small>
              </span>
              <em>{{ task.status === 'completed' ? '已完成' : '待完成' }}</em>
            </button>
          </div>

          <button class="primary-action" type="button" @click="enterHome">
            <Home :size="18" />
            <span>进入首页</span>
          </button>
        </section>

        <section v-else-if="studentView === 'home'" class="screen">
          <header class="screen-header forest-home-header">
            <p>{{ session?.school.schoolName }} · {{ session?.student.className }}</p>
            <h2>你好，{{ studentName }}</h2>
            <strong class="home-main-copy">难过的时候，也可以被温柔接住。</strong>
            <span class="home-sub-copy">{{ homeMoodHeadline }}</span>
          </header>

          <div class="forest-companion-card">
            <div class="companion-figure">狸</div>
            <div class="companion-copy">
              <span class="companion-tag">今日聊愈师</span>
              <strong>{{ activeCompanion.name }}</strong>
              <p>{{ activeCompanion.tone }}</p>
            </div>
            <button type="button" class="primary-action companion-action" @click="openAiCoach">
              立即倾诉
            </button>
          </div>

          <div class="forest-stat-grid">
            <div>
              <span>今日进度</span>
              <strong>{{ todayCompletedTaskCount }}/{{ todayTraining?.tasks.length ?? 0 }}</strong>
            </div>
            <div>
              <span>推荐主题</span>
              <strong>{{ activeCompanion.badge }}</strong>
            </div>
            <div>
              <span>最近记录</span>
              <strong>{{ latestObservationRecord?.sceneLabel ?? '还没有' }}</strong>
            </div>
          </div>

          <div class="forest-entry-grid">
            <button type="button" class="forest-entry primary" @click="openAiCoach">
              <span class="forest-entry-kicker">陪伴对话</span>
              <strong>和 {{ activeCompanion.name }} 聊聊</strong>
              <p>适合现在就想说说感受的时候</p>
            </button>
            <button type="button" class="forest-entry" @click="openDigitalHumanMode('focus')">
              <span class="forest-entry-kicker">思维工具</span>
              <strong>做一次重构练习</strong>
              <p>把焦虑和乱想拆开看看</p>
            </button>
            <button type="button" class="forest-entry" @click="loadObservationHistory()">
              <span class="forest-entry-kicker">成长日记</span>
              <strong>看看最近的自己</strong>
              <p>把每次起伏变成可以回看的轨迹</p>
            </button>
          </div>

          <div class="forest-journal-card">
            <div class="summary-card-head">
              <span>
                <strong>成长摘记</strong>
              </span>
              <button type="button" @click="openDigitalHumanMode('focus')">
                去记录
              </button>
            </div>

            <template v-if="latestObservationRecord">
              <strong>{{ latestObservationRecord.sceneLabel }}</strong>
              <p>{{ latestObservationRecord.indicatorSummary }}</p>
              <div class="summary-tags">
                <span>{{ formatObservationTime(latestObservationRecord.observedAt) }}</span>
                <span v-for="feature in latestObservationRecord.featureHighlights" :key="`home-${feature.code}`">
                  {{ feature.label }}
                </span>
              </div>
            </template>

            <p v-else class="summary-empty">
              今天的故事还没开始，先做一次聊愈练习吧。
            </p>
          </div>

          <div class="quick-actions forest-quick-actions">
            <button type="button" @click="restartScreening">
              <HeartPulse :size="18" />
              <span>重新筛查</span>
            </button>
            <button type="button" @click="studentView = 'consent'">
              <ShieldCheck :size="18" />
              <span>授权管理</span>
            </button>
            <button type="button" @click="openDigitalHumanMode('release')">
              <Sparkles :size="18" />
              <span>舒压练习</span>
            </button>
            <button type="button" @click="openDigitalHumanMode('restore')">
              <Bot :size="18" />
              <span>能量唤醒</span>
            </button>
          </div>
        </section>

        <section v-else-if="studentView === 'ai'" class="screen ai-screen">
          <header class="screen-header ai-header forest-ai-header">
            <p>24 小时安心倾诉</p>
            <h2>{{ activeCompanion.name }} 正在等你开口</h2>
            <span class="home-sub-copy">先说感受，不急着把自己解释清楚。</span>
          </header>

          <div class="forest-speaker-card">
            <div class="companion-figure large">狸</div>
            <div class="companion-copy">
              <span class="companion-tag">你的聊愈伙伴</span>
              <strong>{{ activeCompanion.name }}</strong>
              <p>{{ activeCompanion.tone }}</p>
            </div>
          </div>

          <button class="secondary-action ai-mode-entry forest-mode-entry" type="button" :disabled="loading" @click="openDigitalHumanMode('focus')">
            <Sparkles :size="18" />
            <span>切到思维工具</span>
          </button>

          <div class="observation-card forest-observation-card">
            <div class="observation-head">
              <span>
                <strong>聊愈建议</strong>
              </span>
              <button type="button" :disabled="loading" @click="loadWellbeingObservation">
                生成
              </button>
            </div>

            <template v-if="wellbeingObservation">
              <div class="mini-section">
                <h3>表情/行为指标</h3>
                <p v-for="indicator in wellbeingObservation.expressionIndicators" :key="indicator.code">
                  {{ indicator.label }}：{{ indicator.observation }}
                </p>
              </div>

              <div class="mini-section">
                <h3>可能状态</h3>
                <p v-for="indicator in wellbeingObservation.wellbeingIndicators" :key="indicator.code">
                  {{ indicator.label }}。{{ indicator.explanation }}
                </p>
              </div>

              <div class="mini-section">
                <h3>行为建议</h3>
                <p v-for="suggestion in wellbeingObservation.behaviorSuggestions" :key="suggestion.code">
                  {{ suggestion.title }}：{{ suggestion.detail }}
                </p>
              </div>

              <div class="mini-section two-column">
                <p v-for="suggestion in wellbeingObservation.musicSuggestions" :key="suggestion.code">
                  音乐：{{ suggestion.title }}
                </p>
                <p v-for="suggestion in wellbeingObservation.dietSuggestions" :key="suggestion.code">
                  膳食：{{ suggestion.title }}
                </p>
              </div>

              <div class="safety-panel observation-compact">
                <ShieldCheck :size="18" />
                <span>{{ wellbeingObservation.disclaimer }}</span>
              </div>

              <article class="chat-bubble assistant observation-script">
                {{ wellbeingObservation.avatarScript }}
              </article>
            </template>
          </div>

          <div class="chat-list forest-chat-list">
            <article v-for="(message, index) in aiMessages" :key="index" class="chat-bubble" :class="message.role">
              {{ message.content }}
            </article>
          </div>

          <div class="suggestion-strip">
            <button type="button" @click="aiInput = '我今天有点紧张，想放松一下'">紧张</button>
            <button type="button" @click="aiInput = '我睡不太好，想做呼吸练习'">睡眠</button>
            <button type="button" @click="aiInput = '我想记录今天的一件小事'">记录</button>
          </div>

          <form class="chat-input" @submit.prevent="sendAiMessage">
            <input v-model="aiInput" placeholder="输入你的感受..." />
            <button type="submit" aria-label="发送" :disabled="loading">
              <Send :size="18" />
            </button>
          </form>

          <button class="forest-voice-button" type="button">
            <span>按住倾诉</span>
          </button>

          <button class="secondary-action back-home" type="button" @click="studentView = 'home'">
            回到首页
          </button>
        </section>

        <section v-else-if="studentView === 'digitalHuman'" class="screen digital-screen">
          <header class="screen-header ai-header digital-header forest-tool-header">
            <p>海量思维工具</p>
            <h2>{{ activeCompanion.badge }}</h2>
            <span class="home-sub-copy">{{ activeCompanion.name }} 会陪你把感受拆开一点点看。</span>
          </header>

          <div class="digital-hero forest-digital-hero">
            <div class="digital-avatar">
              <div class="companion-figure large">狸</div>
              <span>
                <strong>{{ activeDigitalScene.title }}</strong>
                <small>{{ activeDigitalScene.subtitle }}</small>
              </span>
            </div>
          </div>

          <div class="scene-tabs">
            <button
              v-for="scene in digitalScenes"
              :key="scene.id"
              class="scene-pill"
              :class="{ active: activeDigitalSceneId === scene.id }"
              type="button"
              :disabled="loading"
              @click="selectDigitalScene(scene.id)"
            >
              <strong>{{ scene.title }}</strong>
            </button>
          </div>

          <div class="manual-checkin-card">
            <div class="summary-card-head">
              <span>
                <strong>本次探索线索</strong>
              </span>
              <button type="button" :disabled="loading" @click="loadManualSceneObservation()">
                开始分析
              </button>
            </div>

            <div class="summary-tags">
              <span>{{ activeDigitalScene.title }}</span>
              <span v-for="label in activeManualIndicatorLabels" :key="label">
                {{ label }}
              </span>
            </div>
          </div>

          <div class="digital-grid">
            <article class="coach-stage">
              <p class="stage-label">当前步骤</p>
              <strong>{{ currentDigitalStep?.title }}</strong>
              <p>{{ currentDigitalStep?.detail }}</p>
              <div class="stage-progress">
                <span :style="{ width: `${(Math.max(digitalStepIndex, 0) + 1) / activeDigitalScene.steps.length * 100}%` }" />
              </div>
              <em>{{ digitalCompletionText }}</em>
            </article>

            <article class="digital-summary">
              <p class="stage-label">聊愈师提示</p>
              <strong>{{ wellbeingObservation?.avatarScript ?? '先生成观察建议，再进入本轮训练。' }}</strong>
            </article>
          </div>

          <div class="observation-card">
            <div class="observation-head">
              <span>
                <strong>本轮状态观察</strong>
              </span>
              <button type="button" :disabled="loading" @click="loadManualSceneObservation()">
                重新分析
              </button>
            </div>

            <template v-if="wellbeingObservation">
              <div class="mini-section">
                <h3>可能状态</h3>
                <p v-for="indicator in wellbeingObservation.wellbeingIndicators" :key="indicator.code">
                  {{ indicator.label }}：{{ indicator.explanation }}
                </p>
              </div>

              <div class="mini-section three-column">
                <div>
                  <h3>行动</h3>
                  <p v-for="suggestion in wellbeingObservation.behaviorSuggestions" :key="suggestion.code">
                    {{ suggestion.title }}
                  </p>
                </div>
                <div>
                  <h3>声音</h3>
                  <p v-for="suggestion in wellbeingObservation.musicSuggestions" :key="suggestion.code">
                    {{ suggestion.title }}
                  </p>
                </div>
                <div>
                  <h3>饮食</h3>
                  <p v-for="suggestion in wellbeingObservation.dietSuggestions" :key="suggestion.code">
                    {{ suggestion.title }}
                  </p>
                </div>
              </div>
            </template>
          </div>

          <div class="history-card">
            <div class="observation-head">
              <span>
                <strong>最近观察记录</strong>
              </span>
              <button type="button" :disabled="loading" @click="loadObservationHistory()">
                刷新记录
              </button>
            </div>

            <template v-if="recentObservationRecords.length">
              <article v-for="record in recentObservationRecords" :key="record.sessionKey" class="history-row">
                <div class="history-row-head">
                  <strong>{{ record.sceneLabel }}</strong>
                  <span>{{ formatObservationTime(record.observedAt) }}</span>
                </div>

                <div class="history-row-tags">
                  <span>{{ record.sourceLabel }}</span>
                  <span>{{ record.status }}</span>
                </div>

                <p>{{ record.indicatorSummary }}</p>

                <div class="history-row-features">
                  <span v-for="feature in record.featureHighlights" :key="`${record.sessionKey}-${feature.code}`">
                    {{ feature.label }}
                  </span>
                </div>
              </article>
            </template>

            <p v-else class="history-empty">
              完成一次状态分析后，这里会自动沉淀最近记录。
            </p>
          </div>

          <div class="split-actions">
            <button class="secondary-action" type="button" :disabled="digitalStepIndex === 0" @click="digitalStepIndex -= 1">
              上一步
            </button>
            <button class="primary-action" type="button" :disabled="loading || digitalSessionComplete" @click="nextDigitalStep">
              {{ digitalStepIndex === activeDigitalScene.steps.length - 1 ? '完成本轮训练' : '继续下一步' }}
            </button>
          </div>

          <div v-if="digitalSessionComplete" class="digital-finish">
            <strong>本轮训练已完成</strong>
            <p>可以把这次数字人训练同步到今日任务，也可以继续和 AI 陪练沟通当前感受。</p>
            <div class="digital-finish-actions">
              <button class="primary-action" type="button" :disabled="loading || linkedDigitalTask?.status === 'completed'" @click="syncDigitalTrainingTask">
                <CheckCircle2 :size="18" />
                <span>{{ linkedDigitalTask?.status === 'completed' ? '已同步到今日训练' : `同步 ${activeDigitalScene.syncTaskTitle}` }}</span>
              </button>
              <button class="secondary-action" type="button" @click="studentView = 'ai'">
                <Bot :size="18" />
                <span>继续 AI 陪练</span>
              </button>
            </div>
          </div>

          <button class="secondary-action back-home" type="button" @click="studentView = 'home'">
            回到首页
          </button>
        </section>
      </template>

      <section v-else-if="selectedRole === 'guardian'" class="screen">
        <header class="screen-header parent">
          <p>{{ guardianSummary?.studentName ?? '林同学' }} · {{ guardianSummary?.weekLabel ?? '本周' }}</p>
          <h2>家长端摘要</h2>
        </header>

        <button class="secondary-action refresh" type="button" :disabled="loading" @click="loadGuardianSummary">
          刷新摘要
        </button>

        <div class="metric-grid parent-metrics">
          <div>
            <span>情绪稳定</span>
            <strong>{{ guardianSummary?.emotionLabel ?? '较稳定' }}</strong>
          </div>
          <div>
            <span>训练完成</span>
            <strong>{{ guardianSummary?.trainingCompletionRate ?? 0 }}%</strong>
          </div>
          <div>
            <span>提醒</span>
            <strong>{{ guardianSummary?.reminderCount ?? 0 }} 条</strong>
          </div>
        </div>

        <div class="trend-card">
          <span
            v-for="(value, index) in guardianSummary?.trendBars ?? [32, 46, 40, 58, 50, 42, 60]"
            :key="index"
            :style="{ height: `${value}%` }"
          />
        </div>

        <div class="list-panel">
          <h3>提醒事项</h3>
          <div v-for="reminder in guardianSummary?.reminders ?? []" :key="reminder" class="list-row">
            <Bell :size="18" />
            <span>{{ reminder }}</span>
          </div>
        </div>

        <div class="summary-card guardian-observation-card">
          <div class="summary-card-head">
            <span>
              <strong>最近训练观察</strong>
            </span>
          </div>

          <template v-if="guardianSummary?.latestObservation">
            <strong>{{ guardianSummary.latestObservation.sceneLabel }}</strong>
            <p>{{ guardianSummary.latestObservation.indicatorSummary }}</p>
            <div class="summary-tags">
              <span>{{ guardianSummary.latestObservation.sourceLabel }}</span>
              <span>{{ formatObservationTime(guardianSummary.latestObservation.observedAt) }}</span>
              <span v-for="tag in guardianSummary.latestObservation.focusTags" :key="tag">
                {{ tag }}
              </span>
            </div>
          </template>

          <p v-else class="summary-empty">
            当前还没有最近训练观察记录，建议先鼓励孩子完成一次训练打卡。
          </p>
        </div>

        <div class="privacy-card">
          <LockKeyhole :size="18" />
          <span>
            <strong>摘要可见</strong>
          </span>
        </div>
      </section>

      <section v-else class="screen">
        <header class="screen-header teacher">
          <p>学校心理老师</p>
          <h2>老师端预警</h2>
        </header>

        <button class="secondary-action refresh" type="button" :disabled="loading" @click="loadTeacherAlerts()">
          刷新预警
        </button>

        <div class="alert-list">
          <button
            v-for="alert in teacherAlerts?.alerts"
            :key="alert.id"
            type="button"
            class="alert-row"
            :class="[alert.riskLevel.toLowerCase(), { active: isActiveTeacherAlert(alert.id) }]"
            :disabled="loading"
            @click="selectTeacherAlert(alert.id)"
          >
            <span>{{ alert.riskLevel === 'HIGH' ? '高风险' : alert.riskLevel === 'MEDIUM' ? '中风险' : '低风险' }}</span>
            <strong>{{ alert.studentName }}</strong>
            <p>{{ alert.summary }}</p>
            <small>{{ alert.className }} · {{ alert.status }}</small>
          </button>
        </div>

        <div v-if="teacherAlerts?.highlightedDetail" class="list-panel">
          <h3>复核详情</h3>
          <p class="teacher-detail-meta">
            {{ teacherAlerts.highlightedDetail.studentName }} · {{ teacherAlerts.highlightedDetail.riskLabel }}
          </p>
          <p class="result-note">{{ teacherAlerts.highlightedDetail.reason }}</p>
          <div v-for="action in teacherAlerts.highlightedDetail.suggestedActions" :key="action" class="list-row">
            <PhoneCall :size="18" />
            <span>{{ action }}</span>
          </div>
        </div>

        <div v-if="teacherAlerts?.highlightedDetail" class="summary-card teacher-support-card">
          <div class="summary-card-head">
            <span>
              <strong>训练观察快照</strong>
            </span>
          </div>

          <template v-if="teacherAlerts.highlightedDetail.latestObservation">
            <div class="metric-grid teacher-metrics">
              <div>
                <span>今日训练完成</span>
                <strong>{{ teacherAlerts.highlightedDetail.trainingCompletionRate ?? 0 }}%</strong>
              </div>
              <div>
                <span>最近场景</span>
                <strong>{{ teacherAlerts.highlightedDetail.latestObservation.sceneLabel }}</strong>
              </div>
              <div>
                <span>观察时间</span>
                <strong>{{ formatObservationTime(teacherAlerts.highlightedDetail.latestObservation.observedAt) }}</strong>
              </div>
            </div>

            <p class="result-note">{{ teacherAlerts.highlightedDetail.latestObservation.indicatorSummary }}</p>

            <div class="summary-tags">
              <span>{{ teacherAlerts.highlightedDetail.latestObservation.sourceLabel }}</span>
              <span v-for="tag in teacherAlerts.highlightedDetail.latestObservation.focusTags" :key="tag">
                {{ tag }}
              </span>
            </div>
          </template>

          <p v-else class="summary-empty">
            当前还没有最近训练观察记录，建议先联系学生补做一次训练打卡后再综合判断。
          </p>

          <div
            v-if="teacherFollowUpTip"
            class="teacher-tip-card"
            :class="`is-${teacherFollowUpTip.level}`"
          >
            <strong>{{ teacherFollowUpTip.title }}</strong>
            <p>{{ teacherFollowUpTip.detail }}</p>
          </div>
        </div>

        <div v-if="teacherAlerts?.highlightedDetail" class="history-card teacher-timeline-card">
          <div class="observation-head">
            <span>
              <strong>最近训练时间线</strong>
            </span>
          </div>

          <template v-if="teacherRecentObservationRecords.length">
            <article
              v-for="(record, index) in teacherRecentObservationRecords"
              :key="`${teacherAlerts.highlightedDetail.id}-${record.observedAt}-${index}`"
              class="history-row"
            >
              <div class="history-row-head">
                <strong>{{ index === 0 ? `最新一次 · ${record.sceneLabel}` : record.sceneLabel }}</strong>
                <span>{{ formatObservationTime(record.observedAt) }}</span>
              </div>

              <div class="history-row-tags">
                <span>{{ record.sourceLabel }}</span>
                <span v-for="tag in record.focusTags" :key="`${record.observedAt}-${tag}`">
                  {{ tag }}
                </span>
              </div>

              <p>{{ record.indicatorSummary }}</p>
            </article>
          </template>

          <p v-else class="history-empty">
            当前还没有最近训练时间线，建议先提醒学生完成一次状态打卡或训练打卡。
          </p>
        </div>

        <div v-if="teacherAlerts?.highlightedDetail" class="history-card teacher-action-card">
          <div class="observation-head">
            <span>
              <strong>处置记录时间线</strong>
            </span>
          </div>

          <template v-if="teacherActionTimeline.length">
            <article
              v-for="(item, index) in teacherActionTimeline"
              :key="`${teacherAlerts.highlightedDetail.id}-${item.occurredAt}-${index}`"
              class="history-row teacher-action-row"
              :class="`is-${item.kind}`"
            >
              <div class="history-row-head">
                <strong>{{ item.title }}</strong>
                <span>{{ formatObservationTime(item.occurredAt) }}</span>
              </div>

              <div class="summary-tags teacher-action-tags">
                <span>{{ item.actorLabel }}</span>
                <span>{{ item.kind === 'created' ? '系统建单' : '老师跟进' }}</span>
              </div>

              <p>{{ item.detail }}</p>
            </article>
          </template>

          <p v-else class="history-empty">
            当前还没有处置记录，老师一旦开始跟进，这里会自动形成时间线。
          </p>
        </div>

        <div v-if="teacherAlerts?.highlightedDetail" class="summary-card teacher-note-card">
          <div class="summary-card-head">
            <span>
              <strong>老师跟进备注</strong>
            </span>
          </div>

          <div class="summary-tags">
            <span>当前状态：{{ currentTeacherAlertItem?.status ?? '待处理' }}</span>
            <span>当前学生：{{ teacherAlerts.highlightedDetail.studentName }}</span>
          </div>

          <label class="field teacher-note-field">
            <span>更新状态</span>
            <select v-model="teacherActionStatus">
              <option v-for="status in teacherActionStatusOptions" :key="status" :value="status">
                {{ status }}
              </option>
            </select>
          </label>

          <label class="field teacher-note-field">
            <span>跟进备注</span>
            <textarea
              v-model="teacherActionNote"
              rows="4"
              placeholder="例如：已与班主任沟通，计划明天课后单独询问学生近况。"
            />
          </label>

          <div class="teacher-note-templates">
            <button
              v-for="template in teacherActionNoteTemplates"
              :key="template"
              type="button"
              :disabled="loading"
              @click="applyTeacherNoteTemplate(template)"
            >
              {{ template }}
            </button>
          </div>

          <button
            class="primary-action teacher-note-submit"
            type="button"
            :disabled="loading || !teacherActionNote.trim()"
            @click="submitTeacherActionNote"
          >
            保存备注并更新状态
          </button>
        </div>

        <div class="teacher-actions">
          <button type="button" :disabled="loading" @click="updateTeacherAlert('处理中', '老师已开始线下复核')">
            标记处理中
          </button>
          <button type="button" :disabled="loading" @click="updateTeacherAlert('已记录处置', '已记录本次关怀与处置过程')">
            记录处置
          </button>
        </div>
      </section>
    </section>
  </main>
</template>
