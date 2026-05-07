import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/app_models.dart';

class ApiClient {
  ApiClient({required this.baseUrl, http.Client? client}) : _client = client ?? http.Client();

  final String baseUrl;
  final http.Client _client;

  Future<StudentSession> loginStudent({
    required String schoolCode,
    required String studentId,
    required String studentName,
  }) async {
    final data = await _post(
      '/api/v1/auth/student/login',
      {
        'schoolCode': schoolCode,
        'studentId': studentId,
        'studentName': studentName,
      },
    );
    return StudentSession.fromJson(data);
  }

  Future<ConsentStatus> getConsentStatus({required String studentId}) async {
    final data = await _get('/api/v1/consents/status?studentId=$studentId');
    return ConsentStatus.fromJson(data);
  }

  Future<ConsentStatus> submitConsent(ConsentSubmission submission) async {
    final data = await _post('/api/v1/consents/submit', submission.toJson());
    return ConsentStatus.fromJson(data);
  }

  Future<ScreeningTemplate> getScreeningTemplate() async {
    final data = await _get('/api/v1/screenings/template');
    return ScreeningTemplate.fromJson(data);
  }

  Future<ScreeningResult> submitScreening({
    required String studentId,
    required int sleepScore,
    required int stressScore,
    required List<String> answers,
    required String note,
  }) async {
    final data = await _post(
      '/api/v1/screenings/submit',
      {
        'studentId': studentId,
        'sleepScore': sleepScore,
        'stressScore': stressScore,
        'answers': answers,
        'note': note,
      },
    );
    return ScreeningResult.fromJson(data);
  }

  Future<TodayTraining> getTodayTraining({required String studentId}) async {
    final data = await _get('/api/v1/trainings/today?studentId=$studentId');
    return TodayTraining.fromJson(data);
  }

  Future<Map<String, dynamic>> _get(String path) async {
    final response = await _client.get(_uri(path)).timeout(const Duration(seconds: 8));
    return _extractData(response);
  }

  Future<Map<String, dynamic>> _post(String path, Map<String, dynamic> body) async {
    final response = await _client
        .post(
          _uri(path),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode(body),
        )
        .timeout(const Duration(seconds: 8));
    return _extractData(response);
  }

  Uri _uri(String path) {
    final normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.substring(0, baseUrl.length - 1) : baseUrl;
    return Uri.parse('$normalizedBaseUrl$path');
  }

  Map<String, dynamic> _extractData(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiClientException('请求失败：${response.statusCode}');
    }

    final decoded = jsonDecode(response.body) as Map<String, dynamic>;
    if (decoded['success'] != true) {
      throw ApiClientException(decoded['message'] as String? ?? '接口返回失败');
    }

    final data = decoded['data'];
    if (data is! Map) {
      throw ApiClientException('接口数据格式异常');
    }
    return Map<String, dynamic>.from(data);
  }
}

class ApiClientException implements Exception {
  ApiClientException(this.message);

  final String message;

  @override
  String toString() => message;
}
