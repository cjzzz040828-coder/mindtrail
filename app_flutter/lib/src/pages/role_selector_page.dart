import 'package:flutter/material.dart';

import '../api/api_client.dart';
import 'student_flow_pages.dart';

class RoleSelectorPage extends StatelessWidget {
  const RoleSelectorPage({super.key, required this.apiClient});

  final ApiClient apiClient;

  @override
  Widget build(BuildContext context) {
    final roles = [
      const RoleCardData(
        title: '学生端',
        subtitle: '登录、授权、筛查、训练、首页',
        color: Color(0xFF57A87B),
        icon: Icons.school_outlined,
      ),
      const RoleCardData(
        title: '家长端',
        subtitle: '摘要、趋势、提醒、授权',
        color: Color(0xFFF29B42),
        icon: Icons.family_restroom_outlined,
      ),
      const RoleCardData(
        title: '老师端',
        subtitle: '预警、复核、处置、审计',
        color: Color(0xFFE06666),
        icon: Icons.assignment_ind_outlined,
      ),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('心理健康 App'),
        centerTitle: true,
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(24),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    color: const Color(0xFF57A87B).withOpacity(0.12),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: const Text(
                    '学生端最小闭环已接 Mock API',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF3F7E5D),
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  '从原型图走向可运行 MVP',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 8),
                const Text(
                  '当前建议先从学生端跑通完整链路，再逐步补齐家长端、老师端与后台管理。',
                  style: TextStyle(fontSize: 14, color: Color(0xFF66736C)),
                ),
                const SizedBox(height: 12),
                Text(
                  '当前 API：${apiClient.baseUrl}',
                  style: const TextStyle(fontSize: 13, color: Color(0xFF66736C)),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          ...roles.map((role) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: _RoleEntryCard(
                data: role,
                onTap: () {
                  if (role.title == '学生端') {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => StudentLoginPage(apiClient: apiClient),
                      ),
                    );
                    return;
                  }

                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => PlaceholderRolePage(data: role),
                    ),
                  );
                },
              ),
            );
          }),
        ],
      ),
    );
  }
}

class _RoleEntryCard extends StatelessWidget {
  const _RoleEntryCard({
    required this.data,
    required this.onTap,
  });

  final RoleCardData data;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: onTap,
      child: Ink(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Row(
          children: [
            CircleAvatar(
              radius: 28,
              backgroundColor: data.color.withOpacity(0.12),
              child: Icon(data.icon, color: data.color),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    data.title,
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    data.subtitle,
                    style: const TextStyle(fontSize: 14, color: Color(0xFF6C7A73)),
                  ),
                ],
              ),
            ),
            Icon(Icons.chevron_right, color: data.color),
          ],
        ),
      ),
    );
  }
}

class PlaceholderRolePage extends StatelessWidget {
  const PlaceholderRolePage({super.key, required this.data});

  final RoleCardData data;

  @override
  Widget build(BuildContext context) {
    final modules = switch (data.title) {
      '家长端' => ['本周摘要', '趋势详情', '提醒事项', '授权管理'],
      _ => ['预警列表', '复核详情', '处置记录', '审计筛选'],
    };

    return Scaffold(
      appBar: AppBar(title: Text(data.title)),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: data.color.withOpacity(0.12),
              borderRadius: BorderRadius.circular(24),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  data.title,
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.w800, color: data.color),
                ),
                const SizedBox(height: 8),
                Text(
                  data.subtitle,
                  style: const TextStyle(fontSize: 14, color: Color(0xFF5B6761)),
                ),
                const SizedBox(height: 8),
                const Text(
                  '这一端目前保留为结构入口，后续可以按原型图继续细化。',
                  style: TextStyle(fontSize: 14, color: Color(0xFF5B6761)),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          ...modules.map(
            (item) => Card(
              elevation: 0,
              margin: const EdgeInsets.only(bottom: 12),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
              child: ListTile(
                leading: Icon(Icons.radio_button_checked, color: data.color),
                title: Text(item),
                subtitle: const Text('下一步可继续补真实页面和接口'),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class RoleCardData {
  const RoleCardData({
    required this.title,
    required this.subtitle,
    required this.color,
    required this.icon,
  });

  final String title;
  final String subtitle;
  final Color color;
  final IconData icon;
}
