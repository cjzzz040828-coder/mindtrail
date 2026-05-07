import 'package:flutter/material.dart';

import '../api/api_client.dart';
import '../models/app_models.dart';

class StudentLoginPage extends StatefulWidget {
  const StudentLoginPage({super.key, required this.apiClient});

  final ApiClient apiClient;

  @override
  State<StudentLoginPage> createState() => _StudentLoginPageState();
}

class _StudentLoginPageState extends State<StudentLoginPage> {
  final _schoolCodeController = TextEditingController(text: 'SCH-001');
  final _studentIdController = TextEditingController(text: 'S2024001');
  final _studentNameController = TextEditingController(text: '林同学');

  bool _submitting = false;

  @override
  void dispose() {
    _schoolCodeController.dispose();
    _studentIdController.dispose();
    _studentNameController.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    setState(() => _submitting = true);
    try {
      final session = await widget.apiClient.loginStudent(
        schoolCode: _schoolCodeController.text.trim(),
        studentId: _studentIdController.text.trim(),
        studentName: _studentNameController.text.trim(),
      );
      if (!mounted) {
        return;
      }
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ConsentPage(
            apiClient: widget.apiClient,
            session: session,
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('登录失败：$error')),
      );
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('学生登录')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          _HeroCard(
            color: const Color(0xFF57A87B),
            title: '学生端最小闭环',
            subtitle: '登录后会依次进入授权、筛查、结果、训练和首页。',
          ),
          const SizedBox(height: 20),
          _SectionCard(
            title: '学校与身份信息',
            child: Column(
              children: [
                _LabeledField(label: '学校代码', controller: _schoolCodeController),
                const SizedBox(height: 12),
                _LabeledField(label: '学号', controller: _studentIdController),
                const SizedBox(height: 12),
                _LabeledField(label: '姓名', controller: _studentNameController),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _PrimaryButton(
            text: _submitting ? '登录中...' : '登录并继续',
            onPressed: _submitting ? null : _login,
          ),
        ],
      ),
    );
  }
}

class ConsentPage extends StatefulWidget {
  const ConsentPage({
    super.key,
    required this.apiClient,
    required this.session,
  });

  final ApiClient apiClient;
  final StudentSession session;

  @override
  State<ConsentPage> createState() => _ConsentPageState();
}

class _ConsentPageState extends State<ConsentPage> {
  ConsentStatus? _status;
  String? _error;
  bool _loading = true;
  bool _submitting = false;

  bool _guardianConsent = false;
  bool _studentAssent = false;
  bool _cameraTrainingConsent = false;
  bool _avatarConsent = false;

  @override
  void initState() {
    super.initState();
    _loadStatus();
  }

