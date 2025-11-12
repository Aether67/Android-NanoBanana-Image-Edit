import 'package:flutter/material.dart';

class ActionButtons extends StatelessWidget {
  final bool canGenerate;
  final bool isGenerating;
  final VoidCallback onGenerate;
  final VoidCallback onEnhance;
  final bool canEnhance;

  const ActionButtons({
    super.key,
    required this.canGenerate,
    required this.isGenerating,
    required this.onGenerate,
    required this.onEnhance,
    required this.canEnhance,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: ElevatedButton.icon(
            onPressed: canGenerate && !isGenerating ? onGenerate : null,
            icon: isGenerating
                ? const SizedBox(
                    width: 16,
                    height: 16,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.auto_awesome),
            label: Text(isGenerating ? 'Generating...' : 'Generate'),
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
            ),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: ElevatedButton.icon(
            onPressed: canEnhance && !isGenerating ? onEnhance : null,
            icon: const Icon(Icons.auto_fix_high),
            label: const Text('Enhance'),
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
            ),
          ),
        ),
      ],
    );
  }
}
