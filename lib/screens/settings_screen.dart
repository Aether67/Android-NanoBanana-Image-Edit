import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../widgets/api_key_dialog.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_rounded),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildSection(
            context,
            title: 'General',
            children: [
              _buildAPIKeySetting(context),
              const SizedBox(height: 12),
              _buildThemeSetting(context),
            ],
          ),
          const SizedBox(height: 24),
          _buildSection(
            context,
            title: 'Performance',
            children: [
              _buildPerformanceInfo(context),
            ],
          ),
          const SizedBox(height: 24),
          _buildSection(
            context,
            title: 'Beta Features',
            subtitle: 'Experimental features (may be unstable)',
            children: [
              _buildBetaFeatureToggle(
                context,
                title: 'Batch Processing',
                subtitle: 'Process multiple images at once',
                icon: Icons.layers_rounded,
                settingKey: 'beta_batch_processing',
              ),
              _buildBetaFeatureToggle(
                context,
                title: 'Image History',
                subtitle: 'Keep track of all generated images',
                icon: Icons.history_rounded,
                settingKey: 'beta_image_history',
              ),
              _buildBetaFeatureToggle(
                context,
                title: 'Advanced AI Parameters',
                subtitle: 'Fine-tune AI generation settings',
                icon: Icons.tune_rounded,
                settingKey: 'beta_advanced_params',
              ),
              _buildBetaFeatureToggle(
                context,
                title: 'Variant Comparison',
                subtitle: 'Compare multiple image variants side-by-side',
                icon: Icons.compare_rounded,
                settingKey: 'beta_variant_comparison',
              ),
            ],
          ),
          const SizedBox(height: 24),
          _buildSection(
            context,
            title: 'About',
            children: [
              _buildAboutInfo(context),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSection(
    BuildContext context, {
    required String title,
    String? subtitle,
    required List<Widget> children,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                    ),
              ),
              if (subtitle != null) ...[
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Theme.of(context).colorScheme.outline,
                      ),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 8),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(8),
            child: Column(
              children: children,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAPIKeySetting(BuildContext context) {
    final provider = context.watch<AppProvider>();
    final hasKey = provider.state.apiKey.isNotEmpty;

    return ListTile(
      leading: Icon(
        Icons.key_rounded,
        color: hasKey
            ? Theme.of(context).colorScheme.primary
            : Theme.of(context).colorScheme.outline,
        size: 22,
      ),
      title: const Text('API Key'),
      subtitle: Text(hasKey ? 'Configured' : 'Not set'),
      trailing: Icon(
        hasKey ? Icons.check_circle_rounded : Icons.error_outline_rounded,
        color: hasKey ? Colors.green : Colors.orange,
        size: 22,
      ),
      onTap: () {
        showDialog(
          context: context,
          builder: (context) => const ApiKeyDialog(),
        );
      },
    );
  }

  Widget _buildThemeSetting(BuildContext context) {
    return ListTile(
      leading: Icon(
        Icons.dark_mode_rounded,
        color: Theme.of(context).colorScheme.primary,
        size: 22,
      ),
      title: const Text('Theme'),
      subtitle: const Text('Dark Minimalist'),
      trailing: const Icon(Icons.check_rounded, size: 22),
    );
  }

  Widget _buildPerformanceInfo(BuildContext context) {
    return ListTile(
      leading: Icon(
        Icons.speed_rounded,
        color: Theme.of(context).colorScheme.primary,
        size: 22,
      ),
      title: const Text('High Refresh Rate'),
      subtitle: const Text('90Hz/120Hz on supported devices'),
      trailing: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primaryContainer,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Text(
          'ACTIVE',
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.bold,
            color: Theme.of(context).colorScheme.onPrimaryContainer,
          ),
        ),
      ),
    );
  }

  Widget _buildBetaFeatureToggle(
    BuildContext context, {
    required String title,
    required String subtitle,
    required IconData icon,
    required String settingKey,
  }) {
    final provider = context.watch<AppProvider>();
    final isEnabled = provider.getBetaFeature(settingKey);

    return ListTile(
      leading: Icon(
        icon,
        color: isEnabled
            ? Theme.of(context).colorScheme.primary
            : Theme.of(context).colorScheme.outline,
      ),
      title: Text(title),
      subtitle: Text(subtitle),
      trailing: Switch(
        value: isEnabled,
        onChanged: (value) {
          provider.setBetaFeature(settingKey, value);
        },
        activeColor: Theme.of(context).colorScheme.primary,
      ),
    );
  }

  Widget _buildAboutInfo(BuildContext context) {
    return Column(
      children: [
        ListTile(
          leading: Icon(
            Icons.info_outline_rounded,
            color: Theme.of(context).colorScheme.primary,
          ),
          title: const Text('Version'),
          subtitle: const Text('2.0.0 (Flutter)'),
        ),
        ListTile(
          leading: Icon(
            Icons.code_rounded,
            color: Theme.of(context).colorScheme.primary,
          ),
          title: const Text('Platform'),
          subtitle: const Text('Cross-platform (iOS + Android)'),
        ),
        ListTile(
          leading: Icon(
            Icons.psychology_rounded,
            color: Theme.of(context).colorScheme.primary,
          ),
          title: const Text('AI Model'),
          subtitle: const Text('Google Gemini 2.0 Flash'),
        ),
      ],
    );
  }
}
