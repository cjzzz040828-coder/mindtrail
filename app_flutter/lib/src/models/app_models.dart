class StudentSession {
  const StudentSession({
    required this.sessionToken,
    required this.role,
    required this.consentRequired,
    required this.nextStep,
    required this.studentId,
    required this.studentName,
    required this.className,
    required this.schoolName,
    required this.schoolCode,
  });

  factory StudentSession.fromJson(Map<String, dynamic> json) {
    final student = Map<String, dynamic>.from(json['student'] as Map);
    final school = Map<String, dynamic>.from(json['school'] as Map);
    return StudentSession(
      sessionToken: json['sessionToken'] as String,
      role: json['role'] as String,
      consentRequired: json['consentRequired'] as bool,
      nextStep: json['nextStep'] as String,
      studentId: student['studentId'] as String,
      studentName: student['studentName'] as String,
      className: student['className'] as String,
      schoolName: school['schoolName'] as String,
      schoolCode: school['schoolCode'] as String,
    );
  }

  final String sessionToken;
  final String role;
  final bool consentRequired;
  final String nextStep;
  final String studentId;
  final String studentName;
  final String className;
  final String schoolName;
  final String schoolCode;
}

class ConsentStatus {
  const ConsentStatus({
    required this.studentId,
    required this.version,
    required this.guardianConsent,
    required this.studentAssent,
    required this.cameraTrainingConsent,
    required this.avatarConsent,
    required this.allRequiredCompleted,
    required this.nextStep,
  });

  factory ConsentStatus.fromJson(Map<String, dynamic> json) {
    return ConsentStatus(
      studentId: json['studentId'] as String,
      version: json['version'] as String,
      guardianConsent: json['guardianConsent'] as bool,
      studentAssent: json['studentAssent'] as bool,
      cameraTrainingConsent: json['cameraTrainingConsent'] as bool,
      avatarConsent: json['avatarConsent'] as bool,
      allRequiredCompleted: json['allRequiredCompleted'] as bool,
      nextStep: json['nextStep'] as String,
    );
  }

  final String studentId;
  final String version;
  final bool guardianConsent;
  final bool studentAssent;
  final bool cameraTrainingConsent;
  final bool avatarConsent;
  final bool allRequiredCompleted;
  final String nextStep;
}

class ConsentSubmission {
  const ConsentSubmission({
    required this.studentId,
    required this.guardianConsent,
    required this.studentAssent,
    required this.cameraTrainingConsent,
    required this.avatarConsent,
  });

  Map<String, dynamic> toJson() {
    return {
      'studentId': studentId,
      'guardianConsent': guardianConsent,
      'studentAssent': studentAssent,
      'cameraTrainingConsent': cameraTrainingConsent,
      'avatarConsent': avatarConsent,
    };
  }

  final String studentId;
  final bool guardianConsent;
  final bool studentAssent;
  final bool cameraTrainingConsent;
  final bool avatarConsent;
}

class ScreeningTemplate {
  const ScreeningTemplate({
    required this.title,
    required this.questionCount,
    required this.estimatedDuration,
    required this.questions,
  });

  factory ScreeningTemplate.fromJson(Map<String, dynamic> json) {
    return ScreeningTemplate(
      title: json['title'] as String,
      questionCount: json['questionCount'] as int,
      estimatedDuration: json['estimatedDuration'] as String,
      questions: (json['questions'] as List)
          .map((item) => ScreeningQuestion.fromJson(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }

  final String title;
  final int questionCount;
  final String estimatedDuration;
  final List<ScreeningQuestion> questions;
}

class ScreeningQuestion {
  const ScreeningQuestion({
    required this.id,
    required this.title,
    required this.options,
  });

  factory ScreeningQuestion.fromJson(Map<String, dynamic> json) {
    return ScreeningQuestion(
      id: json['id'] as String,
      title: json['title'] as String,
      options: (json['options'] as List).cast<String>(),
    );
  }

  final String id;
  final String title;
  final List<String> options;
}

class ScreeningResult {
  const ScreeningResult({
    required this.riskLevel,
    required this.trend,
    required this.disclaimer,
    required this.trainingSuggestions,
    required this.safetyNotice,
  });

  factory ScreeningResult.fromJson(Map<String, dynamic> json) {
    return ScreeningResult(
      riskLevel: json['riskLevel'] as String,
      trend: json['trend'] as String,
      disclaimer: json['disclaimer'] as String,
      trainingSuggestions: (json['trainingSuggestions'] as List).cast<String>(),
      safetyNotice: json['safetyNotice'] as String,
    );
  }

  final String riskLevel;
  final String trend;
  final String disclaimer;
  final List<String> trainingSuggestions;
  final String safetyNotice;
}

class TodayTraining {
  const TodayTraining({
    required this.studentId,
    required this.encouragement,
    required this.tasks,
  });

  factory TodayTraining.fromJson(Map<String, dynamic> json) {
    return TodayTraining(
      studentId: json['studentId'] as String,
      encouragement: json['encouragement'] as String,
      tasks: (json['tasks'] as List)
          .map((item) => TrainingTask.fromJson(Map<String, dynamic>.from(item as Map)))
          .toList(),
    );
  }

  final String studentId;
  final String encouragement;
  final List<TrainingTask> tasks;
}

class TrainingTask {
  const TrainingTask({
    required this.id,
    required this.title,
    required this.duration,
    required this.status,
  });

  factory TrainingTask.fromJson(Map<String, dynamic> json) {
    return TrainingTask(
      id: json['id'] as String,
      title: json['title'] as String,
      duration: json['duration'] as String,
      status: json['status'] as String,
    );
  }

  final String id;
  final String title;
  final String duration;
  final String status;
}
