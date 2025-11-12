import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../models/main_ui_state.dart';
import '../widgets/api_key_dialog.dart';
import '../widgets/image_picker_section.dart';
import '../widgets/style_picker_section.dart';
import '../widgets/prompt_input_section.dart';
import '../widgets/generation_display.dart';
import '../widgets/action_buttons.dart';

class MainScreen extends StatelessWidget {
  const MainScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('NanoBanana AI Image Editor'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () => _showApiKeyDialog(context),
          ),
        ],
      ),
      body: Consumer<AppProvider>(
        builder: (context, provider, child) {
          final state = provider.state;

          // Show API key dialog if not set
          if (state.apiKey.isEmpty) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              _showApiKeyDialog(context);
            });
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Image Picker Section
                ImagePickerSection(
                  selectedImages: state.selectedImages,
                  onPickImages: provider.pickImages,
                  onRemoveImage: provider.removeSelectedImage,
                  onClearImages: provider.clearSelectedImages,
                ),
                const SizedBox(height: 16),

                // Style Picker Section
                StylePickerSection(
                  selectedIndex: state.selectedStyleIndex,
                  onStyleSelected: provider.updateStyleIndex,
                ),
                const SizedBox(height: 16),

                // Prompt Input Section
                PromptInputSection(
                  prompt: state.currentPrompt,
                  onPromptChanged: provider.updatePrompt,
                  enabled: state.selectedStyleIndex == 4,
                ),
                const SizedBox(height: 16),

                // Action Buttons
                ActionButtons(
                  canGenerate: state.selectedImages.isNotEmpty &&
                      state.currentPrompt.isNotEmpty &&
                      state.apiKey.isNotEmpty,
                  isGenerating: state.generationState is GenerationStateLoading,
                  onGenerate: () => provider.generateContent(null),
                  onEnhance: () => provider.enhanceImage(),
                  canEnhance: state.generationState is GenerationStateSuccess,
                ),
                const SizedBox(height: 24),

                // Generation Display
                GenerationDisplay(
                  generationState: state.generationState,
                  enhancementState: state.enhancementState,
                  onReset: provider.resetToIdle,
                  onSaveVariant: provider.saveAsVariant,
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  void _showApiKeyDialog(BuildContext context) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const ApiKeyDialog(),
    );
  }
}