  Future<void> _loadStatus() async {
    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final status = await widget.apiClient.getConsentStatus(studentId: widget.session.studentId);
      if (!mounted) {
        return;
      }
      setState(() {
        _status = status;
        _guardianConsent = status.guardianConsent;
        _studentAssent = status.studentAssent;
        _cameraTrainingConsent = status.cameraTrainingConsent;
        _avatarConsent = status.avatarConsent;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }
      setState(() => _error = '$error');
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  Future<void> _submit() async {
    setState(() => _submitting = true);
    try {
      final status = await widget.apiClient.submitConsent(
        ConsentSubmission(
          studentId: widget.session.studentId,
          guardianConsent: _guardianConsent,
          studentAssent: _studentAssent,
          cameraTrainingConsent: _cameraTrainingConsent,
          avatarConsent: _avatarConsent,
        ),
      );

      if (!status.allRequiredCompleted) {
        if (!mounted) {
          return;
        }
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('请先完成全部必选授权，再进入筛查流程。')),
        );
        return;
      }

      if (!mounted) {
        return;
      }
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ScreeningIntroPage(
            apiClient: widget.apiClient,
            session: widget.session,
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('提交失败：$error')),
      );
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_error != null) {
      return Scaffold(
        appBar: AppBar(title: const Text('授权中心')),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('授权状态加载失败：$_error'),
                const SizedBox(height: 12),
                FilledButton(onPressed: _loadStatus, child: const Text('重试')),
              ],
            ),
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(title: const Text('同意与授权')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          _HeroCard(
            color: const Color(0xFF57A87B),
            title: '授权版本 ${_status?.version ?? 'v1.0'}',
            subtitle: '监护人同意、学生知情同意、摄像头训练授权为必选项。数字人授权为可选项。',
          ),
          const SizedBox(height: 20),
          _SwitchCard(
            title: '监护人同意',
            subtitle: '用于记录监护人已知晓并同意试点服务。',
            value: _guardianConsent,
            onChanged: (value) => setState(() => _guardianConsent = value),
          ),
          const SizedBox(height: 12),
          _SwitchCard(
            title: '学生知情同意',
            subtitle: '确认学生已知晓服务边界，不用于医学诊断。',
            value: _studentAssent,
            onChanged: (value) => setState(() => _studentAssent = value),
          ),
          const SizedBox(height: 12),
          _SwitchCard(
            title: '摄像头训练授权',
            subtitle: '仅用于结构化训练辅助，不参与心理诊断。',
            value: _cameraTrainingConsent,
            onChanged: (value) => setState(() => _cameraTrainingConsent = value),
          ),
          const SizedBox(height: 12),
          _SwitchCard(
            title: '数字人授权',
            subtitle: '允许在需要时启用数字人陪练，可随时关闭。',
            value: _avatarConsent,
            onChanged: (value) => setState(() => _avatarConsent = value),
          ),
          const SizedBox(height: 16),
          _PrimaryButton(
            text: _submitting ? '提交中...' : '确认授权并继续',
            onPressed: _submitting ? null : _submit,
          ),
        ],
      ),
    );
  }
}

class ScreeningIntroPage extends StatefulWidget {
  const ScreeningIntroPage({
    super.key,
    required this.apiClient,
    required this.session,
  });

  final ApiClient apiClient;
  final StudentSession session;

  @override
  State<ScreeningIntroPage> createState() => _ScreeningIntroPageState();
}

class _ScreeningIntroPageState extends State<ScreeningIntroPage> {
  late Future<ScreeningTemplate> _templateFuture;

  @override
  void initState() {
    super.initState();
    _reloadTemplate();
  }

  void _reloadTemplate() {
    _templateFuture = widget.apiClient.getScreeningTemplate();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('开始筛查')),
      body: FutureBuilder<ScreeningTemplate>(
        future: _templateFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text('筛查模板加载失败：${snapshot.error}'),
                    const SizedBox(height: 12),
                    FilledButton(
                      onPressed: () => setState(_reloadTemplate),
                      child: const Text('重试'),
                    ),
                  ],
                ),
              ),
            );
          }

          final template = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              _HeroCard(
                color: const Color(0xFF57A87B),
                title: template.title,
                subtitle: '当前共 ${template.questionCount} 题，预计 ${template.estimatedDuration} 完成。',
              ),
              const SizedBox(height: 20),
              const _SectionCard(
                title: '筛查说明',
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('1. 当前结果仅作为辅助筛查，不构成医学诊断。'),
                    SizedBox(height: 8),
                    Text('2. 摄像头或数字人信号不会作为风险分级依据。'),
                    SizedBox(height: 8),
                    Text('3. 如出现危机表达，将触发人工复核流程。'),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              _PrimaryButton(
                text: '开始答题',
                onPressed: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => ScreeningQuestionPage(
                        apiClient: widget.apiClient,
                        session: widget.session,
                        template: template,
                      ),
                    ),
                  );
                },
              ),
            ],
          );
        },
      ),
    );
  }
}

class ScreeningQuestionPage extends StatefulWidget {
  const ScreeningQuestionPage({
    super.key,
    required this.apiClient,
    required this.session,
    required this.template,
  });

  final ApiClient apiClient;
  final StudentSession session;
  final ScreeningTemplate template;

