import 'package:flutter/material.dart';
import '../models/ai_parameters.dart';

class AdvancedAIParametersWidget extends StatefulWidget {
  final AIParameters parameters;
  final Function(AIParameters) onParametersChanged;

  const AdvancedAIParametersWidget({
    super.key,
    required this.parameters,
    required this.onParametersChanged,
  });

  @override
  State<AdvancedAIParametersWidget> createState() =>
      _AdvancedAIParametersWidgetState();
}

class _AdvancedAIParametersWidgetState
    extends State<AdvancedAIParametersWidget> {
  late AIParameters _params;

  @override
  void initState() {
    super.initState();
    _params = widget.parameters;
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.science_rounded,
                  color: Theme.of(context).colorScheme.tertiary,
                  size: 22,
                ),
                const SizedBox(width: 8),
                Text(
                  'Advanced AI Parameters',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const Spacer(),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.tertiaryContainer,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    'BETA',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.onTertiaryContainer,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            _buildSlider(
              context,
              title: 'Creativity Level',
              subtitle: 'Higher values = more creative, less predictable',
              value: _params.creativityLevel,
              min: 0.0,
              max: 1.0,
              divisions: 20,
              onChanged: (value) {
                setState(() {
                  _params = _params.copyWith(creativityLevel: value);
                });
                widget.onParametersChanged(_params);
              },
              icon: Icons.auto_awesome_rounded,
            ),
            const SizedBox(height: 16),
            _buildSlider(
              context,
              title: 'Detail Level',
              subtitle: 'Controls output detail and quality',
              value: _params.detailLevel.toDouble(),
              min: 1.0,
              max: 5.0,
              divisions: 4,
              onChanged: (value) {
                setState(() {
                  _params = _params.copyWith(detailLevel: value.toInt());
                });
                widget.onParametersChanged(_params);
              },
              icon: Icons.high_quality_rounded,
            ),
            const SizedBox(height: 16),
            _buildSlider(
              context,
              title: 'Reasoning Depth',
              subtitle: 'Deeper reasoning for complex tasks',
              value: _params.reasoningDepth.toDouble(),
              min: 1.0,
              max: 5.0,
              divisions: 4,
              onChanged: (value) {
                setState(() {
                  _params = _params.copyWith(reasoningDepth: value.toInt());
                });
                widget.onParametersChanged(_params);
              },
              icon: Icons.psychology_rounded,
            ),
            const SizedBox(height: 16),
            _buildStyleSelector(context),
            const SizedBox(height: 12),
            _buildResetButton(context),
          ],
        ),
      ),
    );
  }

  Widget _buildSlider(
    BuildContext context, {
    required String title,
    required String subtitle,
    required double value,
    required double min,
    required double max,
    required int divisions,
    required ValueChanged<double> onChanged,
    required IconData icon,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(
              icon,
              size: 18,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(width: 8),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  Text(
                    subtitle,
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: Theme.of(context).colorScheme.outline,
                        ),
                  ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primaryContainer,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                value.toStringAsFixed(divisions > 10 ? 2 : 0),
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).colorScheme.onPrimaryContainer,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        SliderTheme(
          data: SliderTheme.of(context).copyWith(
            activeTrackColor: Theme.of(context).colorScheme.primary,
            inactiveTrackColor:
                Theme.of(context).colorScheme.surfaceVariant,
            thumbColor: Theme.of(context).colorScheme.primary,
            overlayColor:
                Theme.of(context).colorScheme.primary.withOpacity(0.2),
            trackHeight: 4,
          ),
          child: Slider(
            value: value,
            min: min,
            max: max,
            divisions: divisions,
            onChanged: onChanged,
          ),
        ),
      ],
    );
  }

  Widget _buildStyleSelector(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(
              Icons.style_rounded,
              size: 18,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(width: 8),
            const Text(
              'Output Style',
              style: TextStyle(fontWeight: FontWeight.w600),
            ),
          ],
        ),
        const SizedBox(height: 8),
        SegmentedButton<AIOutputStyle>(
          segments: const [
            ButtonSegment(
              value: AIOutputStyle.concise,
              label: Text('Concise'),
              icon: Icon(Icons.short_text_rounded, size: 16),
            ),
            ButtonSegment(
              value: AIOutputStyle.balanced,
              label: Text('Balanced'),
              icon: Icon(Icons.align_horizontal_center_rounded, size: 16),
            ),
            ButtonSegment(
              value: AIOutputStyle.detailed,
              label: Text('Detailed'),
              icon: Icon(Icons.article_rounded, size: 16),
            ),
          ],
          selected: {_params.outputStyle},
          onSelectionChanged: (Set<AIOutputStyle> selected) {
            setState(() {
              _params = _params.copyWith(outputStyle: selected.first);
            });
            widget.onParametersChanged(_params);
          },
          style: ButtonStyle(
            backgroundColor: MaterialStateProperty.resolveWith((states) {
              if (states.contains(MaterialState.selected)) {
                return Theme.of(context).colorScheme.primaryContainer;
              }
              return null;
            }),
          ),
        ),
      ],
    );
  }

  Widget _buildResetButton(BuildContext context) {
    return Center(
      child: TextButton.icon(
        onPressed: () {
          setState(() {
            _params = const AIParameters();
          });
          widget.onParametersChanged(_params);
        },
        icon: const Icon(Icons.restart_alt_rounded, size: 18),
        label: const Text('Reset to Defaults'),
      ),
    );
  }
}
