import 'package:flutter/material.dart';

class ActionButtons extends StatefulWidget {
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
  State<ActionButtons> createState() => _ActionButtonsState();
}

class _ActionButtonsState extends State<ActionButtons>
    with SingleTickerProviderStateMixin {
  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);

    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.05).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      label: 'Generate AI content',
      enabled: widget.canGenerate && !widget.isGenerating,
      child: Row(
        children: [
          Expanded(
            flex: 3,
            child: AnimatedBuilder(
            animation: _pulseAnimation,
            builder: (context, child) {
              return Transform.scale(
                scale: widget.canGenerate && !widget.isGenerating
                    ? _pulseAnimation.value
                    : 1.0,
                child: Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(12),
                    boxShadow: widget.canGenerate && !widget.isGenerating
                        ? [
                            BoxShadow(
                              color: Theme.of(context)
                                  .colorScheme
                                  .primary
                                  .withOpacity(0.3 * _pulseAnimation.value),
                              blurRadius: 12 * _pulseAnimation.value,
                              spreadRadius: 2 * (_pulseAnimation.value - 1),
                            ),
                          ]
                        : [],
                  ),
                  child: ElevatedButton.icon(
                    onPressed:
                        widget.canGenerate && !widget.isGenerating ? widget.onGenerate : null,
                    icon: widget.isGenerating
                        ? SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2.5,
                              valueColor: AlwaysStoppedAnimation<Color>(
                                Theme.of(context).colorScheme.onPrimary,
                              ),
                            ),
                          )
                        : const Icon(Icons.auto_awesome_rounded, size: 22),
                    label: Text(
                      widget.isGenerating ? 'Generating...' : 'Generate',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 15,
                      ),
                    ),
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                  ),
                ),
              );
            },
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          flex: 2,
          child: Semantics(
            button: true,
            label: 'Enhance generated image',
            enabled: widget.canEnhance && !widget.isGenerating,
            child: AnimatedScale(
              scale: widget.canEnhance && !widget.isGenerating ? 1.0 : 0.95,
              duration: const Duration(milliseconds: 200),
              child: ElevatedButton.icon(
              onPressed:
                  widget.canEnhance && !widget.isGenerating ? widget.onEnhance : null,
              icon: const Icon(Icons.auto_fix_high_rounded, size: 20),
              label: const Text(
                'Enhance',
                style: TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 14,
                ),
              ),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                backgroundColor: Theme.of(context).colorScheme.secondary,
                foregroundColor: Theme.of(context).colorScheme.onSecondary,
              ),
            ),
          ),
        ),
      ],
    );
  }
}
