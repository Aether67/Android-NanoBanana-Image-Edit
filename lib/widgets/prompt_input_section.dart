import 'package:flutter/material.dart';

class PromptInputSection extends StatelessWidget {
  final String prompt;
  final Function(String) onPromptChanged;
  final bool enabled;

  const PromptInputSection({
    super.key,
    required this.prompt,
    required this.onPromptChanged,
    required this.enabled,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Prompt',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 12),
            TextField(
              controller: TextEditingController(text: prompt)
                ..selection = TextSelection.collapsed(offset: prompt.length),
              onChanged: onPromptChanged,
              enabled: enabled,
              maxLines: 4,
              decoration: InputDecoration(
                hintText: enabled
                    ? 'Enter your custom prompt...'
                    : 'Select a style or choose Custom Prompt',
                border: const OutlineInputBorder(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