  @override
  State<ScreeningQuestionPage> createState() => _ScreeningQuestionPageState();
}

class _ScreeningQuestionPageState extends State<ScreeningQuestionPage> {
  late final List<String?> _answers;
  final _noteController = TextEditingController();
  int _currentIndex = 0;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _answers = List<String?>.filled(widget.template.questions.length, null);
  }

  @override
  void dispose() {
    _noteController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (_answers.any((item) => item == null)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请先完成全部题目。')),
      );
      return;
    }

    setState(() => _submitting = true);
    try {
      final result = await widget.apiClient.submitScreening(
        studentId: widget.session.studentId,
        sleepScore: _mapSleepScore(_answers[0]!),
        stressScore: _mapStressScore(_answers[1]!),
        answers: _answers.cast<String>(),
        note: _noteController.text.trim(),
      );
      if (!mounted) {
        return;
      }
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => ScreeningResultPage(
            apiClient: widget.apiClient,
            session: widget.session,
            result: result,
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('提交失败：$error')),
      );
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }

  int _mapSleepScore(String answer) {
    switch (answer) {
      case '很好':
        return 2;
      case '一般':
        return 5;
      case '不太好':
        return 7;
      default:
        return 9;
    }
  }

  int _mapStressScore(String answer) {
    switch (answer) {
      case '几乎没有':
        return 1;
      case '偶尔':
        return 4;
      case '经常':
        return 7;
      default:
        return 9;
    }
  }

  @override
  Widget build(BuildContext context) {
    final question = widget.template.questions[_currentIndex];
    final progress = (_currentIndex + 1) / widget.template.questions.length;

    return Scaffold(
      appBar: AppBar(title: const Text('筛查答题')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            '第 ${_currentIndex + 1} 题 / 共 ${widget.template.questions.length} 题',
            style: const TextStyle(fontSize: 14, color: Color(0xFF6C7A73)),
          ),
          const SizedBox(height: 8),
          LinearProgressIndicator(value: progress, borderRadius: BorderRadius.circular(999)),
          const SizedBox(height: 20),
          _SectionCard(
            title: question.title,
            child: Column(
              children: question.options.map((option) {
                final selected = _answers[_currentIndex] == option;
                return Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: InkWell(
                    borderRadius: BorderRadius.circular(16),
                    onTap: () => setState(() => _answers[_currentIndex] = option),
                    child: Ink(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: selected ? const Color(0xFF57A87B).withOpacity(0.10) : const Color(0xFFF8FBF9),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: selected ? const Color(0xFF57A87B) : const Color(0xFFE3ECE6),
                        ),
                      ),
                      child: Row(
                        children: [
                          Expanded(child: Text(option, style: const TextStyle(fontSize: 16))),
                          if (selected) const Icon(Icons.check_circle, color: Color(0xFF57A87B)),
                        ],
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: '补充说明（可选）',
            child: TextField(
              controller: _noteController,
              maxLines: 4,
              decoration: const InputDecoration(
                hintText: '如果你愿意，可以补充最近的感受或想说的话。',
                border: InputBorder.none,
              ),
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: _currentIndex == 0 ? null : () => setState(() => _currentIndex -= 1),
                  child: const Text('上一题'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _PrimaryButton(
                  text: _currentIndex == widget.template.questions.length - 1
                      ? (_submitting ? '提交中...' : '提交筛查')
                      : '下一题',
                  onPressed: _submitting
                      ? null
                      : () {
                          if (_answers[_currentIndex] == null) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('请先选择一个答案。')),
                            );
                            return;
                          }

                          if (_currentIndex == widget.template.questions.length - 1) {
                            _submit();
                            return;
                          }

                          setState(() => _currentIndex += 1);
                        },
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class ScreeningResultPage extends StatelessWidget {
  const ScreeningResultPage({
    super.key,
    required this.apiClient,
    required this.session,
    required this.result,
  });

  final ApiClient apiClient;
  final StudentSession session;
  final ScreeningResult result;

  @override
  Widget build(BuildContext context) {
    final riskColor = switch (result.riskLevel) {
      'HIGH' => const Color(0xFFD96A6A),
      'MEDIUM' => const Color(0xFFF0A34A),
      _ => const Color(0xFF57A87B),
    };

    return Scaffold(
      appBar: AppBar(title: const Text('筛查结果')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          _HeroCard(
            color: riskColor,
            title: result.trend,
            subtitle: result.disclaimer,
          ),
          const SizedBox(height: 20),
          _SectionCard(
            title: '建议的下一步',
            child: Column(
              children: result.trainingSuggestions.map((item) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Row(
                    children: [
                      const Icon(Icons.check_circle, color: Color(0xFF57A87B)),
                      const SizedBox(width: 12),
                      Expanded(child: Text(item)),
                    ],
                  ),
                );
              }).toList(),
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: '安全提醒',
            child: Text(result.safetyNotice),
          ),
          const SizedBox(height: 16),
          _PrimaryButton(
            text: '查看今日训练计划',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => TrainingPlanPage(
                    apiClient: apiClient,
                    session: session,
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}

class TrainingPlanPage extends StatefulWidget {
  const TrainingPlanPage({
    super.key,
    required this.apiClient,
    required this.session,
  });

  final ApiClient apiClient;
  final StudentSession session;

  @override
  State<TrainingPlanPage> createState() => _TrainingPlanPageState();
}

class _TrainingPlanPageState extends State<TrainingPlanPage> {
  late final Future<TodayTraining> _trainingFuture;

  @override
  void initState() {
    super.initState();
    _trainingFuture = widget.apiClient.getTodayTraining(studentId: widget.session.studentId);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('今日训练计划')),
      body: FutureBuilder<TodayTraining>(
        future: _trainingFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Text('训练计划加载失败：${snapshot.error}'),
              ),
            );
          }

          final training = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              _HeroCard(
                color: const Color(0xFF57A87B),
                title: '今天的练习安排',
                subtitle: training.encouragement,
              ),
              const SizedBox(height: 20),
              ...training.tasks.map((task) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: _TaskTile(task: task),
                );
              }),
              const SizedBox(height: 8),
              _PrimaryButton(
                text: '进入学生首页',
                onPressed: () {
                  Navigator.of(context).pushAndRemoveUntil(
                    MaterialPageRoute(
                      builder: (_) => StudentHomePage(
                        apiClient: widget.apiClient,
                        session: widget.session,
                      ),
                    ),
                    (route) => route.isFirst,
                  );
                },
              ),
            ],
          );
        },
      ),
    );
  }
}

class StudentHomePage extends StatefulWidget {
  const StudentHomePage({
    super.key,
    required this.apiClient,
    required this.session,
  });

  final ApiClient apiClient;
  final StudentSession session;

  @override
  State<StudentHomePage> createState() => _StudentHomePageState();
}

class _StudentHomePageState extends State<StudentHomePage> {
  late Future<TodayTraining> _trainingFuture;

  @override
  void initState() {
    super.initState();
    _trainingFuture = widget.apiClient.getTodayTraining(studentId: widget.session.studentId);
  }

  Future<void> _refresh() async {
    setState(() {
      _trainingFuture = widget.apiClient.getTodayTraining(studentId: widget.session.studentId);
    });
    await _trainingFuture;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('学生首页')),
      body: RefreshIndicator(
        onRefresh: _refresh,
        child: FutureBuilder<TodayTraining>(
          future: _trainingFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }

            if (snapshot.hasError) {
              return ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(24),
                children: [
                  Text('首页数据加载失败：${snapshot.error}'),
                ],
              );
            }

            final training = snapshot.data!;
            return ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(20),
              children: [
                _HeroCard(
                  color: const Color(0xFF57A87B),
                  title: '你好，${widget.session.studentName}',
                  subtitle: '${widget.session.schoolName} · ${widget.session.className}',
                ),
                const SizedBox(height: 20),
                _SectionCard(
                  title: '今日概览',
                  child: Row(
                    children: const [
                      Expanded(child: _MetricTile(label: '睡眠', value: '一般')),
                      SizedBox(width: 12),
                      Expanded(child: _MetricTile(label: '压力', value: '中等')),
                      SizedBox(width: 12),
                      Expanded(child: _MetricTile(label: '趋势', value: '低风险')),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                _SectionCard(
                  title: '快捷入口',
                  child: Column(
                    children: [
                      _ActionTile(
                        title: '重新筛查',
                        subtitle: '再次进入筛查流程，更新今天的状态',
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => ScreeningIntroPage(
                                apiClient: widget.apiClient,
                                session: widget.session,
                              ),
                            ),
                          );
                        },
                      ),
                      const Divider(height: 24),
                      _ActionTile(
                        title: '授权管理',
                        subtitle: '查看和更新当前同意与授权状态',
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => ConsentPage(
                                apiClient: widget.apiClient,
                                session: widget.session,
                              ),
                            ),
                          );
                        },
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                _SectionCard(
                  title: '今日训练',
                  child: Column(
                    children: training.tasks.map((task) => _TaskTile(task: task)).toList(),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}

class _HeroCard extends StatelessWidget {
  const _HeroCard({
    required this.color,
    required this.title,
    required this.subtitle,
  });

  final Color color;
  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.w800, color: color),
          ),
          const SizedBox(height: 8),
          Text(
            subtitle,
            style: const TextStyle(fontSize: 14, color: Color(0xFF5B6761)),
          ),
        ],
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.child,
  });

  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700)),
          const SizedBox(height: 14),
          child,
        ],
      ),
    );
  }
}

