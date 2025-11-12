import 'package:flutter/material.dart';

class PromptInputSection extends StatefulWidget {
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
  State<PromptInputSection> createState() => _PromptInputSectionState();
}

class _PromptInputSectionState extends State<PromptInputSection>
    with SingleTickerProviderStateMixin {
  late TextEditingController _controller;
  late AnimationController _shimmerController;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: widget.prompt);
    _shimmerController = AnimationController(
      duration: const Duration(milliseconds: 2000),
      vsync: this,
    )..repeat();
  }

  @override
  void didUpdateWidget(PromptInputSection oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.prompt != oldWidget.prompt) {
      _controller.text = widget.prompt;
      _controller.selection =
          TextSelection.collapsed(offset: widget.prompt.length);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    _shimmerController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      child: Card(
        elevation: widget.enabled ? 3 : 1,
        shadowColor: Theme.of(context).colorScheme.primary.withOpacity(0.1),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    Icons.edit_note_rounded,
                    color: widget.enabled
                        ? Theme.of(context).colorScheme.primary
                        : Theme.of(context).colorScheme.outline,
                    size: 24,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Prompt',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: widget.enabled
                              ? null
                              : Theme.of(context).colorScheme.outline,
                        ),
                  ),
                  const Spacer(),
                  if (widget.enabled && widget.prompt.isNotEmpty)
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 4,
                      ),
                      decoration: BoxDecoration(
                        color: Theme.of(context).colorScheme.primaryContainer,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        '${widget.prompt.length} chars',
                        style: TextStyle(
                          fontSize: 12,
                          color: Theme.of(context).colorScheme.onPrimaryContainer,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                ],
              ),
              const SizedBox(height: 16),
              AnimatedContainer(
                duration: const Duration(milliseconds: 300),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  border: widget.enabled
                      ? Border.all(
                          color: Theme.of(context)
                              .colorScheme
                              .primary
                              .withOpacity(0.3),
                          width: 2,
                        )
                      : null,
                ),
                child: TextField(
                  controller: _controller,
                  onChanged: widget.onPromptChanged,
                  enabled: widget.enabled,
                  maxLines: 5,
                  style: const TextStyle(
                    fontSize: 15,
                    height: 1.5,
                  ),
                  decoration: InputDecoration(
                    hintText: widget.enabled
                        ? 'Describe your vision in detail...'
                        : 'Select "Custom Prompt" style to enable',
                    hintStyle: TextStyle(
                      color: Theme.of(context).colorScheme.outline,
                    ),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: BorderSide.none,
                    ),
                    filled: true,
                    fillColor: widget.enabled
                        ? Theme.of(context).colorScheme.surface
                        : Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.3),
                    contentPadding: const EdgeInsets.all(16),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
