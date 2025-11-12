import 'package:flutter/material.dart';
import 'dart:typed_data';
import '../models/main_ui_state.dart';

class GenerationDisplay extends StatelessWidget {
  final GenerationState generationState;
  final EnhancementResult? enhancementState;
  final VoidCallback onReset;
  final VoidCallback onSaveVariant;

  const GenerationDisplay({
    super.key,
    required this.generationState,
    this.enhancementState,
    required this.onReset,
    required this.onSaveVariant,
  });

  @override
  Widget build(BuildContext context) {
    if (generationState is GenerationStateIdle) {
      return const SizedBox.shrink();
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Result',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: onReset,
                ),
              ],
            ),
            const SizedBox(height: 12),
            _buildContent(context),
          ],
        ),
      ),
    );
  }

  Widget _buildContent(BuildContext context) {
    if (generationState is GenerationStateLoading) {
      final loading = generationState as GenerationStateLoading;
      return Column(
        children: [
          const CircularProgressIndicator(),
          const SizedBox(height: 16),
          Text(loading.message),
          if (loading.progress > 0)
            Padding(
              padding: const EdgeInsets.only(top: 8),
              child: LinearProgressIndicator(value: loading.progress),
            ),
        ],
      );
    }

    if (generationState is GenerationStateError) {
      final error = generationState as GenerationStateError;
      return Column(
        children: [
          Icon(Icons.error_outline, size: 48, color: Theme.of(context).colorScheme.error),
          const SizedBox(height: 16),
          Text(
            error.message,
            style: TextStyle(color: Theme.of(context).colorScheme.error),
            textAlign: TextAlign.center,
          ),
        ],
      );
    }

    if (generationState is GenerationStateSuccess) {
      final success = generationState as GenerationStateSuccess;
      return Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (success.image != null) ...[
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Image.memory(
                success.image!,
                fit: BoxFit.contain,
              ),
            ),
            const SizedBox(height: 12),
            if (enhancementState != null)
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.primaryContainer,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  children: [
                    Icon(
                      Icons.check_circle,
                      color: Theme.of(context).colorScheme.primary,
                      size: 20,
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        enhancementState!.message.isNotEmpty
                            ? enhancementState!.message
                            : 'Enhanced in ${enhancementState!.processingTimeMs}ms',
                        style: TextStyle(
                          color: Theme.of(context).colorScheme.onPrimaryContainer,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: onSaveVariant,
              icon: const Icon(Icons.save),
              label: const Text('Save as Variant'),
            ),
          ],
          if (success.text != null) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surfaceVariant,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'AI Analysis',
                    style: Theme.of(context).textTheme.titleSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(success.text!),
                ],
              ),
            ),
          ],
          if (success.reasoning != null) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.tertiaryContainer,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Reasoning',
                    style: Theme.of(context).textTheme.titleSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(success.reasoning!),
                ],
              ),
            ),
          ],
        ],
      );
    }

    return const SizedBox.shrink();
  }
}