class _PrimaryButton extends StatelessWidget {
  const _PrimaryButton({
    required this.text,
    required this.onPressed,
  });

  final String text;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      child: FilledButton(
        onPressed: onPressed,
        style: FilledButton.styleFrom(
          padding: const EdgeInsets.symmetric(vertical: 16),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
        ),
        child: Text(text),
      ),
    );
  }
}

class _LabeledField extends StatelessWidget {
  const _LabeledField({
    required this.label,
    required this.controller,
  });

  final String label;
  final TextEditingController controller;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      decoration: InputDecoration(
        labelText: label,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(18)),
      ),
    );
  }
}

class _SwitchCard extends StatelessWidget {
  const _SwitchCard({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
      ),
      child: SwitchListTile(
        value: value,
        onChanged: onChanged,
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
        subtitle: Text(subtitle),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      ),
    );
  }
}

class _TaskTile extends StatelessWidget {
  const _TaskTile({required this.task});

  final TrainingTask task;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFFF7FBF8),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          const CircleAvatar(
            radius: 18,
            backgroundColor: Color(0xFFE4F4EA),
            child: Icon(Icons.check_circle_outline, color: Color(0xFF57A87B)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(task.title, style: const TextStyle(fontWeight: FontWeight.w700)),
                const SizedBox(height: 4),
                Text(task.duration, style: const TextStyle(color: Color(0xFF6C7A73))),
              ],
            ),
          ),
          Text(task.status, style: const TextStyle(color: Color(0xFF6C7A73))),
        ],
      ),
    );
  }
}

class _MetricTile extends StatelessWidget {
  const _MetricTile({
    required this.label,
    required this.value,
  });

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFFF7FBF8),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          Text(label, style: const TextStyle(fontSize: 13, color: Color(0xFF6C7A73))),
          const SizedBox(height: 6),
          Text(value, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700)),
        ],
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  const _ActionTile({
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Ink(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          children: [
            const Icon(Icons.arrow_circle_right_outlined, color: Color(0xFF57A87B)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700)),
                  const SizedBox(height: 4),
                  Text(subtitle, style: const TextStyle(color: Color(0xFF6C7A73))),
                ],
              ),
            ),
            const Icon(Icons.chevron_right),
          ],
        ),
      ),
    );
  }
}
