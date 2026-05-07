import 'package:flutter/material.dart';

import 'api/api_client.dart';
import 'pages/role_selector_page.dart';

void runMentalHealthApp() {
  const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8080',
  );

  runApp(MentalHealthApp(apiClient: ApiClient(baseUrl: apiBaseUrl)));
}

class MentalHealthApp extends StatelessWidget {
  const MentalHealthApp({super.key, required this.apiClient});

  final ApiClient apiClient;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '心理健康 App',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF5AAE84),
          brightness: Brightness.light,
        ),
        scaffoldBackgroundColor: const Color(0xFFF5FAF7),
        useMaterial3: true,
      ),
      home: RoleSelectorPage(apiClient: apiClient),
    );
  }
}
