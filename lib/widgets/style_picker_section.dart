import 'package:flutter/material.dart';
import '../models/main_ui_state.dart';

class StylePickerSection extends StatelessWidget {
  final int selectedIndex;
  final Function(int) onStyleSelected;

  const StylePickerSection({
    super.key,
    required this.selectedIndex,
    required this.onStyleSelected,
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
              'Choose Style',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: List.generate(
                CreativeStyles.styles.length,
                (index) => ChoiceChip(
                  label: Text(CreativeStyles.styles[index]),
                  selected: selectedIndex == index,
                  onSelected: (selected) {
                    if (selected) {
                      onStyleSelected(index);
                    }
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
