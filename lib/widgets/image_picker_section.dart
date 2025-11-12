import 'dart:typed_data';
import 'package:flutter/material.dart';

class ImagePickerSection extends StatelessWidget {
  final List<Uint8List> selectedImages;
  final VoidCallback onPickImages;
  final Function(int) onRemoveImage;
  final VoidCallback onClearImages;

  const ImagePickerSection({
    super.key,
    required this.selectedImages,
    required this.onPickImages,
    required this.onRemoveImage,
    required this.onClearImages,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Select Images',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                if (selectedImages.isNotEmpty)
                  TextButton(
                    onPressed: onClearImages,
                    child: const Text('Clear All'),
                  ),
              ],
            ),
            const SizedBox(height: 12),
            if (selectedImages.isEmpty)
              Center(
                child: ElevatedButton.icon(
                  onPressed: onPickImages,
                  icon: const Icon(Icons.add_photo_alternate),
                  label: const Text('Pick Images'),
                ),
              )
            else
              Column(
                children: [
                  SizedBox(
                    height: 120,
                    child: ListView.builder(
                      scrollDirection: Axis.horizontal,
                      itemCount: selectedImages.length,
                      itemBuilder: (context, index) {
                        return Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: Stack(
                            children: [
                              ClipRRect(
                                borderRadius: BorderRadius.circular(8),
                                child: Image.memory(
                                  selectedImages[index],
                                  width: 120,
                                  height: 120,
                                  fit: BoxFit.cover,
                                ),
                              ),
                              Positioned(
                                top: 4,
                                right: 4,
                                child: IconButton(
                                  icon: const Icon(Icons.close, color: Colors.white),
                                  style: IconButton.styleFrom(
                                    backgroundColor: Colors.black54,
                                    padding: const EdgeInsets.all(4),
                                  ),
                                  onPressed: () => onRemoveImage(index),
                                ),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
                  ),
                  const SizedBox(height: 12),
                  OutlinedButton.icon(
                    onPressed: onPickImages,
                    icon: const Icon(Icons.add),
                    label: const Text('Add More Images'),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
