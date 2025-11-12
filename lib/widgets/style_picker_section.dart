import 'package:flutter/material.dart';
import '../models/main_ui_state.dart';

class StylePickerSection extends StatefulWidget {
  final int selectedIndex;
  final Function(int) onStyleSelected;

  const StylePickerSection({
    super.key,
    required this.selectedIndex,
    required this.onStyleSelected,
  });

  @override
  State<StylePickerSection> createState() => _StylePickerSectionState();
}

class _StylePickerSectionState extends State<StylePickerSection> {
  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shadowColor: Theme.of(context).colorScheme.primary.withOpacity(0.1),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.palette_rounded,
                  color: Theme.of(context).colorScheme.primary,
                  size: 24,
                ),
                const SizedBox(width: 8),
                Text(
                  'Choose Style',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: List.generate(
                CreativeStyles.styles.length,
                (index) => _buildStyleChip(context, index),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStyleChip(BuildContext context, int index) {
    final isSelected = widget.selectedIndex == index;

    return AnimatedScale(
      scale: isSelected ? 1.05 : 1.0,
      duration: const Duration(milliseconds: 200),
      curve: Curves.easeOutCubic,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color:
                        Theme.of(context).colorScheme.primary.withOpacity(0.3),
                    blurRadius: 8,
                    spreadRadius: 1,
                    offset: const Offset(0, 2),
                  ),
                ]
              : [],
        ),
        child: ChoiceChip(
          label: Text(
            CreativeStyles.styles[index],
            style: TextStyle(
              fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            ),
          ),
          selected: isSelected,
          onSelected: (selected) {
            if (selected) {
              widget.onStyleSelected(index);
            }
          },
          selectedColor: Theme.of(context).colorScheme.primaryContainer,
          backgroundColor: Theme.of(context).colorScheme.surface,
          side: BorderSide(
            color: isSelected
                ? Theme.of(context).colorScheme.primary
                : Theme.of(context).colorScheme.outline.withOpacity(0.3),
            width: isSelected ? 2 : 1,
          ),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          elevation: isSelected ? 3 : 0,
          pressElevation: 2,
        ),
      ),
    );
  }
}
